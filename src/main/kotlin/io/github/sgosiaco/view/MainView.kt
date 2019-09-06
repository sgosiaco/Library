package io.github.sgosiaco.view

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.io.File
import java.time.LocalDate
import kotlin.system.exitProcess

data class Book (
        @SerializedName("checkedout") var checkedout: Boolean,
        @SerializedName("author") val author : String,
        @SerializedName("year") val year : Int,
        @SerializedName("pub") val pub : String,
        @SerializedName("title") val title : String
)

data class Person (
        @SerializedName("name") val name : String,
        @SerializedName("email") val email : String,
        @SerializedName("phone") val phone : Int,
        @SerializedName("aff") val aff : String
)

data class Checkout(val person: Person, val book: Book, val cDate: LocalDate, val dDate: LocalDate, var rDate: LocalDate?, var returned: Boolean)

class MyController: Controller() {
    private val bookjson = File("books.json").readText(Charsets.UTF_8)
    val bookList: ObservableList<Book> = FXCollections.observableArrayList(Gson().fromJson(bookjson, Array<Book>::class.java).toList())

    private val peoplejson = File("people.json").readText(Charsets.UTF_8)
    val personList: ObservableList<Person> = FXCollections.observableArrayList(Gson().fromJson(peoplejson, Array<Person>::class.java).toList())

    private val checkedjson = File("checked.json").readText(Charsets.UTF_8)
    val checkedList: ObservableList<Checkout> = FXCollections.observableArrayList(Gson().fromJson(checkedjson, Array<Checkout>::class.java).toList())

    fun savePeople() {
        File("people.json").writeText(Gson().toJson(personList))
    }

    fun saveBooks() {
        File("books.json").writeText(Gson().toJson(bookList))
    }

    fun saveChecked() {
        File("checked.json").writeText(Gson().toJson(checkedList))
    }
}

class MainView : View("Library") {
    private val controller: MyController by inject()
    override val root = vbox {
        menubar {
            menu("File") {
                item("Open") {
                    action { println("Open")}
                }
                item("Save", "Shortcut+S") {
                    action {
                        println("Save")
                        controller.saveBooks()
                        controller.savePeople()
                        controller.saveChecked()
                    }
                }
                item("Quit", "Shortcut+Q") {
                    action {
                        println("Quit")
                        exitProcess(1)
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
                    //readonlyColumn("Checked Out", Book::checkedout)
                    columnResizePolicy = SmartResize.POLICY

                    contextmenu {
                        item("Add book").action {
                            find<AddBookFragment>().openModal()
                        }
                        item("Checkout").action {
                            selectedItem?.apply {
                                println("Loaning $title $author $pub $year")
                                find<CheckoutFragment>(mapOf(CheckoutFragment::book to this)).openModal()
                            }
                        }
                        item("Check History").action {
                            selectedItem?.apply {
                                println("Checking $title")
                                find<HistoryFragment>().openWindow()
                            }
                        }
                    }

                }
            }
            item("People") {
                tableview(controller.personList) {
                    vboxConstraints {
                        vGrow = Priority.ALWAYS
                    }
                    readonlyColumn("Name", Person::name)
                    readonlyColumn("Email", Person::email)
                    readonlyColumn("Phone number", Person::phone)
                    readonlyColumn("Affiliation", Person::aff)
                    columnResizePolicy = SmartResize.POLICY

                    contextmenu {
                        item("Add").action {
                            find<AddPersonFragment>().openModal()
                        }
                        item("Check History").action {
                            selectedItem?.apply {
                                println("Checking $title")
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
                    //readonlyColumn("Title", Book::title)
                    //readonlyColumn("Author", Book::author)
                    //readonlyColumn("Publisher", Book::pub)
                    //readonlyColumn("Year", Book::year)

                    //readonlyColumn("Name", Book::title)
                    //readonlyColumn("Email", Book::title)
                    //readonlyColumn("Phone number", Book::title)
                    //readonlyColumn("Affiliation", Book::title)
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
                                println("Returning ${book.title}")
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