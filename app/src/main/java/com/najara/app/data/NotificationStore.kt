package com.najara.app.data

import android.app.NotificationManager
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import org.json.JSONArray
import org.json.JSONObject

data class NotifItem(
    val id: String,
    val title: String,
    val body: String,
    val time: Long,
    val read: Boolean
)

object NotificationStore {
    private const val PREFS = "najara_notifs"
    private const val KEY = "items"
    private const val MAX_ITEMS = 50

    // UI इसे पढ़कर अपने-आप अपडेट होता है
    val version = mutableStateOf(0)

    fun trayId(tag: String?, id: Int) = "n${tag ?: ""}:$id"

    private fun load(context: Context): MutableList<NotifItem> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        val list = mutableListOf<NotifItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    NotifItem(
                        o.getString("id"),
                        o.getString("title"),
                        o.getString("body"),
                        o.getLong("time"),
                        o.getBoolean("read")
                    )
                )
            }
        } catch (e: Exception) {
        }
        return list
    }

    private fun save(context: Context, list: List<NotifItem>) {
        val arr = JSONArray()
        list.take(MAX_ITEMS).forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("body", it.body)
                    .put("time", it.time)
                    .put("read", it.read)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
        version.value = version.value + 1
    }

    fun getAll(context: Context): List<NotifItem> = load(context)

    fun unreadCount(context: Context): Int = load(context).count { !it.read }

    fun add(context: Context, id: String, title: String, body: String, time: Long) {
        val list = load(context)
        if (list.any { it.id == id }) return
        list.add(0, NotifItem(id, title, body, time, false))
        save(context, list)
    }

    fun markAllRead(context: Context) {
        val list = load(context)
        if (list.none { !it.read }) return
        save(context, list.map { it.copy(read = true) })
    }

    // ऐप बंद रहते आईं नोटिफिकेशन (जो स्टेटस बार में पड़ी हैं) लिस्ट में जोड़ता है
    fun syncFromTray(context: Context) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.activeNotifications.forEach { sbn ->
                val extras = sbn.notification.extras
                val title = extras.getCharSequence("android.title")?.toString() ?: ""
                val body = extras.getCharSequence("android.text")?.toString() ?: ""
                if (title.isNotEmpty() || body.isNotEmpty()) {
                    add(context, trayId(sbn.tag, sbn.id), title, body, sbn.postTime)
                }
            }
        } catch (e: Exception) {
        }
    }
}
