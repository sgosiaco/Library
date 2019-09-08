package io.github.sgosiaco.view

import io.github.sgosiaco.library.Checkout
import io.github.sgosiaco.library.MyController
import io.github.sgosiaco.library.Person
import javafx.beans.property.SimpleObjectProperty
import javafx.util.StringConverter
import tornadofx.*
import java.time.LocalDate

class PeopleConverter: StringConverter<Person>() {
    override fun fromString(string: String): Person {
        return Person()
    }
    override fun toString(person: Person): String {
        return person.name
    }
}

class CheckoutFragment : Fragment() {
    private val controller: MyController by inject()
    private val person = SimpleObjectProperty<Person>()
    private val cDate = SimpleObjectProperty<LocalDate>()
    private val dDate = SimpleObjectProperty<LocalDate>()
    private val book = controller.sBook.item

    override fun onDock() {
        currentStage?.height = currentStage?.height?.plus(15.0) ?: 0.0
    }

    override val root = form {
        title = "Checkout"
        fieldset("Info") {
            field("Name") {
                combobox(person, controller.peopleList) {
                    converter = PeopleConverter()
                    makeAutocompletable()
                }
            }
            field("Checkout Date") {
                datepicker(cDate).value = LocalDate.now()
            }
            field("Due Date") {
                datepicker(dDate).value = LocalDate.now().plusWeeks(2)
            }
        }
        hbox {
            button ("Checkout") {
                enableWhen( person.isNotNull)
                action {
                    confirm(
                            header = "Checkout book?",
                            content = """Checkout "${book.title}" to ${person.value.name}?""",
                            actionFn = {
                                var index = controller.bookList.indexOf(book)
                                book.checkedout = true
                                controller.bookList[index] = book
                                index = controller.peopleList.indexOf(person.value)
                                person.value.cNum += 1
                                controller.peopleList[index] = person.value
                                controller.checkedList.add(Checkout(person.value, book, cDate.value, dDate.value, null,false))
                                close()
                            }
                    )

                }
            }
            button("Cancel").action {
                close()
            }
        }
    }
}
