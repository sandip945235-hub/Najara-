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
    val url: String = ""
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
                    url = o.optString("url")
                )
            }
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
                    .put("url", it.url)
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

    fun fileFor(context: Context, fileName: String): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        return File(dir, fileName)
    }

    fun query(context: Context, id: Long): DownloadProgress? {
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
            return DownloadProgress(status, downloaded, total)
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
