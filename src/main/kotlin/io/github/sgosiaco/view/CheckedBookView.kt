package io.github.sgosiaco.view

import io.github.sgosiaco.library.Action
import io.github.sgosiaco.library.Checkout
import io.github.sgosiaco.library.MyController
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.time.LocalDate

class CheckedBookView : View("Checked Out (Books)") {
    private val controller: MyController by inject()
    private var search = ""

    override val root = vbox {
        textfield(search) {
            controller.sfCheckedList.filterWhen(textProperty()) { query, item ->
                !item.returned && item.containsString(query)
            }
            promptText = "Search ${title}"
        }
        tableview(controller.sfCheckedList) {
            vgrow = Priority.ALWAYS
            columnResizePolicy = SmartResize.POLICY
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
