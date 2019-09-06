package io.github.sgosiaco.view

import io.github.sgosiaco.library.Book
import io.github.sgosiaco.library.MyController
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class EditBookFragment : Fragment() {
    private val controller: MyController by inject()
    private var newTitle = SimpleStringProperty()
    private var newAuthor = SimpleStringProperty()
    private var newPub = SimpleStringProperty()
    private var newYear = SimpleIntegerProperty()
    val book: Book by param()

    override val root = form {
        title = """Editing ${book.title}"""
        fieldset("Info") {
            field("Title") {
                textfield(newTitle) {
                    textProperty().set(book.title)
                }
            }
            field("Author(s)") {
                textfield(newAuthor) {
                    textProperty().set(book.author)
                }
            }
            field("Publisher(s)") {
                textfield(newPub) {
                    textProperty().set(book.pub)
                }
            }
            field("Year") {
                textfield(newYear) {
                    textProperty().set(book.year.toString())
                }
            }
        }
        button("Save") {
            action {
                confirm(
                        header = "Apply Changes?",
                        actionFn = {
                            val index = controller.bookList.indexOf(book)
                            book.apply {
                                title = newTitle.value
                                author = newAuthor.value
                                pub = newPub.value
                                year = newYear.value.toInt()
                            }
                            controller.bookList[index] = book
                            close()
                        }
                )
            }
        }
    }
}
