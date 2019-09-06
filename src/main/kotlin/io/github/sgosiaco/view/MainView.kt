package io.github.sgosiaco.view

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.time.LocalDate
import kotlin.system.exitProcess

data class Book (
        @SerializedName("checkedout") var checkedout: Boolean,
        @SerializedName("author") var author : String,
        @SerializedName("year") var year : Int,
        @SerializedName("pub") var pub : String,
        @SerializedName("title") var title : String
)

data class Person (
        @SerializedName("name") var name : String,
        @SerializedName("email") var email : String,
        @SerializedName("phone") var phone : Int,
        @SerializedName("aff") var aff : String
)

data class Checkout(val person: Person, val book: Book, val cDate: LocalDate, val dDate: LocalDate, var rDate: LocalDate?, var returned: Boolean)

class MyController: Controller() {
    private var bookjson = File("books.json").readText(Charsets.UTF_8)
    var bookList: ObservableList<Book> = FXCollections.observableArrayList(Gson().fromJson(bookjson, Array<Book>::class.java).toList())

    private var peoplejson = File("people.json").readText(Charsets.UTF_8)
    var peopleList: ObservableList<Person> = FXCollections.observableArrayList(Gson().fromJson(peoplejson, Array<Person>::class.java).toList())

    private var checkedjson = File("checked.json").readText(Charsets.UTF_8)
    var checkedList: ObservableList<Checkout> = FXCollections.observableArrayList(Gson().fromJson(checkedjson, Array<Checkout>::class.java).toList())

    fun savePeople() {
        File("people.json").writeText(Gson().toJson(peopleList))
    }

    fun saveBooks() {
        File("books.json").writeText(Gson().toJson(bookList))
    }

    fun saveChecked() {
        File("checked.json").writeText(Gson().toJson(checkedList))
    }

    fun openDialog(type: String) {
        val title = when(type) {
            "book" -> "Open book list"
            "person" -> "Open person list"
            "checked" -> "Open checked list"
            else -> "Error"
        }
        val chosenFile = chooseFile(
                title = title,
                filters = arrayOf(FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"))
        )
        if(chosenFile.size == 1) {
            with(chosenFile[0]) {
                val file = File(path)
                when(type) {
                    "book" -> {
                        bookjson = file.readText(Charsets.UTF_8)
                        bookList.setAll(FXCollections.observableArrayList(Gson().fromJson(bookjson, Array<Book>::class.java).toList()))
                    }
                    "person" -> {
                        peoplejson = file.readText(Charsets.UTF_8)
                        peopleList.setAll(FXCollections.observableArrayList(Gson().fromJson(peoplejson, Array<Person>::class.java).toList()))
                    }
                    "checked" -> {
                        checkedjson = file.readText(Charsets.UTF_8)
                        checkedList.setAll(FXCollections.observableArrayList(Gson().fromJson(checkedjson, Array<Checkout>::class.java).toList()))
                    }
                    else -> {
                        println("error")
                    }
                }
            }
        }
        else {

        }
    }
}

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