package io.github.sgosiaco.library.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.model.Action
import javafx.geometry.Side
import javafx.print.PageOrientation
import javafx.print.Paper
import javafx.print.Printer
import javafx.print.PrinterJob
import javafx.scene.control.TabPane
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.transform.Scale
import tornadofx.*
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class MainView : View("Library") {
    private val controller: MainController by inject()
    private lateinit var drawer: Drawer

    override fun onDock() {
        currentStage?.isMaximized = true
    }

    override val root = vbox {
        menubar {
            menu("File") {
                menu("Open") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.FOLDER_OPEN)
                    item("Open Book List").action { controller.openDialog("book") }
                    item("Open Person List").action { controller.openDialog("person") }
                    item("Open Checkout List").action { controller.openDialog("checked") }
                }
                menu("Export") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.DOWNLOAD)
                    item("Export Book List to CSV").action {
                        confirm(
                                header = "Export the book list to CSV?",
                                actionFn = { controller.exportBookCSV() }
                        )
                    }
                    item("Export People List to CSV").action {
                        confirm(
                                header = "Export the people list to CSV?",
                                actionFn = { controller.exportPeopleCSV() }
                        )
                    }
                    item("Export Checked List to CSV").action {
                        confirm(
                                header = "Export the checked list to CSV?",
                                actionFn = { controller.exportCheckedCSV() }
                        )
                    }
                    item("Export All CSV").action {
                        confirm(
                                header = "Export all lists to CSV?",
                                actionFn = {
                                    controller.exportBookCSV()
                                    controller.exportPeopleCSV()
                                    controller.exportCheckedCSV()
                                    controller.exportLog()
                                }
                        )
                    }
                    item("Export Log").action {
                        confirm(
                                header = "Export log?",
                                actionFn = { controller.exportLog() }
                        )
                    }
                }
                item("Print", "Shortcut+P", FontAwesomeIconView(FontAwesomeIcon.PRINT)).action {
                    val printer = Printer.getDefaultPrinter()
                    val layout = printer.createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT)
                    var scaleX = layout.printableWidth / drawer.boundsInParent.width
                    var scaleY = layout.printableHeight / drawer.boundsInParent.height
                    val snap = drawer.snapshot(null, null)
                    val image = ImageView(snap)
                    if (scaleY > scaleX) scaleY = scaleX else scaleX = scaleY
                    image.transforms.add(Scale(scaleX, scaleY))

                    val printerJob = PrinterJob.createPrinterJob(printer)
                    if (printerJob.showPrintDialog(primaryStage.owner) && printerJob.printPage(layout, image)) {
                        printerJob.endJob()
                    }
                }
                menu("Save") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.SAVE)
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
                item("Quit", "Shortcut+Q", FontAwesomeIconView(FontAwesomeIcon.SIGN_OUT)).action {
                    confirm(
                            header = "Are you sure you want to quit?",
                            actionFn = { exitProcess(1) }
                    )
                }
            }
            menu("Add") {
                item("Add New Book") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS_SQUARE)
                    action { find<AddBookFragment>().openModal() }
                }
                item("Add New Person") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.USER_PLUS)
                    action { find<AddPersonFragment>().openModal() }
                }
            }
            menu("Edit") {
                item("Undo", "Shortcut+Z", FontAwesomeIconView(FontAwesomeIcon.UNDO)) {
                    action { controller.undo(controller.undoList.last()) }
                    enableWhen { booleanBinding(controller.undoList) { isNotEmpty() } }
                }
                item("Redo", "Shortcut+Shift+Z", FontAwesomeIconView(FontAwesomeIcon.REPEAT)) {
                    action { controller.redo(controller.redoList.last()) }
                    enableWhen { booleanBinding(controller.redoList) { isNotEmpty() } }
                }
                item("Duplicate Book") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.COPY) //clone is another choice
                    action {
                        if (controller.focus == "Books") {
                            runAsync {
                                val book = controller.sBook.item.copy()
                                controller.checkDupeBook(book)
                                controller.bookList.add(book)
                            }
                        }
                    }
                }
                item("Modify Selected", "Shortcut+E", FontAwesomeIconView(FontAwesomeIcon.EDIT)).action {
                    if (controller.focus == "Books") {
                        find<EditBookFragment>().openModal()
                    } else {
                        find<EditPersonFragment>().openModal()
                    }
                }
                item("Show history", "Shortcut+H", FontAwesomeIconView(FontAwesomeIcon.HISTORY)).action { find<HistoryFragment>().openWindow() }
            }
            menu("Help") {
                item("Access Import Steps") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.QUESTION)
                    action {
                        dialog("Access (Office 365 Version) Import Steps") {
                            text("""1. External Data->New Data Source->From File->Text File
                            |2. Find and select the "books.csv" file and make sure "Import the source data into a new table in the current database." option is selected.
                            |3. Make sure "Delimited" is selected.
                            |4. Make sure "Comma" is selected as the delimiter. Then, make sure to change the text qualifier to " (quote) and then check the box "First Row Contains Field Names"
                            |5. Use the bottom scroll bar to move to the "Year" column and change the data type to "Integer". Then move to the "Checked Out" column and changed the data type to "Yes/No".
                            |6. Choose "Let Access add primary key."
                            |7. Finally, enter the name of the table to import the data into.
                            |**If you plan on reimporting often, check the box "Save import steps" after importing and this will remember all the settings used and the file location for the "books.csv" file
                            |**To run the saved import again go to External Data->Saved Imports and select the import you saved previously. (Warning: this will only write/overwite the same table so be careful!)""".trimMargin()) {
                                style {
                                    fontSize = 16.px
                                }
                            }
                        }
                    }
                }
                item("About") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.INFO)
                    action {
                        dialog("About") {
                            text("Built and developed by Sean Gosiaco") {
                                style {
                                    fontSize = 16.px
                                }
                            }
                        }
                    }
                }
            }
        }
        drawer = drawer(side = Side.LEFT, multiselect = true) {
            vgrow = Priority.ALWAYS
            item("Books", showHeader = false) {
                expanded = true
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab(BookView::class)
                    tab(CheckedBookView::class)
                    tab("Log") {
                        tableview(controller.undoList) {
                            vgrow = Priority.ALWAYS
                            columnResizePolicy = SmartResize.POLICY
                            controller.undoList.onChange {
                                requestResize()
                            }
                            readonlyColumn("Timestamp", Action::timestamp).cellFormat {
                                text = it.format(controller.logFormat)
                            }
                            readonlyColumn("Action", Action::action)
                            readonlyColumn("Object", Action::obj).prefWidth(1000.0)
                            readonlyColumn("New Object", Action::newObj).prefWidth(200.0)
                        }
                    }
                }

            }
            item("People", showHeader = false) {
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab(PeopleView::class)
                    tab(CheckedPersonView::class)
                }
            }
        }
    }
}