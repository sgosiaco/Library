package io.github.sgosiaco.view

import io.github.sgosiaco.library.Action
import io.github.sgosiaco.library.MyController
import javafx.collections.FXCollections
import javafx.util.converter.IntegerStringConverter
import tornadofx.*

class EditPersonFragment : Fragment() {
    private val controller: MyController by inject()
    private val affiliations = FXCollections.observableArrayList<String>("Alumni", "Faculty", "Staff", "Student")

    override val root = form {
        title = """Editing ${controller.sPerson.name.value}"""
        fieldset("Info") {
            field("Name") {
                textfield(controller.sPerson.name)
            }
            field("Email") {
                textfield(controller.sPerson.email)
            }
            field("Phone number") {
                textfield(controller.sPerson.phone, IntegerStringConverter())
            }
            field("Affiliation") {
                combobox(controller.sPerson.aff, affiliations)
            }
        }
        hbox(10.0) {
            button("Save") {
                enableWhen(controller.sPerson.dirty)
                action {
                    confirm(
                            header = "Apply Changes?",
                            actionFn = {
                                val index = controller.peopleList.indexOf(controller.sPerson.item)
                                val old = controller.sPerson.item.copy()
                                controller.sPerson.commit()
                                controller.peopleList[index] = controller.sPerson.item
                                controller.undoList.add(Action("Edited", old, controller.peopleList[index].copy()))
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
