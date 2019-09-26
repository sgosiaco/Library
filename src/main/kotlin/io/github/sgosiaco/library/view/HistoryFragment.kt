package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.model.Book
import io.github.sgosiaco.library.model.Checkout
import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Person
import javafx.scene.layout.Priority
import tornadofx.*

class HistoryFragment : Fragment() {
    private val controller: MainController by inject()
    private val book: Book? = controller.sBook.item
    private val person: Person? = controller.sPerson.item
    private val data = SortedFilteredList(controller.checkedList)
    init {
        when(controller.focus) {
            "Books" -> {
                data.predicate = { it.returned && (book?.equals(it.book) ?: false) }
                title = """History of "${book?.title}" """
            }
            "People" -> {
                data.predicate = { it.returned && person?.email.equals(it.person.email) }
                title = """History of "${person?.name}" """
            }
            else -> data.predicate = { it.returned && (book?.equals(it.book) ?: person?.email.equals(it.person.email) ?: false) }
        }
    }

    override val root = vbox {
        tableview(data) {
            vgrow = Priority.ALWAYS
            columnResizePolicy = SmartResize.POLICY
            readonlyColumn("Book", Checkout::book).isVisible = controller.focus == "People" //controller.sPerson.isNotEmpty
            readonlyColumn("Person", Checkout::person).isVisible = controller.focus == "Books"//controller.sBook.isNotEmpty
            readonlyColumn("Checked Out Date", Checkout::cDate) {
                cellFormat {
                    text = it.format(controller.dateFormat)
                }
                prefWidth(300.0)
            }
            readonlyColumn("Return Date", Checkout::rDate) {
                cellFormat {
                    text = it?.format(controller.dateFormat)
                }
                prefWidth(300.0)
            }
        }
    }
}
