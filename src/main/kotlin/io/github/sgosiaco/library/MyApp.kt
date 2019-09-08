package io.github.sgosiaco.library

import io.github.sgosiaco.view.MainView
import tornadofx.App

class MyApp: App(MainView::class, Styles::class) {
    private val controller: MyController by inject()

    override fun stop() {
        //super.stop()
        controller.saveBooks()
        controller.saveChecked()
        controller.savePeople()
    }
}