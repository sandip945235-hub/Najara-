package com.najara.app

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

    // ✅ अपना Monetag Direct Link यहाँ डालें
    private val monetagDirectLink = "https://omg10.com/4/11919349"

    // ✅ Ad काउंटर (कुल 3 ऐड: Step1 = 1, Step2 = 1, Step3 = 1)
    private var mainAdShown = false
    private var resolutionAdShown = false
    private var selectedChoice = ""

    // मूवी डेटा (Intent से बदल सकते हैं)
    private var movieUrl = "https://yourserver.com/files/SpeedDemon_720p.mkv"
    private var movieTitle = "Speed Demon (2026).mkv"

    // UI references
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

        // STEP 1: Download बटन -> 1 Ad -> Resolution
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

        // STEP 2: 720p / 480p -> 1 Ad -> Popup
        val onResolutionClick = View.OnClickListener {
            if (!resolutionAdShown) {
                resolutionAdShown = true
                openMonetagAdLink()
                btn720p.text = "फिर क्लिक करें"
                btn480p.text = "फिर क्लिक करें"
            } else {
                layoutResolution.visibility = View.GONE
                showDownloadChoiceDialog()
            }
        }

        btn720p.setOnClickListener(onResolutionClick)
        btn480p.setOnClickListener(onResolutionClick)
    }

    // Popup: Fast vs Regular
    private fun showDownloadChoiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("डाउनलोड विकल्प चुनें")
            .setMessage("आप कैसे डाउनलोड करना चाहते हैं?")
            .setPositiveButton("⚡ Fast Download (1DM)") { _, _ ->
                selectedChoice = "fast"
                showFinalAdStep()
            }
            .setNegativeButton("📥 Regular Download") { _, _ ->
                selectedChoice = "regular"
                showFinalAdStep()
            }
            .setCancelable(false)
            .show()
    }

    // STEP 3: 1 Ad -> Download
    private fun showFinalAdStep() {
        openMonetagAdLink()

        val choiceName = if (selectedChoice == "fast") "Fast" else "Regular"
        AlertDialog.Builder(this)
            .setTitle("$choiceName Download")
            .setMessage("डाउनलोड शुरू करने के लिए 'जारी रखें' दबाएँ।")
            .setPositiveButton("जारी रखें") { _, _ ->
                if (selectedChoice == "fast") {
                    checkAndOpen1DM(movieUrl)
                } else {
                    startInAppDownload(movieUrl, movieTitle)
                }
            }
            .setCancelable(false)
            .show()
    }

    // Monetag Ad खोलें
    private fun openMonetagAdLink() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(monetagDirectLink)))
        } catch (e: Exception) {
            Toast.makeText(this, "कृपया पुनः प्रयास करें...", Toast.LENGTH_SHORT).show()
        }
    }

    // 1DM इंस्टॉल है या नहीं, यह चेक करें
    private fun is1DMInstalled(): Boolean {
        val packages = listOf(
            "idm.internet.download.manager",
            "idm.internet.download.manager.plus",
            "idm.internet.download.manager.lite"
        )
        for (pkg in packages) {
            try {
                packageManager.getPackageInfo(pkg, 0)
                return true
            } catch (e: Exception) {
                // अगला पैकेज चेक करें
            }
        }
        return false
    }

    // 1DM Check + Open
    private fun checkAndOpen1DM(url: String) {
        if (is1DMInstalled()) {
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
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=idm.internet.download.manager")
                            )
                        )
                    } catch (e: Exception) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=idm.internet.download.manager")
                            )
                        )
                    }
                }
                .setNegativeButton("Regular Download") { dialog, _ ->
                    dialog.dismiss()
                    startInAppDownload(movieUrl, movieTitle)
                }
                .show()
        }
    }

    // In-App Download (DownloadManager)
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
