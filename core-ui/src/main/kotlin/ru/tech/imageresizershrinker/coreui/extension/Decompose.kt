package ru.tech.imageresizershrinker.coreui.extension

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.reflect.KProperty

val ComponentContext.coroutineScope: CoroutineScope
    get() {
        return instanceKeeper.getOrCreate {
            object : InstanceKeeper.Instance {
                val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

                override fun onDestroy() {
                    scope.cancel()
                }
            }
        }.scope
    }

@JvmInline
value class Nullable<T>(
    val value: T?
) {
    operator fun component1(): T? = value
    operator fun getValue(t: T?, property: KProperty<*>): T? = value
    fun copy(value: T?) = Nullable(value)
}

fun <T> T?.wrap(): Nullable<T> = Nullable(this)