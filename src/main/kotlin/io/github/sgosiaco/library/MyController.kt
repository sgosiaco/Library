package io.github.sgosiaco.library

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.time.LocalDate

data class Book (
        @SerializedName("checkedout") var checkedout: Boolean = false,
        @SerializedName("author") var author : String = "",
        @SerializedName("year") var year : Int = -1,
        @SerializedName("pub") var pub : String = "",
        @SerializedName("title") var title : String = ""
) {
    override fun toString(): String = title
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
}

class SelectedPerson : ItemViewModel<Person>() {
    val name = bind(Person::name)
    val email = bind(Person::email)
    val phone = bind(Person::phone)
    val aff = bind(Person::aff)
    val cNum = bind(Person::cNum)
}

data class Checkout(
        val person: Person,
        val book: Book,
        val cDate: LocalDate,
        val dDate: LocalDate,
        var rDate: LocalDate?,
        var returned: Boolean
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
}