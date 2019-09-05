package io.github.sgosiaco.view

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.github.sgosiaco.library.Styles
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import tornadofx.*
import tornadofx.Stylesheet.Companion.contextMenu
import tornadofx.Stylesheet.Companion.menuItem
import java.io.File
import kotlin.system.exitProcess

data class BookImport (
        @SerializedName("author") val author : String,
        @SerializedName("year") val year : Int,
        @SerializedName("pub") val pub : String,
        @SerializedName("title") val title : String
)

data class PeopleImport (
        @SerializedName("name") val name : String,
        @SerializedName("email") val email : String,
        @SerializedName("phone") val phone : Int,
        @SerializedName("aff") val aff : String
)

data class Book(val title: String, val author: String, val publisher: String, val year: Int, var history: MutableList<Checkout> = mutableListOf())
data class Person(val name: String, val email: String, var history : MutableList<Checkout> = mutableListOf())
data class Checkout(val person: Person, val book: Book, val wDate: String, val rDate: String)

class MyController: Controller() {
    private val bookjson = File("books.json").readText(Charsets.UTF_8) //System.getProperty("user.dir")+"""\books.json"""
    val bookList: ObservableList<BookImport> = FXCollections.observableArrayList(Gson().fromJson(bookjson, Array<BookImport>::class.java).toList())

    private val peoplejson = File("people.json").readText(Charsets.UTF_8)
    val peopleList: ObservableList<PeopleImport> = FXCollections.observableArrayList(Gson().fromJson(peoplejson, Array<PeopleImport>::class.java).toList())
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
                tableview(controller.bookList) {
                    vboxConstraints {
                        vGrow = Priority.ALWAYS
                    }
                    readonlyColumn("Title", BookImport::title)
                    readonlyColumn("Author", BookImport::author)
                    readonlyColumn("Publisher", BookImport::pub)
                    readonlyColumn("Year", BookImport::year)
                    //columnResizePolicy = SmartResize.POLICY

                    contextmenu {
                        item("Checkout").action {
                            selectedItem?.apply {
                                println("Loaning $title $author $pub $year")
                                val params = "items" to listOf(BookImport(author, year, pub, title)).observable()
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
                tableview(controller.peopleList) {
                    vboxConstraints {
                        vGrow = Priority.ALWAYS
                    }
                    readonlyColumn("Name", PeopleImport::name)
                    readonlyColumn("Email", PeopleImport::email)
                    readonlyColumn("Phone number", PeopleImport::phone)
                    readonlyColumn("Affiliation", PeopleImport::aff)
                    //columnResizePolicy = SmartResize.POLICY

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
        }
    }
}