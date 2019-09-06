package io.github.sgosiaco.view

import io.github.sgosiaco.library.Checkout
import io.github.sgosiaco.library.MyController
import javafx.scene.layout.Priority
import tornadofx.*

class HistoryFragment : Fragment("History") {
    private val controller: MyController by inject()
    private val data = SortedFilteredList(controller.checkedList)

    override val root = vbox {
        data.predicate = { it.returned }
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
