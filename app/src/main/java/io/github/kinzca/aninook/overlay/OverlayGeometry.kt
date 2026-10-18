package io.github.kinzca.aninook.overlay

data class PetPosition(val x: Int, val y: Int)

fun clampPosition(x: Int, y: Int, screenWidth: Int, screenHeight: Int, petSize: Int) =
    PetPosition(x.coerceIn(0, (screenWidth - petSize).coerceAtLeast(0)),
        y.coerceIn(0, (screenHeight - petSize).coerceAtLeast(0)))
