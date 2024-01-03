package ru.tech.imageresizershrinker.feature.root.presentation

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.util.Consumer
import androidx.core.view.WindowInsetsControllerCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import ru.tech.imageresizershrinker.coreui.model.toUiState
import ru.tech.imageresizershrinker.coreui.theme.ImageToolboxTheme
import ru.tech.imageresizershrinker.coreui.utils.confetti.LocalConfettiController
import ru.tech.imageresizershrinker.coreui.utils.helper.ContextUtils.isInstalledFromPlayStore
import ru.tech.imageresizershrinker.coreui.utils.helper.ContextUtils.parseImageFromIntent
import ru.tech.imageresizershrinker.coreui.utils.navigation.LocalNavController
import ru.tech.imageresizershrinker.coreui.widget.UpdateSheet
import ru.tech.imageresizershrinker.coreui.widget.controls.EnhancedSliderInit
import ru.tech.imageresizershrinker.coreui.widget.haptics.customHapticFeedback
import ru.tech.imageresizershrinker.coreui.widget.other.LocalToastHost
import ru.tech.imageresizershrinker.coreui.widget.other.ToastHost
import ru.tech.imageresizershrinker.coreui.widget.other.rememberToastHostState
import ru.tech.imageresizershrinker.coreui.widget.sheets.ProcessImagesPreferenceSheet
import ru.tech.imageresizershrinker.coreui.widget.utils.LocalEditPresetsState
import ru.tech.imageresizershrinker.coreui.widget.utils.LocalImageLoader
import ru.tech.imageresizershrinker.coreui.widget.utils.LocalSettingsState
import ru.tech.imageresizershrinker.feature.root.presentation.components.AppExitDialog
import ru.tech.imageresizershrinker.feature.root.presentation.components.EditPresetsSheet
import ru.tech.imageresizershrinker.feature.root.presentation.components.FirstLaunchSetupDialog
import ru.tech.imageresizershrinker.feature.root.presentation.components.JxlWarning
import ru.tech.imageresizershrinker.feature.root.presentation.components.PermissionDialog
import ru.tech.imageresizershrinker.feature.root.presentation.components.ScreenSelector
import ru.tech.imageresizershrinker.feature.root.presentation.components.particles

@Composable
fun RootContent(
    component: RootComponent
) {
    val context = LocalContext.current as ComponentActivity

    DisposableEffect(context) {
        val listener = Consumer<Intent> {
            context.parseImage(it, component)
        }
        listener.accept(context.intent)

        context.addOnNewIntentListener(listener)

        onDispose {
            context.removeOnNewIntentListener(listener)
        }
    }

    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    val editPresetsState = rememberSaveable { mutableStateOf(false) }

    EnhancedSliderInit()

    val settingsState = component.settingsState
    CompositionLocalProvider(
        LocalToastHost provides component.toastHostState,
        LocalSettingsState provides settingsState.toUiState(),
        LocalNavController provides component.navController,
        LocalEditPresetsState provides editPresetsState,
        LocalConfettiController provides rememberToastHostState(),
        LocalImageLoader provides component.imageLoader,
        LocalHapticFeedback provides customHapticFeedback(settingsState.hapticsStrength)
    ) {
        val showSelectSheetState = rememberSaveable(component.showSelectDialog) {
            mutableStateOf(component.showSelectDialog)
        }
        val showUpdateSheetState = rememberSaveable(component.showUpdateDialog) {
            mutableStateOf(component.showUpdateDialog)
        }
        val shouldShowDialog = component.shouldShowDialog

        LaunchedEffect(showSelectSheetState.value) {
            if (!showSelectSheetState.value) {
                delay(600)
                component.hideSelectDialog()
                component.updateUris(null)
            }
        }
        LaunchedEffect(showUpdateSheetState.value) {
            if (!showUpdateSheetState.value) {
                delay(600)
                component.cancelledUpdate()
            }
        }
        val conf = LocalConfiguration.current
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(conf.orientation) {
            if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                systemUiController.isNavigationBarVisible = false
                systemUiController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                systemUiController.isNavigationBarVisible = true
                systemUiController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
        ImageToolboxTheme {
            val tiramisu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

            if (!tiramisu) {
                BackHandler {
                    if (shouldShowDialog) showExitDialog = true
                    else context.finishAffinity()
                }
            }

            Surface(Modifier.fillMaxSize()) {
                ScreenSelector(
                    component
                )

                EditPresetsSheet(
                    editPresetsState = editPresetsState,
                    updatePresets = component::setPresets
                )
                val uris = component.uris
                val hasPdfUri = component.hasPdfUri
                ProcessImagesPreferenceSheet(
                    uris = hasPdfUri?.let {
                        listOf(it)
                    } ?: uris,
                    hasPdf = hasPdfUri != null,
                    visible = showSelectSheetState
                )
            }

            AppExitDialog(
                onDismiss = { showExitDialog = false },
                visible = showExitDialog && !tiramisu
            )

            UpdateSheet(
                tag = component.tag,
                changelog = component.changelog,
                visible = showUpdateSheetState
            )

            ToastHost(
                hostState = LocalConfettiController.current,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                toast = {
                    val primary = MaterialTheme.colorScheme.primary
                    KonfettiView(
                        modifier = Modifier.fillMaxSize(),
                        parties = remember {
                            particles(
                                primary
                            )
                        }
                    )
                }
            )

            ToastHost(
                hostState = LocalToastHost.current
            )

            SideEffect {
                component.tryGetUpdate(
                    installedFromMarket = context.isInstalledFromPlayStore()
                )
            }
            JxlWarning()

            context.FirstLaunchSetupDialog(
                toggleShowUpdateDialog = component::toggleShowUpdateDialog,
                toggleAllowBetas = component::toggleAllowBetas
            )

            PermissionDialog()
        }
    }
}

private fun Context.parseImage(intent: Intent?, component: RootComponent) {
    parseImageFromIntent(
        onStart = {
            component.hideSelectDialog()
        },
        onHasPdfUri = {
            component.updateHasPdfUri(it)
        },
        onColdStart = {
            component.shouldShowExitDialog(false)
        },
        onGetUris = {
            component.updateUris(it)
        },
        showToast = { message, icon ->
            component.showToast(message = message, icon = icon)
        },
        navigate = {
            component.navController.navigate(it)
        },
        notHasUris = component.uris.isEmpty(),
        intent = intent
    )
}