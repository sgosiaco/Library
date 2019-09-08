package io.github.sgosiaco.view

import io.github.sgosiaco.library.MyController
import io.github.sgosiaco.library.Person
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*

class AddPersonFragment : Fragment("Add new person") {
    private val controller: MyController by inject()
    private val name = SimpleStringProperty()
    private val email = SimpleStringProperty()
    private val phone = SimpleIntegerProperty()
    private val aff = SimpleStringProperty()
    private val affiliations = FXCollections.observableArrayList<String>("Alumni", "Faculty", "Staff", "Student")

    override val root = form {
        fieldset("Info") {
            field("Name") {
                textfield(name)
            }
            field("Email") {
                textfield(email)
            }
            field("Phone number") {
                textfield(phone)
            }
            field("Affiliation") {
                combobox(aff, affiliations)
            }
        }
        button("Add person") {
            disableProperty().bind(name.isNull or email.isNull or aff.isNull)
            action {
                controller.peopleList.add(Person(name.value, email.value, phone.value, aff.value, 0))
                close()
            }
        }
    }
}

//TODO add input validation to fields. Also need to add checking to make sure fields are filled out before adding works