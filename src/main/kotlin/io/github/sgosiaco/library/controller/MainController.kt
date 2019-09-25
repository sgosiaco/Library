package io.github.sgosiaco.library.controller

import com.google.gson.Gson
import io.github.sgosiaco.library.model.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.stage.FileChooser
import javafx.util.StringConverter
import tornadofx.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PeopleConverter: StringConverter<Person>() {
    override fun fromString(string: String) = Person()
    override fun toString(person: Person) = "${person.name} <${person.email}>"
}

class MainController: Controller() {
    val sBook = SelectedBook()
    val sPerson = SelectedPerson()
    val sCheckout = SelectedCheckout()
    var focus = ""
    val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")

    private var bookjson = File("books.json").readText(Charsets.UTF_8)
    var bookList: ObservableList<Book> = FXCollections.observableArrayList(Gson().fromJson(bookjson, Array<Book>::class.java).toList())
    val sfBookList = SortedFilteredList(bookList)

    private var peoplejson = File("people.json").readText(Charsets.UTF_8)
    var peopleList: ObservableList<Person> = FXCollections.observableArrayList(Gson().fromJson(peoplejson, Array<Person>::class.java).toList())
    val sfPeopleList = SortedFilteredList(peopleList)
    val sfCheckedPeopleList = SortedFilteredList(peopleList)

    private var checkedjson = File("checked.json").readText(Charsets.UTF_8)
    var checkedList: ObservableList<Checkout> = FXCollections.observableArrayList(Gson().fromJson(checkedjson, Array<Checkout>::class.java).toList())
    val sfCheckedList = SortedFilteredList(checkedList)

    var undoList: ObservableList<Action> = FXCollections.observableArrayList()
    var redoList: ObservableList<Action> = FXCollections.observableArrayList()

    init {
        sfCheckedPeopleList.predicate = { it.cNum > 0 }
        sfCheckedList.predicate = { !it.returned }
    }

    fun savePeople() {
        File("people.json").writeText(Gson().toJson(peopleList))
    }

    fun saveBooks() {
        File("books.json").writeText(Gson().toJson(bookList))
    }

    fun saveChecked() {
        File("checked.json").writeText(Gson().toJson(checkedList))
    }

    fun exportPeopleCSV() {
        var csv = "Name, Email, Phone Number, Affiliation, cNum\n"
        peopleList.forEach { csv += "${it.toCSVAll()}\n" }
        File("people.csv").writeText(csv)
    }

    fun exportBookCSV() {
        var csv = "Title, Author, Publisher, Year, Checked Out\n"
        bookList.forEach { csv += "${it.toCSVChecked()}\n" }
        File("books.csv").writeText(csv)
    }

    fun exportCheckedCSV() {
        var csv = "Title, Author, Publisher, Year, Person, Checked Out, Due\n"
        checkedList.forEach {
            csv += "${it.book.toCSV()}, ${it.person.toCSV()}, ${it.cDate.format(dateFormat)}, ${it.dDate.format(dateFormat)}\n"
        }
        File("checked.csv").writeText(csv)
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
    }

    fun checkBook(checkout: Checkout) {
        with(checkout) {
            var index = checkedList.indexOf(this)
            returned = false
            rDate = null
            if(index == -1) checkedList.add(this) else checkedList[index] = this
            index = bookList.indexOf(book)
            book.checkedout = true
            bookList[index] = book

            index = peopleList.indexOf(person)
            person.cNum += 1
            peopleList[index] = person
        }
    }

    fun returnBook(checkout: Checkout) {
        with(checkout) {
            var index = checkedList.indexOf(this)
            returned = true
            rDate = LocalDate.now()
            checkedList[index] = this

            index = bookList.indexOf(book)
            book.checkedout = false
            bookList[index] = book

            index = peopleList.indexOf(person)
            person.cNum -= 1
            peopleList[index] = person
        }
    }

    fun checkDupeBook(book: Book) {
        while(book in bookList) {
            book.dupe += 1
        }
    }

    fun undo(act: Action) {
        with(act) {
            when (obj) {
                is Book -> {
                    val index = bookList.indexOf(newObj)
                    when(action) {
                        "Added" -> bookList.remove(obj as Book)
                        "Edited" -> bookList[index] = obj as Book
                        "Deleted" -> bookList.add(obj as Book)
                        else -> println("Error with undo")
                    }
                }
                is Person -> {
                    val index = peopleList.indexOf(newObj)
                    when(action) {
                        "Added" -> peopleList.remove(obj as Person)
                        "Edited" -> peopleList[index] = obj as Person
                        "Deleted" -> peopleList.add(obj as Person)
                        else -> println("Error with undo")
                    }
                }
                else -> {
                    val index = checkedList.indexOf(newObj)
                    when(action) {
                        "Edited" -> {
                            var personIndex = peopleList.indexOf((newObj as Checkout).person)
                            (newObj as Checkout).person.cNum -= 1
                            peopleList[personIndex] = (newObj as Checkout).person
                            personIndex = peopleList.indexOf((obj as Checkout).person)
                            (obj as Checkout).person.cNum += 1
                            peopleList[personIndex] = (obj as Checkout).person
                            checkedList[index] = obj as Checkout
                        }
                        "Checkout" -> {
                            returnBook(obj as Checkout)
                            checkedList.remove(obj as Checkout)
                        }
                        "Returned" -> {
                            checkBook(obj as Checkout)
                        }
                    }
                }

            }
            undoList.remove(act)
            redoList.add(act)
        }
    }

    fun redo(act: Action) {
        with(act) {
            when (obj) {
                is Book -> {
                    val index = bookList.indexOf(obj as Book)
                    when(action) {
                        "Added" -> bookList.add(obj as Book)
                        "Edited" -> bookList[index] = newObj as Book
                        "Deleted" -> bookList.remove(obj as Book)
                    }
                }
                is Person -> {
                    val index = peopleList.indexOf(obj as Person)
                    when(action) {
                        "Added" -> peopleList.add(obj as Person)
                        "Edited" -> peopleList[index] = newObj as Person
                        "Deleted" -> peopleList.remove(obj as Person)
                    }
                }
                else -> {
                    val index = checkedList.indexOf(obj as Checkout)
                    when(action) {
                        "Edited" -> {
                            var personIndex = peopleList.indexOf((obj as Checkout).person)
                            (obj as Checkout).person.cNum -= 1
                            peopleList[personIndex] = (obj as Checkout).person
                            personIndex = peopleList.indexOf((newObj as Checkout).person)
                            (newObj as Checkout).person.cNum += 1
                            peopleList[personIndex] = (newObj as Checkout).person
                            checkedList[index] = newObj as Checkout
                        }
                        "Checkout" -> {
                            checkBook(obj as Checkout)
                        }
                        "Returned" -> {
                            returnBook(obj as Checkout)
                        }
                    }
                }
            }
            redoList.remove(act)
            undoList.add(act)
        }
    }
}