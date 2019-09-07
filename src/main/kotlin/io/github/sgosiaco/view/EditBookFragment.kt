package io.github.sgosiaco.view

import io.github.sgosiaco.library.Book
import io.github.sgosiaco.library.BookModel
import io.github.sgosiaco.library.MyController
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.util.converter.IntegerStringConverter
import javafx.util.converter.NumberStringConverter
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
        /*
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

         */
        fieldset("Info") {
            field("Title") {
                textfield(controller.bookModel.title)
            }
            field("Author(s)") {
                textfield(controller.bookModel.author)
            }
            field("Publisher(s)") {
                textfield(controller.bookModel.pub)
            }
            field("Year") {
                textfield(controller.bookModel.year, NumberStringConverter())
            }
        }
        hbox {
            button("Save") {
                enableWhen(controller.bookModel.dirty)
                action {
                    confirm(
                            header = "Apply Changes?",
                            actionFn = {
                                controller.bookModel.commit()
                                close()
                            }
                    )
                }
            }
            button("Cancel").action {
                //add rollback here?
                close()
            }
            button("Reset").action {
                controller.bookModel.rollback()
            }
        }
    }
}
