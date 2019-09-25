package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Person
import javafx.scene.layout.Priority
import tornadofx.*

class PeopleView : View("People") {
    private val controller: MainController by inject()
    private var search = ""

    override val root = vbox {
        val searchBox = textfield(search) {
            controller.sfPeopleList.filterWhen(textProperty()) { query, item ->
                item.containsString(query)
            }
            promptText = "Search title"
        }
        button("x").action {
            searchBox.clear()
        }
        tableview(controller.sfPeopleList) {
            focusedProperty().onChange {
                controller.focus = "People"
            }
            bindSelected(controller.sPerson)
            vgrow = Priority.ALWAYS
            columnResizePolicy = SmartResize.POLICY
            controller.sfPeopleList.onChange {
                requestResize()
            }
            readonlyColumn("Name", Person::name)
            readonlyColumn("Email", Person::email)
            readonlyColumn("Phone number", Person::phone)
            readonlyColumn("Affiliation", Person::aff)
            readonlyColumn("Number of books checked out", Person::cNum)

            contextmenu {
                item("Add person").action { find<AddPersonFragment>().openModal() }
                item("Edit person").action {
                    selectedItem?.apply {
                        find<EditPersonFragment>().openModal()
                    }
                }
                item("Delete person").action {
                    selectedItem?.apply {
                        if(cNum > 0) {
                            error(header = "Can't delete a person that has checked out book(s)!")
                        }
                        else {
                            confirm(
                                    header = "Delete $name?",
                                    actionFn = {
                                        controller.undoList.add(Action("Deleted", selectedItem as Any, "Nothing"))
                                        controller.redoList.setAll()
                                        controller.peopleList.remove(selectedItem)
                                    }
                            )
                        }
                    }
                }
                item("Show History").action {
                    selectedItem?.apply {
                        find<HistoryFragment>().openWindow()
                    }
                }
            }
        }
    }
}
