package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.model.Checkout
import io.github.sgosiaco.library.controller.MainController
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.time.LocalDate

class CheckedBookView : View("Checked Out (Books)") {
    private val controller: MainController by inject()
    private var search = SimpleStringProperty()
    private var filter = SimpleStringProperty()
    //private val allState = booleanBinding(filter) { value == "All" }
    //private val todayState = booleanBinding(filter) { value == "Today" }
    //private val overdueState = booleanBinding(filter) { value == "Overdue" }

    init {
        search.value = ""
        filter.value = "All"
    }

    override val root = vbox {
        hbox {
            textfield(search) {
                controller.sfCheckedList.filterWhen(textProperty()) { query, item ->
                    when(filter.value) {
                        "All" -> !item.returned && item.containsString(query)
                        "Today" -> !item.returned && item.dDate.isEqual(LocalDate.now()) && item.containsString(query)
                        "Overdue" -> !item.returned && item.dDate.isBefore(LocalDate.now()) && item.containsString(query)
                        else -> !item.returned && item.containsString(query)
                    }
                }
                promptText = "Search $title"
            }
            togglegroup {
                togglebutton("All Books") {
                    action {
                        filter.value = "All"
                        controller.sfCheckedList.predicate = { !it.returned && (if(search.value != "") it.containsString(search.value) else true) }
                    }
                }
                togglebutton("Due today") {
                    action {
                        filter.value = "Today"
                        controller.sfCheckedList.predicate = { !it.returned && it.dDate.isEqual(LocalDate.now()) && (if(search.value != "") it.containsString(search.value) else true) }
                    }
                }
                togglebutton("Overdue") {
                    action {
                        filter.value = "Overdue"
                        controller.sfCheckedList.predicate = { !it.returned && it.dDate.isBefore(LocalDate.now()) && (if(search.value != "") it.containsString(search.value) else true) }
                    }
                }
            }
        }
        tableview(controller.sfCheckedList) {
            vgrow = Priority.ALWAYS
            columnResizePolicy = SmartResize.POLICY
            controller.sfCheckedList.onChange {
                requestResize()
            }
            readonlyColumn("Title", Checkout::book).value {
                it.value.book.title
            }
            readonlyColumn("Author", Checkout::book).value {
                it.value.book.author
            }
            readonlyColumn("Publisher", Checkout::book).value {
                it.value.book.pub
            }
            readonlyColumn("Year", Checkout::book).value {
                it.value.book.year
            }
            readonlyColumn("Person", Checkout::person) {
                value {
                    "${it.value.person.name} <${it.value.person.email}>"
                }
            }
            readonlyColumn("Checked Out", Checkout::cDate).cellFormat {
                text = it.format(controller.dateFormat)
            }
            readonlyColumn("Due", Checkout::dDate).cellFormat {
                text = it.format(controller.dateFormat)
                style {
                    if (it.isBefore(LocalDate.now())) {
                        backgroundColor += c("#8b0000")
                        textFill = Color.WHITE
                    }
                    else if(it.isEqual(LocalDate.now())) {
                        backgroundColor += c("#FFFF99")
                        textFill = Color.BLACK
                    }
                }
            }
            contextmenu {
                item("Return").action {
                    selectedItem?.apply {
                        confirm(
                                header = "Return ${book.title}?",
                                content = "Borrowed by ${person.name} <${person.email}>",
                                actionFn = {
                                    controller.returnBook(this)
                                    controller.undoList.add(Action("Returned", this, "Nothing"))
                                }
                        )
                    }
                }
                item("Show History").action {
                    selectedItem?.apply {
                        controller.sBook.item = book
                        find<HistoryFragment>().openWindow()
                    }
                }
            }
        }
    }

}
