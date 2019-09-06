package io.github.sgosiaco.view

import io.github.sgosiaco.library.Book
import io.github.sgosiaco.library.Checkout
import io.github.sgosiaco.library.MyController
import io.github.sgosiaco.library.Person
import javafx.geometry.Side
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.time.LocalDate
import kotlin.system.exitProcess

class MainView : View("Library") {
    private val controller: MyController by inject()
    override val root = vbox {
        menubar {
            menu("File") {
                menu("Open") {
                    item("Open book list") {
                        action {
                            controller.openDialog("book")
                        }
                    }
                    item("Open person list") {
                        action {
                            controller.openDialog("person")
                        }
                    }
                    item("Open checked list") {
                        action {
                            controller.openDialog("checked")
                        }
                    }
                }
                menu("Save") {
                    item("Save Books") {
                        action {
                            confirm(
                                    header = "Save the book list?",
                                    actionFn = {
                                        controller.saveBooks()
                                    }
                            )
                        }
                    }
                    item("Save People") {
                        action {
                            confirm(
                                    header = "Save the people list?",
                                    actionFn = {
                                        controller.savePeople()
                                    }
                            )
                        }
                    }
                    item("Save Checked Out") {
                        action {
                            confirm(
                                    header = "Save the checked out list?",
                                    actionFn = {
                                        controller.saveChecked()
                                    }
                            )
                        }
                    }
                    item("Save all", "Shortcut+S") {
                        action {
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
                }
                item("Quit", "Shortcut+Q") {
                    action {
                        confirm(
                                header = "Are you sure you want to quit?",
                                actionFn = {
                                    exitProcess(1)
                                }
                        )
                    }
                }
            }
            menu("Edit") {
                item("Add new book") {
                    action {
                        find<AddBookFragment>().openModal()
                    }
                }
                item("Add new person") {
                    action {
                        find<AddPersonFragment>().openModal()
                    }
                }
            }
        }
        drawer(side = Side.LEFT, multiselect = true) {

            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
            item("Books") {
                expanded = true
                val data = SortedFilteredList(controller.bookList)
                data.predicate = { !it.checkedout }
                tableview(data) {
                    vboxConstraints {
                        vGrow = Priority.ALWAYS
                    }
                    readonlyColumn("Title", Book::title)
                    readonlyColumn("Author", Book::author)
                    readonlyColumn("Publisher", Book::pub)
                    readonlyColumn("Year", Book::year)
                    columnResizePolicy = SmartResize.POLICY

                    contextmenu {
                        item("Add book").action {
                            find<AddBookFragment>().openModal()
                        }
                        item("Edit book"). action {
                            selectedItem?.apply {
                                find<EditBookFragment>(mapOf(EditBookFragment::book to this)).openModal()
                            }
                        }
                        item("Delete book"). action {
                            selectedItem?.apply {
                                confirm(
                                        header = "Delete $title?",
                                        actionFn = {
                                            controller.bookList.remove(selectedItem)
                                        }
                                )
                            }
                        }
                        item("Checkout").action {
                            selectedItem?.apply {
                                find<CheckoutFragment>(mapOf(CheckoutFragment::book to this)).openModal()
                            }
                        }
                        item("Check History").action {
                            selectedItem?.apply {
                                find<HistoryFragment>().openWindow()
                            }
                        }
                    }

                }
            }
            item("People") {
                tableview(controller.peopleList) {
                    vboxConstraints {
                        vGrow = Priority.ALWAYS
                    }
                    readonlyColumn("Name", Person::name)
                    readonlyColumn("Email", Person::email)
                    readonlyColumn("Phone number", Person::phone)
                    readonlyColumn("Affiliation", Person::aff)
                    columnResizePolicy = SmartResize.POLICY

                    contextmenu {
                        item("Add person").action {
                            find<AddPersonFragment>().openModal()
                        }
                        item("Check History").action {
                            selectedItem?.apply {
                                find<HistoryFragment>().openWindow()
                            }
                        }
                    }
                }
            }
            item("Checked Out") {
                val data = SortedFilteredList(controller.checkedList)
                data.predicate = { !it.returned }
                tableview(data) {//data
                    vboxConstraints {
                        vGrow = Priority.ALWAYS
                    }
                    readonlyColumn("Book", Checkout::book)
                    readonlyColumn("Person", Checkout::person)
                    readonlyColumn("Checked Out", Checkout::cDate)
                    readonlyColumn("Due", Checkout::dDate).cellFormat {
                        text = it.toString()
                        style {
                            if(it.isBefore(LocalDate.now())) {
                                backgroundColor += c("#8b0000")
                                textFill = Color.WHITE
                            }
                            else {
                                backgroundColor += Color.WHITE
                                textFill = Color.BLACK
                            }
                        }
                    }
                    columnResizePolicy = SmartResize.POLICY

                    contextmenu {
                        item("Return").action {
                            selectedItem?.apply {
                                var index = controller.bookList.indexOf(book)
                                book.checkedout = false
                                controller.bookList[index] = book

                                index = controller.checkedList.indexOf(selectedItem)
                                returned = true
                                rDate = LocalDate.now()
                                controller.checkedList[index] = selectedItem
                            }
                        }
                    }
                }
            }
        }
    }
}