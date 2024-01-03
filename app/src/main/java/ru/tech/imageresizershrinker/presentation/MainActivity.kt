package ru.tech.imageresizershrinker.presentation

import android.os.Bundle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import dagger.hilt.android.AndroidEntryPoint
import ru.tech.imageresizershrinker.coreui.widget.utils.setContentWithWindowSizeClass
import ru.tech.imageresizershrinker.feature.root.presentation.DefaultRootComponent
import ru.tech.imageresizershrinker.feature.root.presentation.RootContent
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : M3Activity() {

    @Inject
    lateinit var rootFactory: DefaultRootComponent.Factory

    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootComponent = retainedComponent(factory = rootFactory::invoke)

        setContentWithWindowSizeClass {
            RootContent(
                component = rootComponent
            )
        }
    }

}