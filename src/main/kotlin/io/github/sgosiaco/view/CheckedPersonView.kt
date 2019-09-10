package io.github.sgosiaco.view

import io.github.sgosiaco.library.Action
import io.github.sgosiaco.library.Checkout
import io.github.sgosiaco.library.MyController
import io.github.sgosiaco.library.Person
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.time.LocalDate

class CheckedPersonView : View("Checked Out (Person)") {
    private val controller: MyController by inject()
    private var search = ""

    override val root = vbox {
        textfield(search) {
            controller.sfCheckedPeopleList.filterWhen(textProperty()) { query, item ->
                item.cNum > 0 && item.containsString(query)
            }
            promptText = "Search ${title}"
        }
        tableview(controller.sfCheckedPeopleList) {
            focusedProperty().onChange {
                controller.focus = "People"
            }
            bindSelected(controller.sPerson)
            vgrow = Priority.ALWAYS
            columnResizePolicy = SmartResize.POLICY
            readonlyColumn("Name", Person::name)
            readonlyColumn("Email", Person::email)
            readonlyColumn("Phone number", Person::phone)
            contextmenu {
                item("Return All").action {
                    selectedItem?.apply {
                        confirm(
                                header = "Return all books borrowed by $name?",
                                actionFn = {
                                    controller.checkedList.filter { !it.returned && it.person == this }.forEach {
                                        controller.returnBook(it)
                                        controller.undoList.add(Action("Returned", it, "Nothing"))
                                    }
                                }
                        )
                    }
                }
                item("Show History").action {
                    selectedItem?.apply {
                        find<HistoryFragment>().openWindow()
                    }
                }
            }
            rowExpander(expandOnDoubleClick = true) { selected ->
                paddingLeft = expanderColumn.width
                val data = SortedFilteredList(controller.checkedList)
                data.predicate = { it.person == selected && !it.returned }
                tableview(data) {
                    vgrow = Priority.ALWAYS
                    columnResizePolicy = SmartResize.POLICY
                    readonlyColumn("Book", Checkout::book) {
                        value {
                            it.value.book.title
                        }
                    }
                    readonlyColumn("Checked Out", Checkout::cDate)
                    readonlyColumn("Due", Checkout::dDate).cellFormat {
                        text = it.toString()
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
    }
}
