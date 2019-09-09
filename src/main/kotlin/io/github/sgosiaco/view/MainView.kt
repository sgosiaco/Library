package io.github.sgosiaco.view

import io.github.sgosiaco.library.*
import javafx.geometry.Side
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*
import java.time.LocalDate
import kotlin.system.exitProcess

class MainView : View("Library") {
    private val controller: MyController by inject()
    private var search = ""

    override fun onDock() {
        currentStage?.isMaximized = true
    }

    private fun undo(act: Action) {
        with(act) {
            if(obj is Book) {
                val index = controller.bookList.indexOf(newObj)
                when(action) {
                    "Added" -> controller.bookList.remove(obj as Book)
                    "Edited" -> controller.bookList[index] = obj as Book
                    "Deleted" -> controller.bookList.add(obj as Book)
                    else -> println("Error with undo")
                }
            }
            else if(obj is Person) {
                val index = controller.peopleList.indexOf(newObj)
                when(action) {
                    "Added" -> controller.peopleList.remove(obj as Person)
                    "Edited" -> controller.peopleList[index] = obj as Person
                    "Deleted" -> controller.peopleList.add(obj as Person)
                    else -> println("Error with undo")
                }
            }
            else {
                when(action) {
                    "Checkout" -> {
                        returnBook(obj as Checkout)
                        controller.checkedList.remove(obj as Checkout)
                    }
                    "Returned" -> {
                        checkBook(obj as Checkout)
                    }
                }
            }
            controller.undoList.remove(act)
            controller.redoList.add(act)
        }
    }

    private fun redo(act: Action) {
        with(act) {
            if(obj is Book) {
                val index = controller.bookList.indexOf(obj as Book)
                when(action) {
                    "Added" -> controller.bookList.add(obj as Book)
                    "Edited" -> controller.bookList[index] = newObj as Book
                    "Deleted" -> controller.bookList.remove(obj as Book)
                }
            }
            else if (obj is Person) {
                val index = controller.peopleList.indexOf(obj as Person)
                when(action) {
                    "Added" -> controller.peopleList.add(obj as Person)
                    "Edited" -> controller.peopleList[index] = newObj as Person
                    "Deleted" -> controller.peopleList.remove(obj as Person)
                }
            }
            else {
                when(action) {
                    "Checkout" -> {
                        checkBook(obj as Checkout)
                    }
                    "Returned" -> {
                        returnBook(obj as Checkout)
                    }
                }
            }
            controller.redoList.remove(act)
            controller.undoList.add(act)
        }
    }

    private fun checkBook(checkout: Checkout) {
        with(checkout) {
            var index = controller.checkedList.indexOf(this)
            returned = false
            rDate = null
            if(index == -1) {
                controller.checkedList.add(this)
            }
            else {
                controller.checkedList[index] = this
            }
            index = controller.bookList.indexOf(book)
            book.checkedout = true
            controller.bookList[index] = book

            index = controller.peopleList.indexOf(person)
            person.cNum += 1
            controller.peopleList[index] = person


        }
    }

    private fun returnBook(checkout: Checkout) {
        with(checkout) {
            var index = controller.checkedList.indexOf(this)
            returned = true
            rDate = LocalDate.now()
            controller.checkedList[index] = this

            index = controller.bookList.indexOf(book)
            book.checkedout = false
            controller.bookList[index] = book

            index = controller.peopleList.indexOf(person)
            person.cNum -= 1
            controller.peopleList[index] = person


        }
    }

    override val root = vbox {
        menubar {
            menu("File") {
                menu("Open") {
                    item("Open Book List").action { controller.openDialog("book") }
                    item("Open Person List").action { controller.openDialog("person") }
                    item("Open Checkout List").action { controller.openDialog("checked") }
                }
                menu("Save") {
                    item("Save Book List").action {
                        confirm(
                                header = "Save the book list?",
                                actionFn = { controller.saveBooks() }
                        )
                    }
                    item("Save People List").action {
                        confirm(
                                header = "Save the people list?",
                                actionFn = { controller.savePeople() }
                        )
                    }
                    item("Save Checkout List").action {
                        confirm(
                                header = "Save the checkout list?",
                                actionFn = { controller.saveChecked() }
                        )
                    }
                    item("Save all", "Shortcut+S").action {
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
                item("Quit", "Shortcut+Q").action {
                    confirm(
                            header = "Are you sure you want to quit?",
                            actionFn = { exitProcess(1) }
                    )
                }
            }
            menu("Add") {
                item("Add New Book").action { find<AddBookFragment>().openModal() }
                item("Add New Person").action { find<AddPersonFragment>().openModal() }
            }
            menu("Edit") {
                item("Duplicate Book").action {
                    if(controller.focus == "Books") {
                        //val index = controller.bookList.indexOf(controller.sBook.item)
                        controller.bookList.add(controller.sBook.item.copy())
                    }
                }
                item("Modify Selected", "Shortcut+E").action {
                    if(controller.focus == "Books") {
                        find<EditBookFragment>().openModal()
                    }
                    else {
                        find<EditPersonFragment>().openModal()
                    }
                }
                item("Show history", "Shortcut+H").action { find<HistoryFragment>().openWindow() }
                item("Redo", "Shortcut+Shift+Z").action {
                    if(controller.redoList.isNotEmpty()) {
                        redo(controller.redoList.last())
                    }
                }
                item("Undo", "Shortcut+Z").action {
                    if(controller.undoList.isNotEmpty()) {
                        undo(controller.undoList.last())
                    }
                }
            }
        }
        textfield(search)
        drawer(side = Side.LEFT, multiselect = true) {
            vgrow = Priority.ALWAYS
            item("Books/People", showHeader = false) {
                expanded = true
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab("Books") {
                        val data = SortedFilteredList(controller.bookList)
                        data.predicate = { !it.checkedout }
                        tableview(data) {
                            focusedProperty().onChange {
                                controller.focus = "Books"
                            }
                            bindSelected(controller.sBook)
                            vgrow = Priority.ALWAYS
                            readonlyColumn("Title", Book::title)
                            readonlyColumn("Author", Book::author)
                            readonlyColumn("Publisher", Book::pub)
                            readonlyColumn("Year", Book::year)
                            columnResizePolicy = SmartResize.POLICY

                            contextmenu {
                                item("Add book").action { find<AddBookFragment>().openModal() }
                                item("Edit book").action {
                                    selectedItem?.apply {
                                        find<EditBookFragment>().openModal() //use openWindow to allow selecting dif book while window open
                                    }
                                }
                                item("Delete book").action {
                                    selectedItem?.apply {
                                        confirm(
                                                header = "Delete $title?",
                                                actionFn = {
                                                    controller.undoList.add(Action("Deleted", selectedItem as Any, "Nothing"))
                                                    controller.bookList.remove(selectedItem)
                                                }
                                        )
                                    }
                                }
                                item("Checkout").action {
                                    selectedItem?.apply {
                                        find<CheckoutFragment>().openModal()
                                    }
                                }
                                item("Show History").action {
                                    selectedItem?.apply {
                                        find<HistoryFragment>().openWindow()
                                    }
                                }
                            }

                        }
                    }
                    tab("People") {
                        tableview(controller.peopleList) {
                            focusedProperty().onChange {
                                controller.focus = "People"
                            }
                            bindSelected(controller.sPerson)
                            vgrow = Priority.ALWAYS
                            readonlyColumn("Name", Person::name)
                            readonlyColumn("Email", Person::email)
                            readonlyColumn("Phone number", Person::phone)
                            readonlyColumn("Affiliation", Person::aff)
                            columnResizePolicy = SmartResize.POLICY

                            contextmenu {
                                item("Add person").action { find<AddPersonFragment>().openModal() }
                                item("Edit person").action {
                                    selectedItem?.apply {
                                        find<EditPersonFragment>().openModal()
                                    }
                                }
                                item("Delete person").action {
                                    selectedItem?.apply {
                                        confirm(
                                                header = "Delete $name?",
                                                actionFn = {
                                                    controller.undoList.add(Action("Deleted", selectedItem as Any, "Nothing"))
                                                    controller.peopleList.remove(selectedItem)
                                                }
                                        )
                                    }
                                }
                                item("Show History").action {
                                    selectedItem?.apply {
                                        find<HistoryFragment>().openWindow()
                                    }
                                }
                            }
                        }
                    }
                }

            }
            item("Checked Out/Actions", showHeader = false) {
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab("Checked Out") {
                        val data = SortedFilteredList(controller.peopleList)
                        data.predicate = { it.cNum > 0 }
                        tableview(data) {
                            focusedProperty().onChange {
                                controller.focus = "People"
                            }
                            bindSelected(controller.sPerson)
                            vgrow = Priority.ALWAYS
                            readonlyColumn("Name", Person::name)
                            readonlyColumn("Email", Person::email)
                            readonlyColumn("Phone number", Person::phone)
                            columnResizePolicy = SmartResize.POLICY
                            contextmenu {
                                item("Return All").action {
                                    selectedItem?.apply {
                                        confirm(
                                                header = "Return all books borrowed by $name?",
                                                actionFn = {
                                                    controller.checkedList.filter { !it.returned && it.person == this }.forEach {
                                                        returnBook(it)
                                                        controller.undoList.add(Action("Returned", it, "Nothing"))
                                                    }
                                                }
                                        )
                                    }
                                }
                                item("Show History").action {
                                    selectedItem?.apply {
                                        find<HistoryFragment>().openWindow()
                                    }
                                }
                            }
                            rowExpander(expandOnDoubleClick = true) { selected ->
                                paddingLeft = expanderColumn.width
                                val data = SortedFilteredList(controller.checkedList)
                                data.predicate = { it.person == selected && !it.returned }
                                tableview(data) {
                                    vgrow = Priority.ALWAYS
                                    readonlyColumn("Book", Checkout::book)
                                    readonlyColumn("Checked Out", Checkout::cDate)
                                    readonlyColumn("Due", Checkout::dDate).cellFormat {
                                        text = it.toString()
                                        style {
                                            if (it.isBefore(LocalDate.now())) {
                                                backgroundColor += c("#8b0000")
                                                textFill = Color.WHITE
                                            } else {
                                                backgroundColor += Color.WHITE
                                                textFill = Color.BLACK
                                            }
                                        }
                                    }
                                    contextmenu {
                                        item("Return").action {
                                            selectedItem?.apply {
                                                confirm(
                                                        header = "Return ${book.title}?",
                                                        content = "Borrowed by ${person.name} <${person.email}>",
                                                        actionFn = {
                                                            returnBook(this)
                                                            controller.undoList.add(Action("Returned", this, "Nothing"))
                                                        }
                                                )
                                            }
                                        }
                                        item("Show History").action {
                                            selectedItem?.apply {
                                                controller.sBook.item = book
                                                find<HistoryFragment>().openWindow()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //tab<HistoryFragment>()
                    tab("Actions") {
                        hbox {
                            tableview(controller.undoList) {
                                readonlyColumn("Action", Action::action)
                                readonlyColumn("Object", Action::obj).prefWidth(200.0)
                                readonlyColumn("New Object", Action::newObj).prefWidth(200.0)
                                contextmenu {
                                    item("Undo").action {
                                        selectedItem?.apply {
                                            undo(this)
                                        }
                                    }
                                }
                            }
                            tableview(controller.redoList) {
                                readonlyColumn("Action", Action::action)
                                readonlyColumn("Object", Action::obj).prefWidth(200.0)
                                readonlyColumn("New Object", Action::newObj).prefWidth(200.0)
                                contextmenu {
                                    item("Redo").action {
                                        selectedItem?.apply {
                                            redo(this)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}