/*
 * VELA canvas artwork cache.
 * Keeps canvas cache management local to the presentation layer while avoiding
 * coupling the UI to Coil's image cache implementation.
 *
 * Built on VELA, licensed under GPL-3.0.
 */
package com.iamyuvraj0.vela.ui.player

import android.content.Context
import java.io.File

object CanvasArtworkPlaybackCache {
    private const val DIRECTORY_NAME = "canvas_artwork"
    private const val DEFAULT_MAX_SIZE_BYTES = 256L * 1024L * 1024L

    @Volatile
    private var directory: File? = null

    @Volatile
    private var maxSizeBytes: Long = DEFAULT_MAX_SIZE_BYTES

    @Synchronized
    fun init(context: Context) {
        if (directory == null) {
            directory = File(context.cacheDir, DIRECTORY_NAME).apply { mkdirs() }
        } else {
            directory?.mkdirs()
        }
        trimIfNeeded()
    }

    @Synchronized
    fun setMaxSize(maxSizeMb: Int) {
        maxSizeBytes = maxSizeMb.coerceAtLeast(0).toLong() * 1024L * 1024L
        trimIfNeeded()
    }

    @Synchronized
    fun size(): Long = directory?.let { directorySize(it) } ?: 0L

    @Synchronized
    fun clear() {
        directory?.listFiles()?.forEach { it.deleteRecursively() }
    }

    private fun trimIfNeeded() {
        val dir = directory ?: return
        if (!dir.exists()) return
        var currentSize = directorySize(dir)
        if (currentSize <= maxSizeBytes) return

        dir.listFiles()
            ?.sortedBy { it.lastModified() }
            ?.forEach { file ->
                if (currentSize <= maxSizeBytes) return@forEach
                val removed = file.length()
                if (file.deleteRecursively()) currentSize -= removed
            }
    }

    private fun directorySize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        return file.listFiles()?.sumOf(::directorySize) ?: 0L
    }
}
