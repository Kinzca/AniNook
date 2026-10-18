package io.github.kinzca.aninook.imports

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties
import java.util.UUID

data class PendingModel(val name: String, val bytes: Long, val storedName: String)

/** Stores one opaque model for later engine validation. Never interprets model contents. */
class PendingModelStore(private val directory: File) {
    fun current(): PendingModel? = synchronized(lock) { readCurrent() }

    fun import(name: String, input: InputStream): PendingModel = synchronized(lock) {
        require(name.substringAfterLast('.', "").lowercase() in setOf("inp", "inx")) {
            "请选择 .inp 或 .inx 文件。当前版本只保存文件，尚不能加载模型。"
        }
        check(directory.isDirectory || directory.mkdirs()) { "无法创建本地模型目录。" }
        val previous = readCurrent()
        val target = File(directory, "${UUID.randomUUID()}.model")
        val metadata = File(directory, "${UUID.randomUUID()}.tmp")
        try {
            val size = target.outputStream().use { output ->
                copyBounded(input, output, MAX_BYTES)
            }
            require(size > 0) { "文件为空，请选择完整的模型文件。" }
            val model = PendingModel(name.take(200), size, target.name)
            val properties = Properties().apply {
                setProperty("name", model.name)
                setProperty("bytes", size.toString())
                setProperty("storedName", target.name)
            }
            metadata.outputStream().use { properties.store(it, "AniNook pending model; not validated") }
            Files.move(metadata.toPath(), File(directory, "current.properties").toPath(),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            previous?.let { File(directory, it.storedName).delete() }
            model
        } catch (error: Exception) {
            target.delete()
            metadata.delete()
            throw error
        }
    }

    fun clear() = synchronized(lock) {
        val previous = readCurrent()
        val metadata = File(directory, "current.properties")
        check(!metadata.exists() || metadata.delete()) { "无法移除模型记录。" }
        previous?.let { File(directory, it.storedName).delete() }
        Unit
    }

    private fun readCurrent(): PendingModel? {
        val metadata = File(directory, "current.properties")
        if (!metadata.isFile) return null
        val properties = Properties().apply { metadata.inputStream().use(::load) }
        val storedName = properties.getProperty("storedName")
        check(storedName != null && Regex("[a-f0-9-]{36}\\.model").matches(storedName)) {
            "本地模型记录损坏，请清除应用数据后重新导入。"
        }
        val file = File(directory, storedName)
        check(file.isFile) { "本地模型文件丢失，请重新导入。" }
        return PendingModel(properties.getProperty("name"), file.length(), storedName)
    }

    companion object {
        const val MAX_BYTES = 64L * 1024 * 1024
        private val lock = Any()
    }
}

internal fun copyBounded(input: InputStream, output: java.io.OutputStream, maxBytes: Long): Long {
    val buffer = ByteArray(8192)
    var total = 0L
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        total += read
        require(total <= maxBytes) { "文件超过 64 MiB；验证版暂不接收更大的模型。" }
        output.write(buffer, 0, read)
    }
    return total
}
