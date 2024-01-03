@file:Suppress("SameParameterValue")

package ru.tech.imageresizershrinker.feature.main.presentation.viewModel

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.exifinterface.media.ExifInterface
import com.arkivanov.decompose.ComponentContext
import com.t8rin.dynamic.theme.ColorTuple
import com.t8rin.dynamic.theme.extractPrimaryColor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ru.tech.imageresizershrinker.coredomain.image.ImageManager
import ru.tech.imageresizershrinker.coredomain.model.FontFam
import ru.tech.imageresizershrinker.coredomain.model.NightMode
import ru.tech.imageresizershrinker.coredomain.model.SettingsState
import ru.tech.imageresizershrinker.coredomain.saving.FileController
import ru.tech.imageresizershrinker.coredomain.use_case.backup_and_restore.CreateBackupFileUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.backup_and_restore.CreateBackupFilenameUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.backup_and_restore.RestoreFromBackupFileUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.RegisterAppOpenUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetAlignmentUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetBorderWidthUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetColorTupleUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetColorTuplesUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetEmojiUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetEmojisCountUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetFilenamePrefixUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetFilenameSuffixUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetFontScaleUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetFontUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetImagePickerModeUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetNightModeUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetSaveFolderUriUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetScreenOrderUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetThemeContrastUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetThemeStyleUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetVibrationStrengthUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleAddFileSizeUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleAddOriginalFilenameUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleAddSequenceNumberUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleAllowBetasUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleAllowCollectAnalyticsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleAllowCollectCrashlyticsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleAllowImageMonetUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleAmoledModeUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleAutoPinClipboardUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleClearCacheOnLaunchUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleDrawAppBarShadowsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleDrawButtonShadowsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleDrawContainerShadowsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleDrawFabShadowsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleDrawSliderShadowsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleDrawSwitchShadowsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleDynamicColorsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleGroupOptionsByTypesUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleInvertColorsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleLockDrawOrientationUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleOverwriteFilesUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleRandomizeFilenameUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleScreensSearchEnabledUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleShowDialogUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.get_settings_state.GetSettingsStateFlowUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.get_settings_state.GetSettingsStateUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.reset_settings.ResetSettingsUseCase
import ru.tech.imageresizershrinker.coreui.extension.coroutineScope
import ru.tech.imageresizershrinker.coreui.utils.navigation.Screen
import ru.tech.imageresizershrinker.coreui.widget.other.ToastHostState
import java.io.OutputStream

class MainComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val tryGetUpdates: (Boolean, Boolean, () -> Unit) -> Unit,
    @Assisted val updateAvailable: State<Boolean>,
    @Assisted val toastHostState: ToastHostState,
    getSettingsStateFlowUseCase: GetSettingsStateFlowUseCase,
    private val imageManager: ImageManager<Bitmap, ExifInterface>,
    private val fileController: FileController,
    private val getSettingsStateUseCase: GetSettingsStateUseCase,
    private val toggleAddSequenceNumberUseCase: ToggleAddSequenceNumberUseCase,
    private val toggleAddOriginalFilenameUseCase: ToggleAddOriginalFilenameUseCase,
    private val setEmojisCountUseCase: SetEmojisCountUseCase,
    private val setImagePickerModeUseCase: SetImagePickerModeUseCase,
    private val toggleAddFileSizeUseCase: ToggleAddFileSizeUseCase,
    private val setEmojiUseCase: SetEmojiUseCase,
    private val setFilenamePrefixUseCase: SetFilenamePrefixUseCase,
    private val toggleShowDialogUseCase: ToggleShowDialogUseCase,
    private val setColorTupleUseCase: SetColorTupleUseCase,
    private val toggleDynamicColorsUseCase: ToggleDynamicColorsUseCase,
    private val setBorderWidthUseCase: SetBorderWidthUseCase,
    private val toggleAllowImageMonetUseCase: ToggleAllowImageMonetUseCase,
    private val toggleAmoledModeUseCase: ToggleAmoledModeUseCase,
    private val setNightModeUseCase: SetNightModeUseCase,
    private val setSaveFolderUriUseCase: SetSaveFolderUriUseCase,
    private val setColorTuplesUseCase: SetColorTuplesUseCase,
    private val setAlignmentUseCase: SetAlignmentUseCase,
    private val setScreenOrderUseCase: SetScreenOrderUseCase,
    private val toggleClearCacheOnLaunchUseCase: ToggleClearCacheOnLaunchUseCase,
    private val toggleGroupOptionsByTypesUseCase: ToggleGroupOptionsByTypesUseCase,
    private val toggleRandomizeFilenameUseCase: ToggleRandomizeFilenameUseCase,
    private val createBackupFileUseCase: CreateBackupFileUseCase,
    private val restoreFromBackupFileUseCase: RestoreFromBackupFileUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase,
    private val createBackupFilenameUseCase: CreateBackupFilenameUseCase,
    private val setFontUseCase: SetFontUseCase,
    private val setFontScaleUseCase: SetFontScaleUseCase,
    private val toggleAllowCollectCrashlyticsUseCase: ToggleAllowCollectCrashlyticsUseCase,
    private val toggleAllowCollectAnalyticsUseCase: ToggleAllowCollectAnalyticsUseCase,
    private val toggleAllowBetasUseCase: ToggleAllowBetasUseCase,
    private val toggleDrawContainerShadowsUseCase: ToggleDrawContainerShadowsUseCase,
    private val toggleDrawButtonShadowsUseCase: ToggleDrawButtonShadowsUseCase,
    private val toggleDrawFabShadowsUseCase: ToggleDrawFabShadowsUseCase,
    private val toggleDrawSliderShadowsUseCase: ToggleDrawSliderShadowsUseCase,
    private val toggleDrawSwitchShadowsUseCase: ToggleDrawSwitchShadowsUseCase,
    private val registerAppOpenUseCase: RegisterAppOpenUseCase,
    private val toggleLockDrawOrientationUseCase: ToggleLockDrawOrientationUseCase,
    private val setThemeContrastUseCase: SetThemeContrastUseCase,
    private val setThemeStyleUseCase: SetThemeStyleUseCase,
    private val toggleInvertColorsUseCase: ToggleInvertColorsUseCase,
    private val toggleScreensSearchEnabledUseCase: ToggleScreensSearchEnabledUseCase,
    private val toggleDrawAppBarShadowsUseCase: ToggleDrawAppBarShadowsUseCase,
    private val toggleAutoPinClipboardUseCase: ToggleAutoPinClipboardUseCase,
    private val setVibrationStrengthUseCase: SetVibrationStrengthUseCase,
    private val toggleOverwriteFilesUseCase: ToggleOverwriteFilesUseCase,
    private val setFilenameSuffixUseCase: SetFilenameSuffixUseCase
) : ComponentContext by componentContext {

    private val _settingsState = mutableStateOf(SettingsState.Default())
    val settingsState: SettingsState by _settingsState

    init {
        if (settingsState.clearCacheOnLaunch) clearCache()

        runBlocking {
            registerAppOpenUseCase()
            _settingsState.value = getSettingsStateUseCase()
        }
        getSettingsStateFlowUseCase().onEach {
            _settingsState.value = it
        }.launchIn(coroutineScope)
    }

    fun getReadableCacheSize(): String = fileController.getReadableCacheSize()

    fun clearCache(onComplete: (String) -> Unit = {}) = fileController.clearCache(onComplete)

    fun tryGetUpdate(
        newRequest: Boolean,
        installedFromMarket: Boolean,
        onNoUpdates: () -> Unit = {}
    ) = tryGetUpdates(newRequest, installedFromMarket, onNoUpdates)

    fun toggleAddSequenceNumber() {
        coroutineScope.launch {
            toggleAddSequenceNumberUseCase()
        }
    }

    fun toggleAddOriginalFilename() {
        coroutineScope.launch {
            toggleAddOriginalFilenameUseCase()
        }
    }

    fun setEmojisCount(count: Int) {
        coroutineScope.launch {
            setEmojisCountUseCase(count)
        }
    }

    fun setImagePickerMode(mode: Int) {
        coroutineScope.launch {
            setImagePickerModeUseCase(mode)
        }
    }

    fun toggleAddFileSize() {
        coroutineScope.launch {
            toggleAddFileSizeUseCase()
        }
    }

    fun setEmoji(emoji: Int) {
        coroutineScope.launch {
            setEmojiUseCase(emoji)
        }
    }

    fun setFilenamePrefix(name: String) {
        coroutineScope.launch {
            setFilenamePrefixUseCase(name)
        }
    }

    fun setFilenameSuffix(name: String) {
        coroutineScope.launch {
            setFilenameSuffixUseCase(name)
        }
    }

    fun toggleShowUpdateDialog() {
        coroutineScope.launch {
            toggleShowDialogUseCase()
        }
    }

    fun setColorTuple(colorTuple: ColorTuple) {
        coroutineScope.launch {
            setColorTupleUseCase(
                colorTuple.run {
                    "${primary.toArgb()}*${secondary?.toArgb()}*${tertiary?.toArgb()}*${surface?.toArgb()}"
                }
            )
        }
    }

    fun toggleDynamicColors() {
        coroutineScope.launch {
            toggleDynamicColorsUseCase()
        }
    }

    fun toggleLockDrawOrientation() {
        coroutineScope.launch {
            toggleLockDrawOrientationUseCase()
        }
    }

    fun setBorderWidth(width: Float) {
        coroutineScope.launch {
            setBorderWidthUseCase(width)
        }
    }

    fun toggleAllowImageMonet() {
        coroutineScope.launch {
            toggleAllowImageMonetUseCase()
        }
    }

    fun toggleAmoledMode() {
        coroutineScope.launch {
            toggleAmoledModeUseCase()
        }
    }

    fun setNightMode(nightMode: NightMode) {
        coroutineScope.launch {
            setNightModeUseCase(nightMode)
        }
    }


    fun showToast(
        message: String,
        icon: ImageVector? = null,
    ) {
        coroutineScope.launch {
            toastHostState.showToast(
                message = message, icon = icon
            )
        }
    }

    fun updateSaveFolderUri(uri: Uri?) {
        coroutineScope.launch {
            setSaveFolderUriUseCase(uri?.toString())
        }
    }

    private fun List<ColorTuple>.asString(): String = joinToString(separator = "*") {
        "${it.primary.toArgb()}/${it.secondary?.toArgb()}/${it.tertiary?.toArgb()}/${it.surface?.toArgb()}"
    }

    fun updateColorTuples(colorTuples: List<ColorTuple>) {
        coroutineScope.launch {
            setColorTuplesUseCase(colorTuples.asString())
        }
    }

    fun setAlignment(align: Float) {
        coroutineScope.launch {
            setAlignmentUseCase(align.toInt())
        }
    }

    fun updateOrder(data: List<Screen>) {
        coroutineScope.launch {
            setScreenOrderUseCase(data.joinToString("/") { it.id.toString() })
        }
    }

    fun toggleClearCacheOnLaunch() {
        coroutineScope.launch {
            toggleClearCacheOnLaunchUseCase()
        }
    }

    fun toggleGroupOptionsByType() {
        coroutineScope.launch {
            toggleGroupOptionsByTypesUseCase()
        }
    }

    fun toggleRandomizeFilename() {
        coroutineScope.launch {
            toggleRandomizeFilenameUseCase()
        }
    }

    fun createBackup(outputStream: OutputStream?, onSuccess: () -> Unit) {
        coroutineScope.launch {
            outputStream?.use {
                it.write(createBackupFileUseCase())
            }
            onSuccess()
        }
    }

    fun restoreBackupFrom(
        uri: Uri,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                restoreFromBackupFileUseCase(uri.toString(), onSuccess, onFailure)
            }
        }
    }

    fun resetSettings() {
        coroutineScope.launch {
            resetSettingsUseCase()
        }
    }

    fun createBackupFilename(): String = createBackupFilenameUseCase()

    fun setFont(font: FontFam) {
        coroutineScope.launch {
            setFontUseCase(font)
        }
    }

    fun onUpdateFontScale(scale: Float) {
        coroutineScope.launch {
            setFontScaleUseCase(scale)
        }
    }

    fun toggleAllowCollectCrashlytics() {
        coroutineScope.launch {
            toggleAllowCollectCrashlyticsUseCase()
        }
    }

    fun toggleAllowCollectAnalytics() {
        coroutineScope.launch {
            toggleAllowCollectAnalyticsUseCase()
        }
    }

    fun toggleAllowBetas(installedFromMarket: Boolean) {
        coroutineScope.launch {
            toggleAllowBetasUseCase()
            tryGetUpdate(
                newRequest = true,
                installedFromMarket = installedFromMarket
            )
        }
    }

    fun toggleDrawContainerShadows() {
        coroutineScope.launch {
            toggleDrawContainerShadowsUseCase()
        }
    }

    fun toggleDrawSwitchShadows() {
        coroutineScope.launch {
            toggleDrawSwitchShadowsUseCase()
        }
    }

    fun toggleDrawSliderShadows() {
        coroutineScope.launch {
            toggleDrawSliderShadowsUseCase()
        }
    }

    fun toggleDrawButtonShadows() {
        coroutineScope.launch {
            toggleDrawButtonShadowsUseCase()
        }
    }

    fun toggleDrawFabShadows() {
        coroutineScope.launch {
            toggleDrawFabShadowsUseCase()
        }
    }

    fun addColorTupleFromEmoji(getEmoji: (Int?) -> String, showShoeDescription: (String) -> Unit) {
        coroutineScope.launch {
            val emojiUri = getEmoji(settingsState.selectedEmoji)
            if (emojiUri.contains("shoe", true)) {
                showShoeDescription(emojiUri)
                setFont(FontFam.DejaVu)
                val colorTuple = ColorTuple(
                    primary = Color(0xFF6D216D),
                    secondary = Color(0xFF240A95),
                    tertiary = Color(0xFFFFFFA0),
                    surface = Color(0xFF1D2D3D)
                )
                val colorTupleS = listOf(colorTuple).asString()
                setColorTuple(colorTuple)
                setColorTuplesUseCase(settingsState.colorTupleList + "*" + colorTupleS)
                updateThemeContrast(0f)
                setThemeStyle(0)
                if (settingsState.isInvertThemeColors) toggleInvertColors()
            } else {
                imageManager.getImage(data = emojiUri)
                    ?.extractPrimaryColor()
                    ?.let { primary ->
                        val colorTuple = ColorTuple(primary)
                        val colorTupleS = listOf(colorTuple).asString()
                        setColorTuple(colorTuple)
                        setColorTuplesUseCase(settingsState.colorTupleList + "*" + colorTupleS)
                    }
            }
            if (settingsState.isDynamicColors) toggleDynamicColors()
        }
    }

    fun updateThemeContrast(value: Float) {
        coroutineScope.launch {
            setThemeContrastUseCase(value.toDouble())
        }
    }

    fun setThemeStyle(value: Int) {
        coroutineScope.launch {
            setThemeStyleUseCase(value)
        }
    }

    fun toggleInvertColors() {
        coroutineScope.launch {
            toggleInvertColorsUseCase()
        }
    }

    fun toggleScreenSearchEnabled() {
        coroutineScope.launch {
            toggleScreensSearchEnabledUseCase()
        }
    }

    fun toggleDrawAppBarShadows() {
        coroutineScope.launch {
            toggleDrawAppBarShadowsUseCase()
        }
    }

    fun toggleAutoPinClipboard() {
        coroutineScope.launch {
            toggleAutoPinClipboardUseCase()
        }
    }

    fun setVibrationStrength(strength: Int) {
        coroutineScope.launch {
            setVibrationStrengthUseCase(strength)
        }
    }

    fun toggleOverwriteFiles() {
        coroutineScope.launch {
            toggleOverwriteFilesUseCase()
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            tryGetUpdates: (Boolean, Boolean, () -> Unit) -> Unit,
            updateAvailable: State<Boolean>,
            toastHostState: ToastHostState
        ): MainComponent
    }

}