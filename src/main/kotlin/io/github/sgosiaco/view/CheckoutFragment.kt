package io.github.sgosiaco.view

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import tornadofx.*
import tornadofx.Stylesheet.Companion.textField
import java.text.SimpleDateFormat
import java.time.LocalDate

class CheckoutFragment : Fragment("Checkout") {
    private val name = SimpleStringProperty()
    //private val wDate =
    //private val rDate = SimpleDateFormat()

    override val root = form {
        fieldset("Info") {
            field("Name") {
                textfield().bind(name)
            }
            field("Checkout") {
                datepicker()
            }
            field("Return") {
                datepicker()
            }
        }
        button ("Checkout") {
            action {
                println("Checking out to ${name.value}")
                close()
            }
        }
    }
}
