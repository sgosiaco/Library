package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.controller.PeopleConverter
import io.github.sgosiaco.library.model.Action
import tornadofx.*

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
                                val old = controller.sCheckout.item.deepCopy()
                                controller.sCheckout.commit()
                                val new = controller.sCheckout.item.deepCopy()
                                val oldIndex = controller.peopleList.indexOf(old.person)
                                val newIndex = controller.peopleList.indexOf(new.person)

                                if(oldIndex != newIndex) {
                                    controller.peopleList[oldIndex] = old.person.copy(cNum = old.person.cNum - 1) //.apply { cNum -= 1 }
                                    controller.peopleList[newIndex] = new.person.apply { cNum += 1}.copy()
                                }

                                controller.checkedList.remove(new)
                                controller.checkedList.add(new)
                                controller.undoList.add(Action("Edited", old.deepCopy(), new.deepCopy()))
                                controller.redoList.setAll()
                                println("""${old.person} ${old.person.cNum} ${new.person} ${new.person.cNum}""")
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
