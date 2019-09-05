package io.github.sgosiaco.view

import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.time.LocalDate

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
            readonlyColumn("Withdrawal Date", Checkout::wDate)
            readonlyColumn("Return Date", Checkout::rDate).cellFormat {
                text = it.toString()
                style {
                    if(it.isBefore(LocalDate.now())) {
                        backgroundColor += c("#8b0000")
                        textFill = Color.WHITE
                    }
                    else {
                        backgroundColor += Color.WHITE
                        textFill = Color.BLACK
                    }
                }
            }
            columnResizePolicy = SmartResize.POLICY

        }
    }
}
