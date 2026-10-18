package io.github.kinzca.aninook.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayGeometryTest {
    @Test fun draggingCannotLeaveTheAvailableScreen() {
        assertEquals(PetPosition(0, 1800), clampPosition(-80, 3000, 1080, 2000, 200))
    }
    @Test fun rotationReclampsThePreviousPosition() {
        assertEquals(PetPosition(800, 880), clampPosition(800, 1800, 2000, 1080, 200))
    }
    @Test fun screenSmallerThanPetHasAValidOrigin() {
        assertEquals(PetPosition(0, 0), clampPosition(100, 100, 100, 100, 200))
    }
}
