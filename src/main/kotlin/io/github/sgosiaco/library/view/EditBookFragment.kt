package io.github.sgosiaco.library.view

import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Action
import io.github.sgosiaco.library.model.Book
import javafx.util.converter.IntegerStringConverter
import tornadofx.*

class EditBookFragment : Fragment() {
    private val controller: MainController by inject()
    private val widthList = mutableListOf<Double>()

    override fun onDock() {
        currentStage?.width = widthList.max() ?: 0.0
    }

    override val root = form {
        title = """Editing ${controller.sBook.title.value}"""
        fieldset("Info") {
            field("Title:") {
                textfield(controller.sBook.title)
                widthList.add(text.length * 47.0)
            }
            field("Author(s):") {
                textfield(controller.sBook.author)
                widthList.add(text.length * 47.0)
            }
            field("Publisher(s):") {
                textfield(controller.sBook.pub)
                widthList.add(text.length * 47.0)
            }
            field("Year:") {
                textfield(controller.sBook.year, IntegerStringConverter())
                widthList.add(text.length * 47.0)
            }

        }
        hbox(10.0) {
            button("Save") {
                enableWhen(controller.sBook.dirty)
                action {
                    confirm(
                            header = "Apply Changes?",
                            actionFn = {
                                runAsync {
                                    val index = controller.bookList.indexOf(controller.sBook.item)
                                    val old = controller.sBook.item.copy()
                                    controller.sBook.item.dupe = 0
                                    controller.sBook.commit()
                                    val book = controller.sBook.item.copy()
                                    controller.bookList[index] = Book()
                                    controller.checkDupeBook(book)
                                    controller.bookList[index] = book
                                    controller.undoList.add(Action("Edited", old, controller.bookList[index].copy()))
                                    controller.redoList.setAll()
                                }
                                close()
                            }
                    )
                }
            }
            button("Cancel").action {
                controller.sBook.rollback()
                close()
            }
            button("Reset") {
                enableWhen(controller.sBook.dirty)
                action {
                    controller.sBook.rollback()
                }
            }
        }
    }
}
