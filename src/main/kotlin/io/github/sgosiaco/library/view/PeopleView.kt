package io.github.sgosiaco.library.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.model.Person
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*
import java.time.LocalDate

class PeopleView : View("People") {
    private val controller: MainController by inject()
    private var search = ""

    override val root = vbox {
        hbox {
            stackpane {
                val searchBox = textfield(search) {
                    controller.sfPeopleList.filterWhen(textProperty()) { query, item ->
                        item.containsString(query)
                    }
                    promptText = "Search title"
                }
                button("X") {
                    visibleWhen { searchBox.textProperty().isNotEmpty }
                    action {
                        searchBox.clear()
                    }
                    stackpaneConstraints {
                        alignment = Pos.CENTER_RIGHT
                    }
                    style {
                        fontWeight = FontWeight.BOLD
                        backgroundRadius += box(0.px)
                        backgroundColor += Color.TRANSPARENT
                        borderColor += box(Color.TRANSPARENT)
                    }
                }
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
                item("Add person") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.USER_PLUS)
                    action { find<AddPersonFragment>().openModal() }
                }
                item("Edit person") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.EDIT)
                    action {
                        selectedItem?.apply {
                            if (cNum > 0) {
                                error("Can't edit a person that has checked out book(s)!")
                            } else {
                                find<EditPersonFragment>().openModal()
                            }
                        }
                    }
                }
                item("Delete person") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.TRASH)
                    action {
                        selectedItem?.apply {
                            if (cNum > 0) {
                                error(header = "Can't delete a person that has checked out book(s)!")
                            } else {
                                confirm(
                                        header = "Delete $name?",
                                        actionFn = {
                                            runAsync {
                                                controller.peopleList.remove(selectedItem)
                                                controller.undoList.add(Action("Deleted", selectedItem as Any, "Nothing"))
                                                controller.redoList.setAll()
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
                item("Draft All Checked Out") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.ENVELOPE_OPEN)
                    action {
                        selectedItem?.apply {
                            if (cNum == 0) {
                                error("No books checked out to this person!")
                            } else {
                                controller.draftAll(this)
                            }
                        }
                    }
                }
                item("Draft Due Tomorrow") {
                    visibleWhen {
                        booleanBinding(controller.checkedList) { any { !it.returned && it.person.email == selectedItem?.email && it.dDate.isEqual(LocalDate.now().plusDays(1)) } }
                    }
                    graphic = FontAwesomeIconView(FontAwesomeIcon.ENVELOPE_OPEN)
                    action {
                        selectedItem?.apply {
                            controller.draftTomorrow(this)
                        }
                    }
                }
                item("Show History") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.HISTORY)
                    action {
                        selectedItem?.apply {
                            find<HistoryFragment>().openWindow()
                        }
                    }
                }
            }
        }
    }
}
