package io.github.sgosiaco.view

import io.github.sgosiaco.library.Action
import io.github.sgosiaco.library.Book
import io.github.sgosiaco.library.MyController
import javafx.scene.layout.Priority
import tornadofx.*

class BookView : View("Books") {
    private val controller: MyController by inject()
    private var search = ""

    override val root = vbox {
        textfield(search) {
            controller.sfBookList.filterWhen(textProperty()) { query, item ->
                !item.checkedout && item.containsString(query)
            }
            promptText = "Search ${title}"
        }
        tableview(controller.sfBookList) {
            focusedProperty().onChange {
                controller.focus = "Books"
            }
            bindSelected(controller.sBook)
            vgrow = Priority.ALWAYS
            readonlyColumn("Title", Book::title) {
                value {
                    if(it.value.dupe > 0) "${it.value.title} (${it.value.dupe})" else it.value.title
                }
            }
            readonlyColumn("Author", Book::author)
            readonlyColumn("Publisher", Book::pub)
            readonlyColumn("Year", Book::year)
            columnResizePolicy = SmartResize.POLICY

            contextmenu {
                item("Add book").action { find<AddBookFragment>().openModal() }
                item("Edit book").action {
                    selectedItem?.apply {
                        find<EditBookFragment>().openModal() //use openWindow to allow selecting dif book while window open
                    }
                }
                item("Delete book").action {
                    selectedItem?.apply {
                        confirm(
                                header = "Delete $title?",
                                actionFn = {
                                    controller.undoList.add(Action("Deleted", selectedItem as Any, "Nothing"))
                                    controller.bookList.remove(selectedItem)
                                }
                        )
                    }
                }
                item("Checkout").action {
                    selectedItem?.apply {
                        find<CheckoutFragment>().openModal()
                    }
                }
                item("Show History").action {
                    selectedItem?.apply {
                        find<HistoryFragment>().openWindow()
                    }
                }
            }

        }
    }
}
