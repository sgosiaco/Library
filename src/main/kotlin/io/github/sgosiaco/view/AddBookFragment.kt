package io.github.sgosiaco.view

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
        button("Add book") {
            disableProperty().bind(bookTitle.isNull or author.isNull or pub.isNull)
            action {
                controller.bookList.add(Book(false, author.value, year.value, pub.value, bookTitle.value))
                close()
            }
        }
    }
}
