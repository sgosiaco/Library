package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Action
import javafx.collections.FXCollections
import javafx.util.converter.LongStringConverter
import tornadofx.*

class EditPersonFragment : Fragment() {
    private val controller: MainController by inject()
    private val affiliations = FXCollections.observableArrayList<String>("Alumni", "Faculty", "Staff", "Student")

    override val root = form {
        title = """Editing ${controller.sPerson.name.value}"""
        fieldset("Info") {
            field("Name:") {
                textfield(controller.sPerson.name).required()
            }
            field("Email:") {
                textfield(controller.sPerson.email).validator {
                    if (controller.checkDupeEmail(it ?: "", controller.sPerson.item)) {
                        error("This email is a duplicate")
                    } else {
                        null
                    }
                }
            }
            field("Phone number:") {
                textfield(controller.sPerson.phone, LongStringConverter()).validator {
                    val text = it ?: ""
                    if (text.matches("^[2-9]\\d{2}\\d{3}\\d{4}\$".toRegex())) {
                        null
                    } else {
                        error("Not a valid phone number")
                    }
                }
            }
            field("Affiliation:") {
                combobox(controller.sPerson.aff, affiliations)
            }
        }
        hbox(10.0) {
            button("Save") {
                enableWhen { controller.sPerson.valid }
                action {
                    confirm(
                            header = "Apply Changes?",
                            actionFn = {
                                runAsync {
                                    val index = controller.peopleList.indexOf(controller.sPerson.item)
                                    val old = controller.sPerson.item.copy()
                                    controller.sPerson.commit()
                                    controller.peopleList[index] = controller.sPerson.item
                                    controller.undoList.add(Action("Edited", old, controller.peopleList[index].copy()))
                                    controller.redoList.setAll()
                                }
                                close()
                            }
                    )
                }
            }
            button("Cancel").action {
                controller.sPerson.rollback()
                close()
            }
            button("Reset") {
                enableWhen(controller.sPerson.dirty)
                action {
                    controller.sPerson.rollback()
                }
            }
        }
    }
}
