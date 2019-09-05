package io.github.sgosiaco.view

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.util.StringConverter
import tornadofx.*
import java.time.LocalDate

class PeopleConverter: StringConverter<Person>() {
    override fun fromString(string: String): Person {
        return Person(string, string, 0, string)
    }
    override fun toString(person: Person): String {
        return person.name
    }
}
class CheckoutFragment : Fragment("Checkout") {
    private val controller: MyController by inject()
    private val person = SimpleObjectProperty<Person>()
    private val wDate = SimpleObjectProperty<LocalDate>()
    private val rDate = SimpleObjectProperty<LocalDate>()

    private val items: ObservableList<Book> by param()

    override val root = form {
        fieldset("Info") {
            field("Name") {
                combobox(person, controller.personList) {
                    //items = controller.peopleList
                    converter = PeopleConverter()
                    makeAutocompletable()

                }
            }
            field("Checkout") {
                datepicker(wDate){
                    value = LocalDate.now()
                }
            }
            field("Return") {
                datepicker(rDate)
            }
        }
        button ("Checkout") {
            action {
                val index = controller.bookList.indexOf(items[0])
                items[0].checkedout = true
                controller.bookList[index] = items[0]
                println(items)
                println("Checking out ${items[0]} to ${person.value} on ${wDate.value} and returning on ${rDate.value}")
                controller.checkedList.add(Checkout(person.value, items[0], wDate.value, rDate.value))
                close()
            }
        }
    }
}
