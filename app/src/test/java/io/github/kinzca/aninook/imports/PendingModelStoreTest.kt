package io.github.kinzca.aninook.imports

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

class PendingModelStoreTest {
    @get:Rule val temporary = TemporaryFolder()
    private fun store() = PendingModelStore(temporary.root)

    @Test fun importSurvivesReopeningAndKeepsExactBytes() {
        val bytes = byteArrayOf(0, 12, -1, 42)
        val model = store().import("测试.INP", ByteArrayInputStream(bytes))
        assertEquals(model, store().current())
        assertArrayEquals(bytes, File(temporary.root, model.storedName).readBytes())
    }

    @Test fun providerFilenameIsNeverUsedAsAPath() {
        val model = store().import("../../outside.inp", byteArrayOf(1).inputStream())
        assertEquals(temporary.root.canonicalFile, File(temporary.root, model.storedName).canonicalFile.parentFile)
        assertFalse(File(temporary.root.parentFile, "outside.inp").exists())
    }

    @Test fun newImportReplacesOnlyThePreviousLocalCopy() {
        val old = store().import("first.inp", byteArrayOf(1).inputStream())
        val new = store().import("second.inx", byteArrayOf(2, 3).inputStream())
        assertEquals(new, store().current())
        assertFalse(File(temporary.root, old.storedName).exists())
        assertEquals(2, temporary.root.listFiles()!!.size)
    }

    @Test fun interruptedReadPreservesPreviousModelAndCleansPartialCopy() {
        val old = store().import("first.inp", byteArrayOf(1).inputStream())
        val broken = object : InputStream() {
            override fun read(): Int = throw IOException("Provider disconnected")
        }
        assertThrows(IOException::class.java) { store().import("broken.inp", broken) }
        assertEquals(old, store().current())
        assertEquals(2, temporary.root.listFiles()!!.size)
    }

    @Test fun oversizeUnknownLengthStreamDoesNotReplacePreviousModel() {
        val old = store().import("first.inp", byteArrayOf(1).inputStream())
        val infinite = object : InputStream() {
            override fun read(): Int = 1
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                buffer.fill(1, offset, offset + length)
                return length
            }
        }
        assertThrows(IllegalArgumentException::class.java) { store().import("large.inp", infinite) }
        assertEquals(old, store().current())
        assertEquals(2, temporary.root.listFiles()!!.size)
    }

    @Test fun emptyAndUnsupportedFilesLeaveNoImportedModel() {
        assertThrows(IllegalArgumentException::class.java) { store().import("empty.inp", byteArrayOf().inputStream()) }
        assertThrows(IllegalArgumentException::class.java) { store().import("live2d.moc3", byteArrayOf(1).inputStream()) }
        assertNull(store().current())
        assertTrue(temporary.root.listFiles()!!.isEmpty())
    }

    @Test fun clearRemovesStoredCopyAndMetadata() {
        store().import("first.inp", byteArrayOf(1).inputStream())
        store().clear()
        assertNull(store().current())
        assertTrue(temporary.root.listFiles()!!.isEmpty())
    }
}
