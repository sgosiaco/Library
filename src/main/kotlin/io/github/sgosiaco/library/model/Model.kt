package io.github.sgosiaco.library.model

import com.google.gson.annotations.SerializedName
import tornadofx.*
import java.time.LocalDate
import java.time.LocalDateTime

data class Book(
        @SerializedName("dupe") var dupe: Int = 0,
        @SerializedName("checkedout") var checkedout: Boolean = false,
        @SerializedName("author") var author: String = "",
        @SerializedName("year") var year: Int = -1,
        @SerializedName("pub") var pub: String = "",
        @SerializedName("title") var title: String = ""
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

data class Person(
        @SerializedName("name") var name: String = "",
        @SerializedName("email") var email: String = "",
        @SerializedName("phone") var phone: Long = -1,
        @SerializedName("aff") var aff: String = "",
        @SerializedName("cNum") var cNum: Int = 0
) {
    override fun toString(): String = "$name <$email>"
    fun toCSV(): String = "$name <$email>"
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
        var person: Person = Person(),
        var book: Book = Book(),
        var cDate: LocalDate = LocalDate.now(),
        var dDate: LocalDate = LocalDate.now().plusWeeks(2),
        var rDate: LocalDate? = null,
        var returned: Boolean = false
) {
    fun deepCopy(person: Person = this.person.copy(), book: Book = this.book.copy(), cDate: LocalDate = this.cDate, dDate: LocalDate = this.dDate, rDate: LocalDate? = this.rDate, returned: Boolean = this.returned) = Checkout(person, book, cDate, dDate, rDate, returned)
    override fun toString(): String = """"${book.title}" checked out to ${person.name}"""
    fun containsString(query: String) = person.containsString(query) ||
            book.containsString(query) ||
            cDate.toString().contains(query) ||
            dDate.toString().contains(query)
}

class SelectedCheckout : ItemViewModel<Checkout>() {
    val person = bind(Checkout::person)
    val book = bind(Checkout::book)
    val cDate = bind(Checkout::cDate)
    val dDate = bind(Checkout::dDate)
    var rDate = bind(Checkout::rDate)
    var returned = bind(Checkout::returned)
}

data class Action(var action: String, var obj: Any, var newObj: Any, val timestamp: LocalDateTime = LocalDateTime.now())