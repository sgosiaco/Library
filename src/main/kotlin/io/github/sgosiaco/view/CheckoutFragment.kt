package io.github.sgosiaco.view

import javafx.beans.property.SimpleObjectProperty
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
    private val cDate = SimpleObjectProperty<LocalDate>()
    private val dDate = SimpleObjectProperty<LocalDate>()
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
            field("Checkout Date") {
                datepicker(cDate){
                    value = LocalDate.now()
                }
            }
            field("Due Date") {
                datepicker(dDate) {
                    value = LocalDate.now().plusWeeks(2)
                }
            }
        }
        button ("Checkout") {
            action {
                val index = controller.bookList.indexOf(book)
                book.checkedout = true
                controller.bookList[index] = book
                println(book)
                println("Checking out $book to ${person.value} on ${cDate.value} and returning on ${dDate.value}")
                controller.checkedList.add(Checkout(person.value, book, cDate.value, dDate.value, null,false))
                close()
            }
        }
    }
}
