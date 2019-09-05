package io.github.sgosiaco.library

import io.github.sgosiaco.view.MainView
import tornadofx.App
import com.google.gson.* //not sure if needed to make sure it includes in jar or if compile was enough instead of implementation in build.gradle

class MyApp: App(MainView::class, Styles::class)