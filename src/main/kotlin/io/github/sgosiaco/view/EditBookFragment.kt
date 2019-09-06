package io.github.sgosiaco.view

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class EditBookFragment : Fragment("Edit Book") {
    private val controller: MyController by inject()
    private var bookTitle = SimpleStringProperty()
    private var author = SimpleStringProperty()
    private var pub = SimpleStringProperty()
    private var year = SimpleIntegerProperty()
    val book: Book by param()

    override val root = form {
        fieldset("Info") {
            field("Title") {
                textfield(bookTitle) {
                    textProperty().set(book.title)
                }
            }
            field("Author(s)") {
                textfield(author) {
                    textProperty().set(book.author)
                }
            }
            field("Publisher(s)") {
                textfield(pub) {
                    textProperty().set(book.pub)
                }
            }
            field("Year") {
                textfield(year) {
                    textProperty().set(book.year.toString())
                }
            }
        }
        button("Edit book") {
            //disableProperty().bind(bookTitle.isNull or author.isNull or pub.isNull)
            action {
                confirm(
                        header = "Save Changes?",
                        actionFn = {
                            val index = controller.bookList.indexOf(book)
                            book.title = bookTitle.value
                            book.author = author.value
                            book.pub = pub.value
                            book.year = year.value.toInt()
                            controller.bookList[index] = book
                        }
                )
                close()
            }
        }
    }
}
