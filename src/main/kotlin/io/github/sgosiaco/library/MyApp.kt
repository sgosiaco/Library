package io.github.sgosiaco.library

import io.github.sgosiaco.library.controller.MainController
import io.github.sgosiaco.library.view.MainView
import tornadofx.App

class MyApp: App(MainView::class, Styles::class) {
    private val controller: MainController by inject()

    override fun stop() {
        super.stop()
        controller.saveBooks()
        controller.saveChecked()
        controller.savePeople()
    }
}