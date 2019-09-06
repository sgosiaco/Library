package io.github.sgosiaco.view

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class EditBookFragment : Fragment("Edit Book") {
    private val controller: MyController by inject()
    private var newTitle = SimpleStringProperty()
    private var newAuthor = SimpleStringProperty()
    private var newPub = SimpleStringProperty()
    private var newYear = SimpleIntegerProperty()
    val book: Book by param()

    override val root = form {
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
        button("Edit book") {
            //disableProperty().bind(bookTitle.isNull or author.isNull or pub.isNull)
            action {
                confirm(
                        header = "Apply Changes?",
                        actionFn = {
                            val index = controller.bookList.indexOf(book)
                            /*
                            book.title = bookTitle.value
                            book.author = author.value
                            book.pub = pub.value
                            book.year = year.value.toInt()
                             */
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
