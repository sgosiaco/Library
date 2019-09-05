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
    val book: Book by param()

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
                val index = controller.bookList.indexOf(book)
                book.checkedout = true
                controller.bookList[index] = book
                println(book)
                println("Checking out $book to ${person.value} on ${wDate.value} and returning on ${rDate.value}")
                controller.checkedList.add(Checkout(person.value, book, wDate.value, rDate.value, false))
                close()
            }
        }
    }
}
