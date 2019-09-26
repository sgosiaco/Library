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
                            val book = controller.sBook.item.copy()
                            controller.checkDupeBook(book)
                            controller.bookList.add(book)
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
                item("Access Import Steps").action {
                    dialog {
                        text("""Make sure you select: "Delimited".
                            | And then on the next prompt **first** select "Text Qualifier" as " (quotes) and then select "First Rows contains field names".
                            | Finally, you can change the data type of the year field to "Integer" and the data type of checkedout to "Yes/No." """.trimMargin())
                    }
                }
                item("About").action {
                    dialog {
                        text("Built and developed by Sean Gosiaco")
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