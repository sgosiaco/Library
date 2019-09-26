package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.model.Person
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.*

class AddPersonFragment : Fragment("Add new person") {
    private val controller: MainController by inject()
    private val model = ViewModel()
    private val name = model.bind { SimpleStringProperty() }
    private val email = model.bind { SimpleStringProperty() }
    private val phone = model.bind { SimpleLongProperty() }
    private val aff = model.bind { SimpleStringProperty() }
    private val affiliations = FXCollections.observableArrayList<String>("Alumni", "Faculty", "Staff", "Student")

    override val root = form {
        fieldset("Info") {
            field("Name:") {
                textfield(name) {
                    required()
                    whenDocked { requestFocus() }
                }
            }
            field("Email:") {
                textfield(email).validator {
                    if (controller.checkDupeEmail(it ?: "", controller.sPerson.item)) {
                        error("This email is a duplicate")
                    } else {
                        null
                    }
                }
            }
            field("Phone number:") {
                textfield(phone).validator {
                    val text = it ?: ""
                    if (text.matches("^[2-9]\\d{2}\\d{3}\\d{4}\$".toRegex())) {
                        null
                    } else {
                        error("Not a valid phone number")
                    }
                }
            }
            field("Affiliation:") {
                combobox(aff, affiliations).required()
            }
        }
        hbox(10.0) {
            button("Add person") {
                enableWhen(model.valid)
                action {
                    runAsync {
                        model.commit()
                        controller.peopleList.add(Person(name.value, email.value, phone.value as Long, aff.value, 0))
                        controller.undoList.add(Action("Added", controller.peopleList.last().copy(), "Nothing"))
                        controller.redoList.setAll()
                    }
                    close()
                }
            }
            button("Cancel").action { close() }
        }
    }
}