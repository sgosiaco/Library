package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.model.Checkout
import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Person
import javafx.beans.property.SimpleObjectProperty
import javafx.util.StringConverter
import tornadofx.*
import java.time.LocalDate

class PeopleConverter: StringConverter<Person>() {
    override fun fromString(string: String): Person {
        return Person()
    }
    override fun toString(person: Person): String {
        return "${person.name} <${person.email}>"
    }
}

class CheckoutFragment : Fragment() {
    private val controller: MainController by inject()
    private val person = SimpleObjectProperty<Person>()
    private val cDate = SimpleObjectProperty<LocalDate>()
    private val dDate = SimpleObjectProperty<LocalDate>()
    private val book = controller.sBook.item

    override fun onDock() {
        currentStage?.height = currentStage?.height?.plus(5.0) ?: 0.0
        currentStage?.isResizable = false
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
                datepicker(cDate) {
                    value = LocalDate.now()
                    cDate.onChange {
                        if(cDate.value.isAfter(dDate.value)) {
                            this.value = dDate.value
                        }
                    }
                }
            }
            field("Due Date") {
                 datepicker(dDate) {
                     value = LocalDate.now().plusWeeks(2)
                     dDate.onChange {
                         if(dDate.value.isBefore(cDate.value)) {
                             this.value = cDate.value
                         }
                     }
                }
            }
        }
        hbox(10.0) {
            button ("Checkout") {
                enableWhen( person.isNotNull)
                action {
                    confirm(
                            header = "Checkout book?",
                            content = """Checkout "${book.title}" to ${person.value.name}?""",
                            actionFn = {
                                controller.checkBook(Checkout(person.value, book, cDate.value, dDate.value, null, false))
                                //person.value.checked.add(controller.checkedList.last())
                                controller.undoList.add(Action("Checkout", controller.checkedList.last(), "Nothing"))
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
