package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.model.Book
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class AddBookFragment : Fragment("Add new book") {
    private val controller: MainController by inject()
    private val bookTitle = SimpleStringProperty()
    private val author = SimpleStringProperty()
    private val pub = SimpleStringProperty()
    private val year = SimpleIntegerProperty()

    override val root = form {
        fieldset("Info") {
            field("Title:") {
                textfield(bookTitle)
            }
            field("Author(s):") {
                textfield(author)
            }
            field("Publisher(s):") {
                textfield(pub)
            }
            field("Year:") {
                textfield(year)
            }
        }
        hbox(10.0) {
            button("Add book") {
                disableWhen(bookTitle.isNull or author.isNull or pub.isNull)
                action {
                    runAsync {
                        val book = Book(0, false, author.value, year.value, pub.value, bookTitle.value)
                        controller.checkDupeBook(book)
                        controller.bookList.add(book)
                        controller.undoList.add(Action("Added", controller.bookList.last().copy(), "Nothing"))
                        controller.redoList.setAll()
                    }
                    close()
                }
            }
            button("Cancel").action { close() }
        }
    }
}

// TODO adding new book to bookList doesn't resort it alpha by title. need to add resort or insert sort by title.
