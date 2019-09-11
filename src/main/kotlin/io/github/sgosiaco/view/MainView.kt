package io.github.sgosiaco.view

import io.github.sgosiaco.library.*
import javafx.geometry.Side
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import tornadofx.*
import kotlin.system.exitProcess

class MainView : View("Library") {
    private val controller: MyController by inject()

    override fun onDock() {
        currentStage?.isMaximized = true
    }

    override val root = vbox {
        menubar {
            menu("File") {
                menu("Open") {
                    item("Open Book List").action { controller.openDialog("book") }
                    item("Open Person List").action { controller.openDialog("person") }
                    item("Open Checkout List").action { controller.openDialog("checked") }
                }
                menu("Export") {
                    item("Export Book List to CSV").action {
                        confirm(
                                header = "Export the book list to CSV?",
                                actionFn = { controller.exportBookCSV() }
                        )
                    }
                    item("Export People List to CSV").action {
                        confirm(
                                header = "Export the people list to CSV?",
                                actionFn = { controller.exportPeopleCSV() }
                        )
                    }
                    item("Export Checked List to CSV").action {
                        confirm(
                                header = "Export the checked list to CSV?",
                                actionFn = { controller.exportCheckedCSV() }
                        )
                    }
                }
                menu("Save") {
                    item("Save Book List").action {
                        confirm(
                                header = "Save the book list?",
                                actionFn = { controller.saveBooks() }
                        )
                    }
                    item("Save People List").action {
                        confirm(
                                header = "Save the people list?",
                                actionFn = { controller.savePeople() }
                        )
                    }
                    item("Save Checkout List").action {
                        confirm(
                                header = "Save the checkout list?",
                                actionFn = { controller.saveChecked() }
                        )
                    }
                    item("Save all", "Shortcut+S").action {
                        confirm(
                                header = "Save all lists?",
                                actionFn = {
                                    controller.saveBooks()
                                    controller.savePeople()
                                    controller.saveChecked()
                                }
                        )
                    }
                }
                item("Quit", "Shortcut+Q").action {
                    confirm(
                            header = "Are you sure you want to quit?",
                            actionFn = { exitProcess(1) }
                    )
                }
            }
            menu("Add") {
                item("Add New Book").action { find<AddBookFragment>().openModal() }
                item("Add New Person").action { find<AddPersonFragment>().openModal() }
            }
            menu("Edit") {
                item("Duplicate Book").action {
                    if(controller.focus == "Books") {
                        val book = controller.sBook.item.copy()
                        controller.checkDupeBook(book)
                        controller.bookList.add(book)
                    }
                }
                item("Modify Selected", "Shortcut+E").action {
                    if(controller.focus == "Books") {
                        find<EditBookFragment>().openModal()
                    }
                    else {
                        find<EditPersonFragment>().openModal()
                    }
                }
                item("Show history", "Shortcut+H").action { find<HistoryFragment>().openWindow() }
                item("Redo", "Shortcut+Shift+Z").action {
                    if(controller.redoList.isNotEmpty()) {
                        controller.redo(controller.redoList.last())
                    }
                }
                item("Undo", "Shortcut+Z").action {
                    if(controller.undoList.isNotEmpty()) {
                        controller.undo(controller.undoList.last())
                    }
                }
            }
        }
        drawer(side = Side.LEFT, multiselect = true) {
            vgrow = Priority.ALWAYS
            item("Books/People", showHeader = false) {
                expanded = true
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab(BookView::class)
                    tab(CheckedBookView::class)
                    tab("Log") {
                        tableview(controller.undoList) {
                            readonlyColumn("Action", Action::action)
                            readonlyColumn("Object", Action::obj).prefWidth(1000.0)
                            readonlyColumn("New Object", Action::newObj).prefWidth(200.0)
                        }
                    }
                }

            }
            item("Checked Out/Log", showHeader = false) {
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab(PeopleView::class)
                    tab(CheckedPersonView::class)
                    /*
                    tab(CheckedPersonView::class)
                    tab(CheckedBookView::class)
                    tab("Log") {
                        tableview(controller.undoList) {
                            readonlyColumn("Action", Action::action)
                            readonlyColumn("Object", Action::obj).prefWidth(1000.0)
                            readonlyColumn("New Object", Action::newObj).prefWidth(200.0)
                        }
                    }

                     */
                }
            }
        }
    }
}