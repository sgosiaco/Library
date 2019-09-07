package io.github.sgosiaco.view

import io.github.sgosiaco.library.MyController
import io.github.sgosiaco.library.Person
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*

class EditPersonFragment : Fragment() {
    private val controller: MyController by inject()
    private var newName = SimpleStringProperty()
    private var newEmail = SimpleStringProperty()
    private var newPhone = SimpleStringProperty()
    private var newAff = SimpleStringProperty()
    private val affiliations = FXCollections.observableArrayList<String>("Alumni", "Faculty", "Staff", "Student")
    val person: Person by param()

    override val root = form {
        fieldset("Info") {
            field("Name") {
                textfield(newName) {
                    textProperty().set(person.name)
                }
            }
            field("Email") {
                textfield(newEmail) {
                    textProperty().set(person.email)
                }
            }
            field("Phone number") {
                textfield(newPhone) {
                    textProperty().set(person.phone.toString())
                }
            }
            field("Affiliation") {
                combobox(newAff, affiliations) {
                    this.value = person.aff
                }
            }
        }
        button("Save") {
            action {
                confirm(
                        header = "Apply Changes?",
                        actionFn = {
                            val index = controller.peopleList.indexOf(person)
                            person.apply {
                                name = newName.value
                                email = newEmail.value
                                phone = newPhone.value.toInt()
                                aff = newAff.value
                            }
                            controller.peopleList[index] = person
                            close()
                        }
                )
            }
        }
    }
}
