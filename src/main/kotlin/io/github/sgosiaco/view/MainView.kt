package io.github.sgosiaco.view

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File
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

data class Checkout(val person: Person, val book: Book, val wDate: String, val rDate: String)

class MyController: Controller() {
    private val bookjson = File("books.json").readText(Charsets.UTF_8) //System.getProperty("user.dir")+"""\books.json"""
    val bookList: ObservableList<Book> = FXCollections.observableArrayList(Gson().fromJson(bookjson, Array<Book>::class.java).toList())

    private val peoplejson = File("people.json").readText(Charsets.UTF_8)
    val personList: ObservableList<Person> = FXCollections.observableArrayList(Gson().fromJson(peoplejson, Array<Person>::class.java).toList())
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
                    action { println("Save")}
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
                    action { println("Add")}
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
                        item("Checkout").action {
                            selectedItem?.apply {
                                println("Loaning $title $author $pub $year")

                                val params = "items" to listOf(controller.bookList[controller.bookList.indexOf(Book(checkedout, author, year, pub, title))]).observable()
                                find<CheckoutFragment>(params).openModal()
                                //openInternalWindow(CheckoutFragment::class)
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
                val data = SortedFilteredList(controller.bookList)
                data.predicate = { it.checkedout }
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
                        item("Return").action {
                            selectedItem?.apply {
                                println("Returning $title")
                                val index = controller.bookList.indexOf(Book(checkedout, author, year, pub, title))
                                controller.bookList[index] = Book(false, author, year, pub, title)
                            }
                        }
                    }
                }
            }
        }
    }
}