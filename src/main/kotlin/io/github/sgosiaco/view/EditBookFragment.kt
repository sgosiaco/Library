package io.github.sgosiaco.view

import io.github.sgosiaco.library.MyController
import javafx.util.converter.IntegerStringConverter
import tornadofx.*

class EditBookFragment : Fragment() {
    private val controller: MyController by inject()
    private val widthList = mutableListOf<Double>()

    override fun onDock() {
        currentStage?.width = widthList.max() ?: 0.0
    }

    override val root = form {
        title = """Editing ${controller.sBook.title.value}"""
        fieldset("Info") {
            field("Title") {
                textfield(controller.sBook.title)
                widthList.add(text.length * 47.0)
            }
            field("Author(s)") {
                textfield(controller.sBook.author)
                widthList.add(text.length * 47.0)
            }
            field("Publisher(s)") {
                textfield(controller.sBook.pub)
                widthList.add(text.length * 47.0)
            }
            field("Year") {
                textfield(controller.sBook.year, IntegerStringConverter())
                widthList.add(text.length * 47.0)
            }

        }
        hbox {
            button("Save") {
                enableWhen(controller.sBook.dirty)
                action {
                    confirm(
                            header = "Apply Changes?",
                            actionFn = {
                                val index = controller.bookList.indexOf(controller.sBook.item)
                                controller.sBook.commit()
                                controller.bookList[index] = controller.sBook.item
                                close()
                            }
                    )
                }
            }
            button("Cancel").action {
                controller.sBook.rollback()
                close()
            }
            button("Reset").action {
                controller.sBook.rollback()
            }
        }
    }
}
