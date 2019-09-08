package io.github.sgosiaco.view

import io.github.sgosiaco.library.Book
import io.github.sgosiaco.library.Checkout
import io.github.sgosiaco.library.MyController
import io.github.sgosiaco.library.Person
import javafx.scene.layout.Priority
import tornadofx.*

class HistoryFragment : Fragment() {
    private val controller: MyController by inject()
    val book: Book? = controller.sBook.item
    val person: Person? = controller.sPerson.item
    private val data = SortedFilteredList(controller.checkedList)
    init {
        data.predicate = { it.returned && (book?.equals(it.book) ?: person?.equals(it.person) ?: false) }
    }

    override val root = vbox {
        tableview(data) {
            title = """History of "${book?.title ?: person?.name}" """
            vgrow = Priority.ALWAYS
            readonlyColumn("Book", Checkout::book).isVisible = controller.sPerson.isNotEmpty
            readonlyColumn("Person", Checkout::person).isVisible = controller.sBook.isNotEmpty
            readonlyColumn("Checked Out Date", Checkout::cDate).prefWidth(300.0)
            readonlyColumn("Return Date", Checkout::rDate).prefWidth(300.0)
            columnResizePolicy = SmartResize.POLICY
            smartResize()
        }
    }
}
