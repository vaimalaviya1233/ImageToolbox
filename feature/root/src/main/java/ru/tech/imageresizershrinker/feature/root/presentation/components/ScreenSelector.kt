package ru.tech.imageresizershrinker.feature.root.presentation.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.arkivanov.decompose.extensions.compose.jetpack.stack.Children
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.stackAnimation
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import com.t8rin.dynamic.theme.rememberAppColorTuple
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.coreui.widget.utils.LocalSettingsState
import ru.tech.imageresizershrinker.feature.main.presentation.MainScreen
import ru.tech.imageresizershrinker.feature.root.presentation.RootComponent
import ru.tech.imageresizershrinker.feature.single_edit.presentation.SingleEditScreen

@Composable
fun ScreenSelector(
    component: RootComponent
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settingsState = LocalSettingsState.current
    val themeState = LocalDynamicThemeState.current
    val appColorTuple = rememberAppColorTuple(
        defaultColorTuple = settingsState.appColorTuple,
        dynamicColor = settingsState.isDynamicColors,
        darkTheme = settingsState.isNightMode
    )
    val onGoBack: () -> Unit = {
        component.updateUris(null)
        component.goBack()
        scope.launch {
            kotlinx.coroutines.delay(350L)
            themeState.updateColorTuple(appColorTuple)
        }
    }
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val easing = CubicBezierEasing(0.48f, 0.19f, 0.05f, 1.03f)

    Children(
        stack = component.childStack,
        modifier = Modifier.fillMaxSize(),
        animation = stackAnimation(slide())
    ) {
        when (val instance = it.instance) {
            is RootComponent.Child.Main -> MainScreen(instance.component)
            is RootComponent.Child.SingleEdit -> SingleEditScreen(
                uriState = instance.uri,
                onGoBack = onGoBack,
                viewModel = instance.component
            )

            is RootComponent.Child.Compare -> TODO()
            is RootComponent.Child.Crop -> TODO()
            is RootComponent.Child.DeleteExif -> TODO()
            is RootComponent.Child.Draw -> TODO()
            is RootComponent.Child.EraseBackground -> TODO()
            is RootComponent.Child.Filter -> TODO()
            is RootComponent.Child.GeneratePalette -> TODO()
            is RootComponent.Child.ImagePreview -> TODO()
            is RootComponent.Child.ImageStitching -> TODO()
            is RootComponent.Child.LimitResize -> TODO()
            is RootComponent.Child.LoadNetImage -> TODO()
            is RootComponent.Child.PdfTools -> TODO()
            is RootComponent.Child.PickColorFromImage -> TODO()
            is RootComponent.Child.ResizeAndConvert -> TODO()
            is RootComponent.Child.ResizeByBytes -> TODO()
            is RootComponent.Child.Cipher -> TODO()
        }
    }
}

//
//            is Screen.ResizeAndConvert -> {
//                ResizeAndConvertScreen(
//                    uriState = screen.uris,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.DeleteExif -> {
//                DeleteExifScreen(
//                    uriState = screen.uris,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.ResizeByBytes -> {
//                BytesResizeScreen(
//                    uriState = screen.uris,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.Crop -> {
//                ru.tech.imageresizershrinker.feature.crop.presentation.CropScreen(
//                    uriState = screen.uri,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.PickColorFromImage -> {
//                PickColorFromImageScreen(
//                    uriState = screen.uri,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.ImagePreview -> {
//                ImagePreviewScreen(
//                    uriState = screen.uris,
//                    onGoBack = {
//                        if (screen.uris != null) {
//                            context.findActivity()?.finishAffinity()
//                        } else onGoBack()
//                    }
//                )
//            }
//
//            is Screen.GeneratePalette -> {
//                GeneratePaletteScreen(
//                    uriState = screen.uri,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.Compare -> {
//                CompareScreen(
//                    comparableUris = screen.uris
//                        ?.takeIf { it.size == 2 }
//                        ?.let { it[0] to it[1] },
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.LoadNetImage -> {
//                LoadNetImageScreen(
//                    url = screen.url,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.Filter -> {
//                FiltersScreen(
//                    type = screen.type,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.LimitResize -> {
//                LimitsResizeScreen(
//                    uriState = screen.uris,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.Draw -> {
//                DrawScreen(
//                    uriState = screen.uri,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.Cipher -> {
//                FileCipherScreen(
//                    uriState = screen.uri,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.EraseBackground -> {
//                EraseBackgroundScreen(
//                    uriState = screen.uri,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.ImageStitching -> {
//                ImageStitchingScreen(
//                    uriState = screen.uris,
//                    onGoBack = onGoBack
//                )
//            }
//
//            is Screen.PdfTools -> {
//                PdfToolsScreen(
//                    type = screen.type,
//                    onGoBack = {
//                        if (screen.type != null) {
//                            context.findActivity()?.finishAffinity()
//                        } else onGoBack()
//                    }
//                )
//            }