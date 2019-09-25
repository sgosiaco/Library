package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Person
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*

class AddPersonFragment : Fragment("Add new person") {
    private val controller: MainController by inject()
    private val name = SimpleStringProperty()
    private val email = SimpleStringProperty()
    private val phone = SimpleLongProperty()
    private val aff = SimpleStringProperty()
    private val affiliations = FXCollections.observableArrayList<String>("Alumni", "Faculty", "Staff", "Student")

    override val root = form {
        fieldset("Info") {
            field("Name:") {
                textfield(name)
            }
            field("Email:") {
                textfield(email)
            }
            field("Phone number:") {
                textfield(phone)
            }
            field("Affiliation:") {
                combobox(aff, affiliations)
            }
        }
        hbox(10.0) {
            button("Add person") {
                disableWhen(name.isNull or email.isNull or aff.isNull)
                action {
                    controller.peopleList.add(Person(name.value, email.value, phone.value, aff.value, 0))
                    controller.undoList.add(Action("Added", controller.peopleList.last().copy(), "Nothing"))
                    controller.redoList.setAll()
                    close()
                }
            }
            button("Cancel").action { close() }
        }
    }
}

//TODO add input validation to fields. Also need to add checking to make sure fields are filled out before adding works