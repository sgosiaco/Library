package io.github.sgosiaco.library.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.model.Checkout
import io.github.sgosiaco.library.model.Person
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*
import java.time.LocalDate

class CheckedPersonView : View("Checked Out (Person)") {
    private val controller: MainController by inject()
    private var search = ""

    override val root = vbox {
        hbox {
            stackpane {
                val searchBox = textfield(search) {
                    controller.sfCheckedPeopleList.filterWhen(textProperty()) { query, item ->
                        item.cNum > 0 && item.containsString(query)
                    }
                    promptText = "Search $title"
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
        tableview(controller.sfCheckedPeopleList) {
            focusedProperty().onChange {
                controller.focus = "People"
            }
            bindSelected(controller.sPerson)
            vgrow = Priority.ALWAYS
            columnResizePolicy = SmartResize.POLICY
            readonlyColumn("Name", Person::name)
            readonlyColumn("Email", Person::email)
            readonlyColumn("Phone number", Person::phone)
            contextmenu {
                item("Draft All Checked Out") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.ENVELOPE_OPEN)
                    action {
                        selectedItem?.apply {
                            controller.draftAll(this)
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
                item("Return All") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.EXCHANGE)
                    action {
                        selectedItem?.apply {
                            confirm(
                                    header = "Return all books borrowed by $name?",
                                    actionFn = {
                                        controller.checkedList.filter { !it.returned && it.person.email == this.email }.forEach {
                                            controller.returnBook(it)
                                            controller.undoList.add(Action("Returned", it.copy(), "Nothing"))
                                        }
                                        controller.redoList.setAll()
                                    }
                            )
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
            rowExpander(expandOnDoubleClick = true) { selected ->
                paddingLeft = expanderColumn.width
                val data = SortedFilteredList(controller.checkedList)
                data.predicate = { it.person.email == selected.email && !it.returned }
                tableview(data) {
                    vgrow = Priority.ALWAYS
                    columnResizePolicy = SmartResize.POLICY
                    bindSelected(controller.sCheckout)
                    readonlyColumn("Book", Checkout::book).value { it.value.book.title }
                    readonlyColumn("Checked Out", Checkout::cDate).value { it.value.cDate.format(controller.dateFormat) }
                    readonlyColumn("Due", Checkout::dDate).cellFormat {
                        text = it.format(controller.dateFormat)
                        style {
                            if (it.isBefore(LocalDate.now())) {
                                backgroundColor += c("#8b0000")
                                textFill = Color.WHITE
                            } else if (it.isEqual(LocalDate.now())) {
                                backgroundColor += c("#FFFF99")
                                textFill = Color.BLACK
                            }
                        }
                    }
                    contextmenu {
                        item("Edit Checkout") {
                            graphic = FontAwesomeIconView(FontAwesomeIcon.EDIT)
                            action {
                                selectedItem?.apply {
                                    find<EditCheckoutFragment>().openModal()
                                }
                            }
                        }
                        item("Return") {
                            graphic = FontAwesomeIconView(FontAwesomeIcon.EXCHANGE)
                            action {
                                selectedItem?.apply {
                                    confirm(
                                            header = "Return ${book.title}?",
                                            content = "Borrowed by ${person.name} <${person.email}>",
                                            actionFn = {
                                                controller.returnBook(this)
                                                controller.undoList.add(Action("Returned", this.copy(), "Nothing"))
                                                controller.redoList.setAll()
                                            }
                                    )
                                }
                            }
                        }
                        item("Show History") {
                            graphic = FontAwesomeIconView(FontAwesomeIcon.HISTORY)
                            action {
                                selectedItem?.apply {
                                    controller.sBook.item = book
                                    find<HistoryFragment>().openWindow()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
