package io.github.sgosiaco.library

import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.view.MainView
import tornadofx.*

class MyApp : App(MainView::class, Styles::class) {
    private val controller: MainController by inject()

    override fun stop() {
        controller.exportLog()
        controller.saveBooks()
        controller.saveChecked()
        controller.savePeople()
        super.stop()

    }
}