package ru.tech.imageresizershrinker.feature.root.presentation

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import coil.ImageLoader
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import ru.tech.imageresizershrinker.coredomain.model.SettingsState
import ru.tech.imageresizershrinker.coreui.utils.navigation.NavController
import ru.tech.imageresizershrinker.coreui.widget.other.ToastHostState
import ru.tech.imageresizershrinker.feature.bytes_resize.presentation.viewModel.BytesResizeViewModel
import ru.tech.imageresizershrinker.feature.cipher.presentation.viewModel.FileCipherViewModel
import ru.tech.imageresizershrinker.feature.compare.presentation.viewModel.CompareViewModel
import ru.tech.imageresizershrinker.feature.crop.presentation.viewModel.CropViewModel
import ru.tech.imageresizershrinker.feature.delete_exif.presentation.viewModel.DeleteExifViewModel
import ru.tech.imageresizershrinker.feature.draw.presentation.viewModel.DrawViewModel
import ru.tech.imageresizershrinker.feature.erase_background.presentation.viewModel.EraseBackgroundViewModel
import ru.tech.imageresizershrinker.feature.filters.presentation.viewModel.FilterViewModel
import ru.tech.imageresizershrinker.feature.generate_palette.presentation.viewModel.GeneratePaletteViewModel
import ru.tech.imageresizershrinker.feature.image_preview.presentation.viewModel.ImagePreviewViewModel
import ru.tech.imageresizershrinker.feature.image_stitch.presentation.viewModel.ImageStitchingViewModel
import ru.tech.imageresizershrinker.feature.limits_resize.presentation.viewModel.LimitsResizeViewModel
import ru.tech.imageresizershrinker.feature.load_net_image.presentation.viewModel.LoadNetImageViewModel
import ru.tech.imageresizershrinker.feature.main.presentation.viewModel.MainComponent
import ru.tech.imageresizershrinker.feature.pdf_tools.presentation.viewModel.PdfToolsViewModel
import ru.tech.imageresizershrinker.feature.pick_color.presentation.viewModel.PickColorViewModel
import ru.tech.imageresizershrinker.feature.resize_convert.presentation.viewModel.ResizeAndConvertViewModel
import ru.tech.imageresizershrinker.feature.single_edit.presentation.viewModel.SingleEditComponent

@Stable
interface RootComponent {

    val navController: NavController

    val changelog: String
    val tag: String
    val hasPdfUri: Uri?
    val uris: List<Uri>
    val showUpdateDialog: Boolean
    val showSelectDialog: Boolean
    val shouldShowDialog: Boolean
    val updateAvailable: Boolean

    val toastHostState: ToastHostState

    val childStack: Value<ChildStack<*, Child>>

    fun hideSelectDialog()

    fun updateUris(uris: List<Uri>?)
    fun cancelledUpdate()
    fun goBack()
    fun setPresets(presets: List<Int>)
    fun updateHasPdfUri(uri: Uri?)
    fun shouldShowExitDialog(value: Boolean)
    fun showToast(message: String, icon: ImageVector?)

    fun tryGetUpdate(
        newRequest: Boolean = false,
        installedFromMarket: Boolean,
        onNoUpdates: () -> Unit = {}
    )

    fun toggleShowUpdateDialog()
    fun toggleAllowBetas(b: Boolean)

    sealed class Child {
        class Main(val component: MainComponent) : Child()
        class Cipher(val component: FileCipherViewModel) : Child()
        class Compare(val component: CompareViewModel) : Child()
        class Crop(val component: CropViewModel) : Child()
        class DeleteExif(val component: DeleteExifViewModel) : Child()
        class Draw(val component: DrawViewModel) : Child()
        class EraseBackground(val component: EraseBackgroundViewModel) : Child()
        class Filter(val component: FilterViewModel) : Child()
        class GeneratePalette(val component: GeneratePaletteViewModel) : Child()
        class ImagePreview(val component: ImagePreviewViewModel) : Child()
        class ImageStitching(val component: ImageStitchingViewModel) : Child()
        class LimitResize(val component: LimitsResizeViewModel) : Child()
        class LoadNetImage(val component: LoadNetImageViewModel) : Child()
        class PdfTools(val component: PdfToolsViewModel) : Child()
        class PickColorFromImage(val component: PickColorViewModel) : Child()
        class ResizeAndConvert(val component: ResizeAndConvertViewModel) : Child()
        class ResizeByBytes(val component: BytesResizeViewModel) : Child()
        class SingleEdit(val component: SingleEditComponent, val uri: Uri?) : Child()
    }

    val settingsState: SettingsState

    val imageLoader: ImageLoader

}