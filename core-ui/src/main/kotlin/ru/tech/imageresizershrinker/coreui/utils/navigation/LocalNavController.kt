package ru.tech.imageresizershrinker.coreui.utils.navigation

import androidx.compose.runtime.compositionLocalOf

val LocalNavController = compositionLocalOf<NavController> { error("NavController not present") }

interface NavController {

    fun pop()

    fun navigate(screen: Screen)

    fun popUpTo(selector: (Screen) -> Boolean)

}