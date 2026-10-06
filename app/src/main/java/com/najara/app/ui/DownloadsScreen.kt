package com.najara.app.data

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class DownloadItem(
    val id: Long,
    val title: String,
    val fileName: String,
    val addedAt: Long = 0L
)

data class DownloadProgress(
    val status: Int,
    val downloaded: Long,
    val total: Long
)

object DownloadStore {

    private const val PREFS = "najara_downloads"
    private const val KEY = "items"

    fun getAll(context: Context): List<DownloadItem> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                DownloadItem(
                    id = o.getLong("id"),
                    title = o.optString("title"),
                    fileName = o.optString("fileName"),
                    addedAt = o.optLong("addedAt", 0L)
                )
            }.sortedByDescending { it.addedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveAll(context: Context, list: List<DownloadItem>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("fileName", it.fileName)
                    .put("addedAt", it.addedAt)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, arr.toString())
            .apply()
    }

    fun add(context: Context, item: DownloadItem) {
        val list = getAll(context).filter { it.id != item.id } + item
        saveAll(context, list)
    }

    fun makeFileName(title: String, url: String): String {
        val clean = title
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
            .ifEmpty { "movie" }
        val rawExt = url.substringBefore('?').substringAfterLast('.', "")
        val ext = if (rawExt.length in 2..4 && rawExt.all { it.isLetterOrDigit() }) {
            rawExt.lowercase()
        } else {
            "mp4"
        }
        return if (clean.lowercase().endsWith(".$ext")) clean else "$clean.$ext"
    }

    fun fileFor(context: Context, fileName: String): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            ?: context.filesDir
        return File(dir, fileName)
    }

    fun clearExisting(context: Context, fileName: String) {
        try {
            val f = fileFor(context, fileName)
            if (f.exists()) f.delete()
        } catch (e: Exception) {
        }
    }

    fun query(context: Context, id: Long): DownloadProgress? {
        return try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val cursor = dm.query(DownloadManager.Query().setFilterById(id)) ?: return null
            cursor.use {
                if (!it.moveToFirst()) return null
                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val downloaded = it.getLong(
                    it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                )
                val total = it.getLong(
                    it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                )
                DownloadProgress(status, downloaded, total)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun remove(context: Context, item: DownloadItem) {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.remove(item.id)
        } catch (e: Exception) {
        }
        try {
            fileFor(context, item.fileName).delete()
        } catch (e: Exception) {
        }
        saveAll(context, getAll(context).filter { it.id != item.id })
    }
}
