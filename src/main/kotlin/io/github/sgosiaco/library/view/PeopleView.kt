package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Person
import javafx.scene.layout.Priority
import tornadofx.*
import java.time.LocalDate

class PeopleView : View("People") {
    private val controller: MainController by inject()
    private var search = ""

    override val root = vbox {
        hbox {
            val searchBox = textfield(search) {
                controller.sfPeopleList.filterWhen(textProperty()) { query, item ->
                    item.containsString(query)
                }
                promptText = "Search title"
            }
            button("x").action {
                searchBox.clear()
            }
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
                        if(cNum > 0) {
                            error("Can't edit a person that has checked out book(s)!")
                        }
                        else {
                            find<EditPersonFragment>().openModal()
                        }
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
                item("Draft All Checked Out") {
                    action {
                        selectedItem?.apply {
                            if(cNum == 0) {
                                error("No books checked out to this person!")
                            }
                            else {
                                controller.draftAll(this)
                            }
                        }
                    }
                }
                item("Draft Due Tomorrow") {
                    visibleWhen {
                        booleanBinding(controller.checkedList) { any { !it.returned && it.person == selectedItem && it.dDate.isEqual(LocalDate.now().plusDays(1)) } }
                    }
                    action {
                        selectedItem?.apply {
                            controller.draftTomorrow(this)
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
