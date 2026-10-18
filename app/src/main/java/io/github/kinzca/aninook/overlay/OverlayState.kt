package io.github.kinzca.aninook.overlay

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PetServiceState(val running: Boolean = false, val message: String? = null)

object OverlayState {
    private val mutable = MutableStateFlow(PetServiceState())
    val state = mutable.asStateFlow()
    fun update(running: Boolean, message: String? = null) {
        mutable.value = PetServiceState(running, message)
    }
}
