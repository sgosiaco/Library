package io.github.sgosiaco.view

import io.github.sgosiaco.library.Action
import io.github.sgosiaco.library.MyController
import io.github.sgosiaco.library.Person
import javafx.scene.layout.Priority
import tornadofx.*

class PeopleView : View("People") {
    private val controller: MyController by inject()
    private var search = ""

    override val root = vbox {
        textfield(search) {
            controller.sfPeopleList.filterWhen(textProperty()) { query, item ->
                item.containsString(query)
            }
            promptText = "Search ${title}"
        }
        tableview(controller.sfPeopleList) {
            focusedProperty().onChange {
                controller.focus = "People"
            }
            bindSelected(controller.sPerson)
            vgrow = Priority.ALWAYS
            readonlyColumn("Name", Person::name)
            readonlyColumn("Email", Person::email)
            readonlyColumn("Phone number", Person::phone)
            readonlyColumn("Affiliation", Person::aff)
            columnResizePolicy = SmartResize.POLICY

            contextmenu {
                item("Add person").action { find<AddPersonFragment>().openModal() }
                item("Edit person").action {
                    selectedItem?.apply {
                        find<EditPersonFragment>().openModal()
                    }
                }
                item("Delete person").action {
                    selectedItem?.apply {
                        confirm(
                                header = "Delete $name?",
                                actionFn = {
                                    controller.undoList.add(Action("Deleted", selectedItem as Any, "Nothing"))
                                    controller.peopleList.remove(selectedItem)
                                }
                        )
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
