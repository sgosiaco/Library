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
        @SerializedName("checkedout") var checkedout: Boolean,
        @SerializedName("author") var author : String,
        @SerializedName("year") var year : Int,
        @SerializedName("pub") var pub : String,
        @SerializedName("title") var title : String
) {
    override fun toString(): String = title
}

data class Person (
        @SerializedName("name") var name : String,
        @SerializedName("email") var email : String,
        @SerializedName("phone") var phone : Int,
        @SerializedName("aff") var aff : String,
        @SerializedName("cNum") var cNum : Int
) {
    override fun toString(): String = "$name <$email>"
}

data class Checkout(
        val person: Person,
        val book: Book,
        val cDate: LocalDate,
        val dDate: LocalDate,
        var rDate: LocalDate?,
        var returned: Boolean
)

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
    }
}