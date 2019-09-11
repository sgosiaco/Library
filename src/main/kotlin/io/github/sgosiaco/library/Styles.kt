package io.github.sgosiaco.library

import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
    }

    init {
        label and heading {
            padding = box(10.px)
            fontSize = 20.px //20.px
            fontWeight = FontWeight.BOLD
        }
        tableView {
            fontSize = 16.px
        }
        tab {
            fontSize = 14.px
        }
        menuBar {
            fontSize = 14.px
        }
        menu {
            fontSize = 14.px
        }
        menuItem {
            fontSize = 14.px
        }
        button {
            fontSize = 14.px
        }
        textField {
            fontSize = 14.px
        }
        field {
            fontSize = 14.px
        }
        confirmation {
            fontSize = 14.px
        }
        toggleButton {
            fontSize = 14.px
        }
    }
}