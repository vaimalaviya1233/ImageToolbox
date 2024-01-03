package ru.tech.imageresizershrinker.feature.root.presentation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.net.toUri
import coil.ImageLoader
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popWhile
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import org.w3c.dom.Element
import ru.tech.imageresizershrinker.coredomain.APP_RELEASES
import ru.tech.imageresizershrinker.coredomain.model.SettingsState
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.SetPresetsUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleAllowBetasUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.edit_settings.ToggleShowDialogUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.get_settings_state.GetSettingsStateFlowUseCase
import ru.tech.imageresizershrinker.coredomain.use_case.get_settings_state.GetSettingsStateUseCase
import ru.tech.imageresizershrinker.coreresources.BuildConfig
import ru.tech.imageresizershrinker.coreui.extension.coroutineScope
import ru.tech.imageresizershrinker.coreui.utils.navigation.NavController
import ru.tech.imageresizershrinker.coreui.utils.navigation.Screen
import ru.tech.imageresizershrinker.coreui.utils.state.update
import ru.tech.imageresizershrinker.coreui.widget.other.ToastHostState
import ru.tech.imageresizershrinker.feature.main.presentation.viewModel.MainComponent
import ru.tech.imageresizershrinker.feature.single_edit.presentation.viewModel.SingleEditComponent
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class DefaultRootComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    override val imageLoader: ImageLoader,
    private val mainComponentFactory: MainComponent.Factory,
    private val singleEditComponentFactory: SingleEditComponent.Factory,
    private val getSettingsStateFlowUseCase: GetSettingsStateFlowUseCase,
    private val getSettingsStateUseCase: GetSettingsStateUseCase,
    private val setPresetsUseCase: SetPresetsUseCase,
    private val toggleShowDialogUseCase: ToggleShowDialogUseCase,
    private val toggleAllowBetasUseCase: ToggleAllowBetasUseCase
) : ComponentContext by componentContext, RootComponent {

    private val _settingsState = mutableStateOf(SettingsState.Default())
    override val settingsState: SettingsState by _settingsState

    init {
        _settingsState.update {
            runBlocking {
                getSettingsStateUseCase()
            }
        }
        getSettingsStateFlowUseCase()
            .onEach { settings ->
                _settingsState.update {
                    settings
                }
            }
            .launchIn(coroutineScope)
    }

    private val navigation = StackNavigation<Screen>()


    private val _uris = mutableStateOf<List<Uri>>(emptyList())
    override val uris by _uris

    private val _hasPdfUri = mutableStateOf<Uri?>(null)
    override val hasPdfUri by _hasPdfUri

    private val _showSelectDialog = mutableStateOf(false)
    override val showSelectDialog by _showSelectDialog

    private val _showUpdateDialog = mutableStateOf(false)
    override val showUpdateDialog by _showUpdateDialog

    private val _cancelledUpdate = mutableStateOf(false)

    private val _shouldShowDialog = mutableStateOf(true)
    override val shouldShowDialog by _shouldShowDialog

    private val _tag = mutableStateOf("")
    override val tag by _tag

    private val _changelog = mutableStateOf("")
    override val changelog by _changelog

    private val _updateAvailable = mutableStateOf(false)
    override val updateAvailable by _updateAvailable

    override val toastHostState = ToastHostState()

    @OptIn(ExperimentalDecomposeApi::class)
    override val navController: NavController = object : NavController {
        override fun pop() = navigation.pop()

        override fun navigate(screen: Screen) = navigation.pushNew(screen)

        override fun popUpTo(selector: (Screen) -> Boolean) {
            if(childStack.value.backStack.isNotEmpty()) navigation.popWhile(selector)
        }
    }

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            initialConfiguration = Screen.Main,
            serializer = SerializersModule {
                Screen.serializer()
            }.serializer(),
            handleBackButton = true,
            childFactory = ::child,
        )

    override fun tryGetUpdate(
        newRequest: Boolean,
        installedFromMarket: Boolean,
        onNoUpdates: () -> Unit
    ) {
        if (settingsState.appOpenCount < 2 && !newRequest) return

        val showDialog = settingsState.showDialogOnStartup
        if (installedFromMarket) {
            if (showDialog) {
                _showUpdateDialog.value = newRequest
            }
        } else {
            if (!_cancelledUpdate.value || newRequest) {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        kotlin.runCatching {
                            val nodes =
                                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                                    URL("$APP_RELEASES.atom").openConnection().getInputStream()
                                )?.getElementsByTagName("feed")

                            if (nodes != null) {
                                for (i in 0 until nodes.length) {
                                    val element = nodes.item(i) as Element
                                    val title = element.getElementsByTagName("entry")
                                    val line = (title.item(0) as Element)
                                    _tag.value = (line.getElementsByTagName("title")
                                        .item(0) as Element).textContent
                                    _changelog.value = (line.getElementsByTagName("content")
                                        .item(0) as Element).textContent
                                }
                            }

                            if (isNeedUpdate(
                                    currentName = BuildConfig.VERSION_NAME,
                                    updateName = tag
                                )
                            ) {
                                _updateAvailable.value = true
                                if (showDialog) {
                                    _showUpdateDialog.value = true
                                }
                            } else {
                                onNoUpdates()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun toggleShowUpdateDialog() {
        coroutineScope.launch {
            toggleShowDialogUseCase()
        }
    }


    override fun toggleAllowBetas(b: Boolean) {
        coroutineScope.launch {
            toggleAllowBetasUseCase()
        }
    }

    private fun isNeedUpdate(currentName: String, updateName: String): Boolean {
        fun String.toVersionCode(): Int {
            return replace(
                regex = Regex("0\\d"),
                transform = {
                    it.value.replace("0", "")
                }
            ).replace("-", "")
                .replace(".", "")
                .replace("_", "")
                .replace("alpha", "1")
                .replace("beta", "2")
                .replace("rc", "3")
                .replace("foss", "")
                .replace("jxl", "")
                .toIntOrNull() ?: -1
        }

        val betaList = listOf(
            "alpha", "beta", "rc"
        )

        val updateVersionCode = updateName.toVersionCode()
        val currentVersionCode = currentName.toVersionCode()
        return if (!updateName.startsWith(currentName)) {
            if (betaList.all { it !in updateName }) {
                updateVersionCode > currentVersionCode
            } else {
                if (settingsState.allowBetas || betaList.any { it in currentName }) {
                    updateVersionCode > currentVersionCode
                } else false
            }
        } else false
    }

    override fun setPresets(presets: List<Int>) {
        coroutineScope.launch {
            setPresetsUseCase(
                presets.joinToString("*")
            )
        }
    }

    override fun cancelledUpdate() {
        _cancelledUpdate.value = true
        _showUpdateDialog.value = false
    }

    override fun goBack() {
        navigation.pop()
    }

    override fun hideSelectDialog() {
        _showSelectDialog.value = false
    }

    override fun updateUris(uris: List<Uri>?) {
        _uris.value = uris ?: emptyList()

        if (uris != null) _showSelectDialog.value = true
    }

    override fun updateHasPdfUri(uri: Uri?) {
        _hasPdfUri.value = uri

        if (uri != null) _showSelectDialog.value = true
    }

    override fun shouldShowExitDialog(value: Boolean) {
        _shouldShowDialog.value = value
    }

    override fun showToast(message: String, icon: ImageVector?) {
        coroutineScope.launch {
            toastHostState.showToast(
                message = message, icon = icon
            )
        }
    }

    private fun child(
        config: Screen,
        context: ComponentContext
    ): RootComponent.Child =
        when (config) {
            is Screen.Main -> RootComponent.Child.Main(
                mainComponent(
                    context = context
                )
            )

            is Screen.SingleEdit -> RootComponent.Child.SingleEdit(
                singleEditComponent(
                    context = context
                ),
                uri = config.uri?.toUri()
            )
            is Screen.Compare -> TODO()
            is Screen.Crop -> TODO()
            is Screen.DeleteExif -> TODO()
            is Screen.Draw -> TODO()
            is Screen.EraseBackground -> TODO()
            is Screen.Filter -> TODO()
            is Screen.GeneratePalette -> TODO()
            is Screen.ImagePreview -> TODO()
            is Screen.ImageStitching -> TODO()
            is Screen.LimitResize -> TODO()
            is Screen.LoadNetImage -> TODO()
            is Screen.PdfTools -> TODO()
            is Screen.PickColorFromImage -> TODO()
            is Screen.ResizeAndConvert -> TODO()
            is Screen.ResizeByBytes -> TODO()
            is Screen.Cipher -> TODO()
        }

    private fun singleEditComponent(
        context: ComponentContext
    ): SingleEditComponent = singleEditComponentFactory(
        componentContext = context
    )

    private fun mainComponent(
        context: ComponentContext
    ): MainComponent = mainComponentFactory(
        componentContext = context,
        tryGetUpdates = ::tryGetUpdate,
        updateAvailable = _updateAvailable,
        toastHostState = toastHostState
    )

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(componentContext: ComponentContext): DefaultRootComponent
    }
}