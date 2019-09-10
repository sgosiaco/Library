package io.github.sgosiaco.library

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Book (
        @SerializedName("dupe") var dupe: Int = 0,
        @SerializedName("checkedout") var checkedout: Boolean = false,
        @SerializedName("author") var author : String = "",
        @SerializedName("year") var year : Int = -1,
        @SerializedName("pub") var pub : String = "",
        @SerializedName("title") var title : String = ""
) {
    override fun toString(): String = title
    fun toCSV(): String = """"$title","$author","$pub","$year""""
    fun toCSVChecked(): String = """"$title","$author","$pub","$year","$checkedout""""
}

class SelectedBook : ItemViewModel<Book>() {
    val checkedout = bind(Book::checkedout)
    val author = bind(Book::author)
    val year = bind(Book::year)
    val pub = bind(Book::pub)
    val title = bind(Book::title)

}

data class Person (
        @SerializedName("name") var name : String = "",
        @SerializedName("email") var email : String = "",
        @SerializedName("phone") var phone : Int = -1,
        @SerializedName("aff") var aff : String = "",
        @SerializedName("cNum") var cNum : Int = 0
) {
    override fun toString(): String = "$name <$email>"
    fun toCSV(): String = "$name <$email>" //no safety quote for comma b/c there should be no commas
    fun toCSVAll(): String = "$name, $email, $phone, $aff, $cNum"
}

class SelectedPerson : ItemViewModel<Person>() {
    val name = bind(Person::name)
    val email = bind(Person::email)
    val phone = bind(Person::phone)
    val aff = bind(Person::aff)
    val cNum = bind(Person::cNum)
}

data class Checkout(
        val person: Person = Person(),
        val book: Book = Book(),
        val cDate: LocalDate = LocalDate.now(),
        val dDate: LocalDate = LocalDate.now().plusWeeks(2),
        var rDate: LocalDate? = null,
        var returned: Boolean = false
) {
    override fun toString(): String = """"${book.title}" checked out to ${person.name}"""
}

data class Action(var action: String, var obj: Any, var newObj: Any)

class MyController: Controller() {
    val sBook = SelectedBook()
    val sPerson = SelectedPerson()
    var focus = ""

    private var bookjson = File("books.json").readText(Charsets.UTF_8)
    var bookList: ObservableList<Book> = FXCollections.observableArrayList(Gson().fromJson(bookjson, Array<Book>::class.java).toList())

    private var peoplejson = File("people.json").readText(Charsets.UTF_8)
    var peopleList: ObservableList<Person> = FXCollections.observableArrayList(Gson().fromJson(peoplejson, Array<Person>::class.java).toList())

    private var checkedjson = File("checked.json").readText(Charsets.UTF_8)
    var checkedList: ObservableList<Checkout> = FXCollections.observableArrayList(Gson().fromJson(checkedjson, Array<Checkout>::class.java).toList())

    var undoList: ObservableList<Action> = FXCollections.observableArrayList()
    var redoList: ObservableList<Action> = FXCollections.observableArrayList()

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
        peopleList.forEach {
            csv += "${it.toCSVAll()}\n"
        }
        File("people.csv").writeText(csv)
    }

    fun exportBookCSV() {
        var csv = "Title, Author(s), Publisher(s), Year, Checked Out\n"
        bookList.forEach {
            csv += "${it.toCSVChecked()}\n"
        }
        File("books.csv").writeText(csv)
    }

    fun exportCheckedCSV() {
        val dateFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy")
        var csv = "Title, Author(s), Publisher(s), Year, Person, Checked Out, Due\n"
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
            if(index == -1) {
                checkedList.add(this)
            }
            else {
                checkedList[index] = this
            }
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
            //println("${bookList.indexOf(book)} ${book.title} ${book.dupe}")
            book.dupe += 1
            /*
            if(book.dupe > 5) {
                exitProcess(1)
            }
            */
        }
    }
}