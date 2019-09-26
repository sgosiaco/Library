package io.github.sgosiaco.library.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.model.Book
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class BookView : View("Books") {
    private val controller: MainController by inject()
    private var search = SimpleStringProperty()
    private var filter = SimpleStringProperty()
    //private val allState = booleanBinding(filter) { value == "All"}
    //private val availableState = booleanBinding(filter) { value == "Available"}
    private val checkedState = booleanBinding(filter) { value == "Checked" }

    init {
        search.value = ""
        filter.value = "All"
    }

    override val root = vbox {
        hbox {
            stackpane {
                val searchBox = textfield(search) {
                    controller.sfBookList.filterWhen(textProperty()) { query, item ->
                        when (filter.value) {
                            "All" -> item.containsString(query)
                            "Available" -> !item.checkedout && item.containsString(query)
                            "Checked" -> item.checkedout && item.containsString(query)
                            else -> !item.checkedout && item.containsString(query)
                        }
                    }
                    promptText = "Search $title"
                }
                button("X") {
                    visibleWhen { searchBox.textProperty().isNotEmpty }
                    action {
                        searchBox.clear()
                    }
                    style {
                        fontWeight = FontWeight.BOLD
                        backgroundRadius += box(0.px)
                        backgroundColor += Color.TRANSPARENT
                        borderColor += box(Color.TRANSPARENT)
                        paddingLeft = 140
                    }
                }
            }
            togglegroup {
                togglebutton("All Books") {
                    action {
                        filter.value = "All"
                        controller.sfBookList.predicate = { it.title != "" && (if (search.value != "") it.containsString(search.value) else true) }
                    }
                }
                togglebutton("Available to Checkout") {
                    action {
                        filter.value = "Available"
                        controller.sfBookList.predicate = { !it.checkedout && (if (search.value != "") it.containsString(search.value) else true) }
                    }
                }
                togglebutton("Checked Out") {
                    action {
                        filter.value = "Checked"
                        controller.sfBookList.predicate = { it.checkedout && (if (search.value != "") it.containsString(search.value) else true) }
                    }
                }
            }
        }
        tableview(controller.sfBookList) {
            focusedProperty().onChange {
                controller.focus = "Books"
            }
            bindSelected(controller.sBook)
            vgrow = Priority.ALWAYS
            columnResizePolicy = SmartResize.POLICY
            controller.sfBookList.onChange {
                requestResize()
            }
            readonlyColumn("Title", Book::title) {
                value {
                    if (it.value.dupe > 0) "${it.value.title} (${it.value.dupe})" else it.value.title
                }
            }
            readonlyColumn("Author", Book::author)
            readonlyColumn("Publisher", Book::pub)
            readonlyColumn("Year", Book::year)


            contextmenu {
                item("Add book") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS_SQUARE)
                    action { find<AddBookFragment>().openModal() }
                    visibleWhen(!checkedState)
                }
                item("Edit book") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.EDIT)
                    action {
                        selectedItem?.apply {
                            if (checkedout) {
                                error("Can't edit a checked out book!")
                            } else {
                                find<EditBookFragment>().openModal()
                            }
                        }
                    }
                    visibleWhen(!checkedState)
                }
                item("Delete book") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.TRASH)
                    action {
                        selectedItem?.apply {
                            if (checkedout) {
                                error(header = "Can't delete a checked out book!")
                            } else {
                                confirm(
                                        header = "Delete $title?",
                                        actionFn = {
                                            runAsync {
                                                controller.bookList.remove(selectedItem)
                                                controller.undoList.add(Action("Deleted", (selectedItem as Book).copy(), "Nothing"))
                                                controller.redoList.setAll()
                                            }
                                        }
                                )
                            }
                        }
                    }
                    visibleWhen(!checkedState)
                }
                item("Checkout") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.BOOK)
                    action {
                        selectedItem?.apply {
                            if (checkedout) {
                                error(header = "Can't checkout an already checked out book!")
                            } else {
                                find<CheckoutFragment>().openModal()
                            }
                        }
                    }
                    visibleWhen(!checkedState)
                }
                item("Show History") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.HISTORY)
                    action { selectedItem?.apply { find<HistoryFragment>().openWindow() } }
                }
            }

        }
    }
}
