package io.github.sgosiaco.view

import io.github.sgosiaco.library.Book
import io.github.sgosiaco.library.Checkout
import io.github.sgosiaco.library.MyController
import io.github.sgosiaco.library.Person
import javafx.scene.layout.Priority
import tornadofx.*

class HistoryFragment : Fragment() {
    private val controller: MyController by inject()
    val book: Book? by param()
    val person: Person? by param()

    override val root = vbox {
        val data = SortedFilteredList(controller.checkedList)
        data.predicate = { it.returned && (book?.equals(it.book) ?: person?.equals(it.person) ?: false) }
        title = """History of "${book?.title ?: person?.name}" """
        tableview(data) {
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
            readonlyColumn("Book", Checkout::book)
            readonlyColumn("Person", Checkout::person)
            readonlyColumn("Checked Out Date", Checkout::cDate)
            readonlyColumn("Return Date", Checkout::rDate)
            columnResizePolicy = SmartResize.POLICY
        }
    }
}
