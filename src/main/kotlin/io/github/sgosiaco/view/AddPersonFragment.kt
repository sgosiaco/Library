package io.github.sgosiaco.view

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*

class AddPersonFragment : Fragment("My View") {
    private val controller: MyController by inject()
    private var name = SimpleStringProperty()
    private var email = SimpleStringProperty()
    private var phone = SimpleIntegerProperty()
    private var aff = SimpleStringProperty()
    private val affilliations = FXCollections.observableArrayList<String>("Alumni", "Faculty", "Staff", "Student")

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
                combobox(aff, affilliations) {
                    //makeAutocompletable()
                }
            }
        }
        button("Add person") {
            action {
                controller.personList.add(Person(name.value, email.value, phone.value, aff.value))
                close()
            }
        }
    }
}
