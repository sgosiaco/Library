package io.github.sgosiaco.view

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import tornadofx.*
import tornadofx.Stylesheet.Companion.textField
import java.text.SimpleDateFormat
import java.time.LocalDate

class CheckoutFragment : Fragment("Checkout") {
    private val name = SimpleStringProperty()
    private val wDate = SimpleObjectProperty<LocalDate>()
    private val rDate = SimpleObjectProperty<LocalDate>()

    private val items: ObservableList<BookImport> by param()

    override val root = form {
        fieldset("Info") {
            field("Name") {
                textfield().bind(name)
            }
            field("Checkout") {
                datepicker().bind(wDate)
            }
            field("Return") {
                datepicker().bind(rDate)
            }
        }
        button ("Checkout") {
            action {
                println(items)
                println("Checking out ${items[0].title} to ${name.value} on ${wDate.value} and returning on ${rDate.value}")
                close()
            }
        }
    }
}
