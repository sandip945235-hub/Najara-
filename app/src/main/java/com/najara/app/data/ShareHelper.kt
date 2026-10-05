package com.najara.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {

    private const val PREFS_NAME = "share_prefs"
    private const val KEY_LAST_SHOWN = "last_shown_time"
    private const val INTERVAL_MS = 48L * 60 * 60 * 1000 // 48 घंटे

    // शेयर होने वाली APK का नाम
    private const val SHARED_APK_NAME = "Najara.apk"

    // ⚠️ यहाँ अपना APK download link डालो
    // Google Drive link, या अपनी website का link, या Play Store link
    private const val APP_DOWNLOAD_LINK = "https://play.google.com/store/apps/details?id=com.najara.app"

    /**
     * 48 घंटे बाद dialog दिखाना है या नहीं check करो
     */
    fun shouldShowDialog(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastShown = prefs.getLong(KEY_LAST_SHOWN, 0L)
        val now = System.currentTimeMillis()
        return (now - lastShown) >= INTERVAL_MS
    }

    /**
     * Dialog दिखाने के बाद time save करो
     */
    fun markDialogShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_SHOWN, System.currentTimeMillis()).apply()
    }

    /**
     * App का Link share करने का function
     */
    fun shareAppLink(context: Context) {
        val message = """
            🎬 Najara App देखो! 🍿
            
            नई मूवीज़, वेब सीरीज़ और बहुत कुछ!
            अभी Download करो और Unlimited Entertainment enjoy करो।
            
            📥 Download: $APP_DOWNLOAD_LINK
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, "Share Link via"))
    }

    /**
     * App की APK share करने का function
     * इंस्टॉल की गई base.apk को पहले Najara.apk नाम से कॉपी करता है, फिर शेयर करता है
     */
    fun shareApk(context: Context) {
        try {
            val original = File(context.packageCodePath)

            if (!original.exists()) {
                shareAppLink(context)
                return
            }

            // कैश फ़ोल्डर में Najara.apk नाम से कॉपी
            val apkFile = File(context.cacheDir, SHARED_APK_NAME)
            original.copyTo(apkFile, overwrite = true)

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }

            val message = """
                🎬 Najara App install करो!
                
                नई मूवीज़, वेब सीरीज़ और बहुत कुछ!
                APK नीचे भेजी है — install करके enjoy करो।
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share APK via"))
        } catch (e: Exception) {
            shareAppLink(context)
        }
    }
}
