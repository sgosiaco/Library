package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.controller.PeopleConverter
import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.model.Checkout
import io.github.sgosiaco.library.model.Person
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import java.time.LocalDate

class EditCheckoutFragment : Fragment() {
    private val controller: MainController by inject()

    override fun onDock() {
        currentStage?.height = currentStage?.height?.plus(10.0) ?: 0.0
        currentStage?.isResizable = false
    }

    override val root = form {
        title = "Editing ${controller.sCheckout.book.value.title}"
        fieldset("Info") {
            field("Name") {
                combobox(controller.sCheckout.person, controller.peopleList) {
                    converter = PeopleConverter()
                    makeAutocompletable()
                }
            }
            field("Checkout Date") {
                datepicker(controller.sCheckout.cDate) {
                    controller.sCheckout.cDate.onChange {
                        if(controller.sCheckout.cDate.value != null) {
                            controller.sCheckout.dDate.value = controller.sCheckout.cDate.value.plusWeeks(2)
                        }
                    }
                }
            }
            field("Due Date") {
                datepicker(controller.sCheckout.dDate) {
                    controller.sCheckout.dDate.onChange {
                        if(controller.sCheckout.cDate.value != null && controller.sCheckout.dDate.value != null) {
                            if(controller.sCheckout.dDate.value.isBefore(controller.sCheckout.cDate.value)) {
                                this.value = controller.sCheckout.cDate.value
                            }
                        }
                    }
                }
            }
        }
        hbox(10.0) {
            button("Save") {
                enableWhen(controller.sCheckout.dirty)
                action {
                    confirm (
                            header = "Apply Changes?",
                            actionFn = {
                                val old = controller.sCheckout.item.copy()
                                controller.sCheckout.item.person.cNum -= 1
                                controller.sCheckout.commit()
                                val new = controller.sCheckout.item.copy()
                                controller.checkedList.remove(old)
                                controller.checkBook(new)
                                controller.undoList.add(Action("Edited", old, new))
                                close()
                            }
                    )
                }
            }
            button("Cancel").action {
                controller.sCheckout.rollback()
                close()
            }
            button("Reset") {
                enableWhen(controller.sCheckout.dirty)
                action {
                    controller.sCheckout.rollback()
                }
            }
        }
    }
}
