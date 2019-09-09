package io.github.sgosiaco.view

import io.github.sgosiaco.library.Action
import io.github.sgosiaco.library.Book
import io.github.sgosiaco.library.MyController
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class AddBookFragment : Fragment("Add new book") {
    private val controller: MyController by inject()
    private val bookTitle = SimpleStringProperty()
    private val author = SimpleStringProperty()
    private val pub = SimpleStringProperty()
    private val year = SimpleIntegerProperty()

    override val root = form {
        fieldset("Info") {
            field("Title") {
                textfield(bookTitle)
            }
            field("Author(s)") {
                textfield(author)
            }
            field("Publisher(s)") {
                textfield(pub)
            }
            field("Year") {
                textfield(year)
            }
        }
        hbox(10.0) {
            button("Add book") {
                disableProperty().bind(bookTitle.isNull or author.isNull or pub.isNull)
                action {
                    controller.bookList.add(Book(false, author.value, year.value, pub.value, bookTitle.value))
                    controller.undoList.add(Action("Added", controller.bookList.last(), "Nothing"))
                    close()
                }
            }
            button("Cancel").action { close() }
        }
    }
}

// TODO adding new book to bookList doesn't resort it alpha by title. need to add resort or insert sort by title.
