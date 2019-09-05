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
data class Book(val title: String, val author: String, val publisher: String, val year: Int, var history: MutableList<Checkout> = mutableListOf())
data class Person(val name: String, val email: String, var history : MutableList<Checkout> = mutableListOf())
data class Checkout(val person: Person, val book: Book, val wDate: String, val rDate: String)

class MyController: Controller() {
    private val json = File("books.json").readText(Charsets.UTF_8) //System.getProperty("user.dir")+"""\books.json"""
    val books: ObservableList<BookImport> = FXCollections.observableArrayList(Gson().fromJson(json, Array<BookImport>::class.java).toList())
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
                tableview(controller.books) {
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
                                println("Loaning $title")
                                find<CheckoutFragment>().openWindow()
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
                tableview(controller.books) {
                    vboxConstraints {
                        vGrow = Priority.ALWAYS
                    }
                    readonlyColumn("Name", BookImport::title)
                    readonlyColumn("Email", BookImport::author)
                    readonlyColumn("Phone number", BookImport::pub)
                    readonlyColumn("Department", BookImport::year)
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

    private fun test() {
        val sean = Person("Sean Gosiaco", "sgosiaco@me.com")
        val ryan = Person("Ryan Gosiaco", "rgosiaco@me.com")
        val tobira = Book("Tobira", "Ito", "JPN", 2011)
        val took = Checkout(sean, tobira, "Today", "Tomorrow")
        val took2 = Checkout(ryan, tobira, "9/5/2019", "9/24/2019")
        sean.history.add(took)
        tobira.history.add(took)
        tobira.history.add(took2)

        println(sean.name)

        println("----------------------")
        for(item in tobira.history)
        {
            println(item.book.author)
            println(item.person.name)
            println(item.wDate)
            println(item.rDate)
            println("----------------------")
        }

    }
}