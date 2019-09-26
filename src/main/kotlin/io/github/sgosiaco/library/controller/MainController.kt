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
            var index = bookList.indexOf(book)
            bookList[index] = book.apply { checkedout = true }.copy()

            index = findPerson(person)
            peopleList[index] = peopleList[index].apply { cNum += 1 }

            index = checkedList.indexOf(this)
            returned = false
            rDate = null
            if(index == -1) checkedList.add(this.deepCopy()) else checkedList[index] = this.deepCopy()
        }
    }

    fun returnBook(checkout: Checkout) {
        with(checkout) {
            val checkoutIndex = checkedList.indexOf(this)

            var index = bookList.indexOf(book)
            bookList[index] = book.apply { checkedout = false }.copy()

            index = findPerson(person)
            peopleList[index] = peopleList[index].apply { cNum -= 1 }

            returned = true
            rDate = LocalDate.now()
            checkedList[checkoutIndex] = this.deepCopy()
        }
    }

    private fun editCheckout(old: Checkout, new: Checkout) {
        val index = checkedList.indexOf(new)
        var personIndex = findPerson(new.person)
        peopleList[personIndex] = peopleList[personIndex].apply { cNum -= 1 }

        personIndex = findPerson(old.person)
        peopleList[personIndex] = peopleList[personIndex].apply { cNum += 1}

        checkedList[index] = old.deepCopy()
    }

    fun checkDupeBook(book: Book) {
        while(book in bookList) {
            book.dupe += 1
        }
    }

    fun checkDupeEmail(email: String, person: Person?) = peopleList.any { it != person && it.email == email }

    fun findPerson(person: Person) = peopleList.indexOf(peopleList.find { it.email == person.email })

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
                    val index = findPerson(newObj as Person)
                    when(action) {
                        "Added" -> peopleList.remove(obj as Person)
                        "Edited" -> peopleList[index] = obj as Person
                        "Deleted" -> peopleList.add(obj as Person)
                        else -> println("Error with undo")
                    }
                }
                else -> {
                    when(action) {
                        "Edited" -> editCheckout(obj as Checkout, newObj as Checkout)
                        "Checkout" -> {
                            returnBook(obj as Checkout)
                            checkedList.remove(obj as Checkout)
                        }
                        "Returned" -> checkBook(obj as Checkout)
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
                    val index = findPerson(obj as Person)
                    when(action) {
                        "Added" -> peopleList.add(obj as Person)
                        "Edited" -> peopleList[index] = newObj as Person
                        "Deleted" -> peopleList.remove(obj as Person)
                    }
                }
                else -> {
                    when(action) {
                        "Edited" -> editCheckout(newObj as Checkout, obj as Checkout)
                        "Checkout" -> checkBook(obj as Checkout)
                        "Returned" -> returnBook(obj as Checkout)
                    }
                }
            }
            redoList.remove(act)
            undoList.add(act)
        }
    }

    fun draftAll(person: Person) {
        val body = "Hello. The following library items are checked out in your name.\n\n"
        var data = ""
        val footer = "Please contact us with any questions by emailing idaas@pomona.edu. " +
                "Library hours are Mondays-Fridays from 8:00am-2:30pm, with a lunch break during the noon hour.\n" +
                "\nThanks!\n" +
                "Madeline"

        checkedList.filter { !it.returned && it.person == person }.forEach {
            data += "Title: ${it.book.title}\n" +
                    "Author: ${it.book.author}\n" +
                    "Checkout Date: ${it.cDate.format(dateFormat)}\n" +
                    "Due Date: ${if(it.dDate.isBefore(LocalDate.now())) "Overdue, please return immediately. (Original due date was ${it.dDate.format(dateFormat)})" else it.dDate.format(dateFormat)}\n" +
                    "\n"
        }
        clipboard.putString(body + data + footer)
    }

    fun draftTomorrow(person:  Person) {
        val body = "Hello. The following library items are due tomorrow. " +
                "Please return these materials to the IDAAS library in Lincoln 1119, Pomona.\n\n"
        var data = ""
        val footer = "Please contact us with any questions by emailing idaas@pomona.edu. " +
                "Library hours are Mondays-Fridays from 8:00am-2:30pm, with a lunch break during the noon hour.\n" +
                "\nThanks!\n" +
                "Madeline"

        checkedList.filter { !it.returned && it.person == person && it.dDate.isEqual(LocalDate.now().plusDays(1)) }.forEach {
            data += "Title: ${it.book.title}\n" +
                    "Author: ${it.book.author}\n\n"
        }
        clipboard.putString(body + data + footer)
    }
}