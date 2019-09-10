package io.github.sgosiaco.library

import com.google.gson.annotations.SerializedName
import tornadofx.*
import java.time.LocalDate

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
    fun containsString(query: String): Boolean = (title.contains(query, true) ||
            author.contains(query, true) ||
            pub.contains(query, true) ||
            year.toString().contains(query, true))
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
    fun containsString(query: String) = name.contains(query, true) ||
            email.contains(query, true) ||
            phone.toString().contains(query, true) ||
            aff.contains(query, true)
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
    fun containsString(query: String) = person.containsString(query) ||
            book.containsString(query) ||
            cDate.toString().contains(query) ||
            dDate.toString().contains(query)
}

data class Action(var action: String, var obj: Any, var newObj: Any)