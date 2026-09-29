package com.yourapp.movieplayer

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DownloadActivity : AppCompatActivity() {

    private val monetagDirectLink = "https://omg10.com/4/11919349"

    private var mainAdShown = false
    private var resolutionAdCount = 0
    private var finalAdCount = 0
    private var selectedChoice = ""

    private var movieUrl = "https://yourserver.com/files/SpeedDemon_720p.mkv"
    private var movieTitle = "Speed Demon (2026).mkv"

    private lateinit var btnMainDownload: Button
    private lateinit var layoutResolution: LinearLayout
    private lateinit var btn720p: Button
    private lateinit var btn480p: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        movieUrl = intent.getStringExtra("movie_url") ?: movieUrl
        movieTitle = intent.getStringExtra("movie_title") ?: movieTitle

        btnMainDownload = findViewById(R.id.btnMainDownload)
        layoutResolution = findViewById(R.id.layoutResolution)
        btn720p = findViewById(R.id.btn720p)
        btn480p = findViewById(R.id.btn480p)

        btnMainDownload.setOnClickListener {
            if (!mainAdShown) {
                mainAdShown = true
                openMonetagAdLink()
                btnMainDownload.text = "फिर क्लिक करें"
            } else {
                btnMainDownload.visibility = View.GONE
                layoutResolution.visibility = View.VISIBLE
            }
        }

        val onResolutionClick = View.OnClickListener {
            when (resolutionAdCount) {
                0 -> {
                    resolutionAdCount = 1
                    openMonetagAdLink()
                    btn720p.text = "फिर क्लिक करें (1/2)"
                    btn480p.text = "फिर क्लिक करें (1/2)"
                }
                1 -> {
                    resolutionAdCount = 2
                    openMonetagAdLink()
                    btn720p.text = "फिर क्लिक करें (2/2)"
                    btn480p.text = "फिर क्लिक करें (2/2)"
                }
                else -> {
                    layoutResolution.visibility = View.GONE
                    showDownloadChoiceDialog()
                }
            }
        }

        btn720p.setOnClickListener(onResolutionClick)
        btn480p.setOnClickListener(onResolutionClick)
    }

    private fun showDownloadChoiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("डाउनलोड विकल्प चुनें")
            .setMessage("आप कैसे डाउनलोड करना चाहते हैं?")
            .setPositiveButton("⚡ Fast Download (1DM)") { _, _ ->
                selectedChoice = "fast"
                finalAdCount = 0
                showFinalAdsStep()
            }
            .setNegativeButton("📥 Regular Download") { _, _ ->
                selectedChoice = "regular"
                finalAdCount = 0
                showFinalAdsStep()
            }
            .setCancelable(false)
            .show()
    }

    private fun showFinalAdsStep() {
        if (finalAdCount < 3) {
            finalAdCount++
            openMonetagAdLink()

            val choiceName = if (selectedChoice == "fast") "Fast" else "Regular"
            AlertDialog.Builder(this)
                .setTitle("$choiceName Download")
                .setMessage("Ad $finalAdCount / 3 पूरा हुआ।\n\nडाउनलोड शुरू करने के लिए 'जारी रखें' दबाएँ।")
                .setPositiveButton("जारी रखें") { _, _ ->
                    showFinalAdsStep()
                }
                .setCancelable(false)
                .show()
        } else {
            if (selectedChoice == "fast") {
                checkAndOpen1DM(movieUrl)
            } else {
                startInAppDownload(movieUrl, movieTitle)
            }
        }
    }

    private fun openMonetagAdLink() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(monetagDirectLink)))
        } catch (e: Exception) {
            Toast.makeText(this, "कृपया पुनः प्रयास करें...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndOpen1DM(url: String) {
        val pm = packageManager
        val is1DMInstalled = try {
            pm.getPackageInfo("idm.internet.download.manager", 0); true
        } catch (e: Exception) {
            try {
                pm.getPackageInfo("idm.internet.download.manager.lite", 0); true
            } catch (e: Exception) { false }
        }

        if (is1DMInstalled) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "1DM नहीं खुल पाया", Toast.LENGTH_SHORT).show()
            }
        } else {
            AlertDialog.Builder(this)
                .setTitle("1DM App आवश्यक है")
                .setMessage("फास्ट डाउनलोड के लिए 1DM इंस्टॉल करें या Regular Download चुनें।")
                .setPositiveButton("Play Store") { _, _ ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=idm.internet.download.manager")))
                    } catch (e: Exception) {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=idm.internet.download.manager")))
                    }
                }
                .setNegativeButton("Regular Download") { dialog, _ ->
                    dialog.dismiss()
                    startInAppDownload(movieUrl, movieTitle)
                }
                .show()
        }
    }

    private fun startInAppDownload(url: String, fileName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription("डाउनलोड हो रही है...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MyMovieApp/$fileName")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            Toast.makeText(this, "डाउनलोड शुरू! नोटिफिकेशन देखें।", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "त्रुटि: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
