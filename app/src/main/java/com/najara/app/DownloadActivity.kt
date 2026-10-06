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
import com.najara.app.data.DownloadItem
import com.najara.app.data.DownloadStore

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

    // इंस्टॉल किए हुए 1DM का पैकेज नाम (नहीं है तो null)
    private fun find1DMPackage(): String? {
        val packages = listOf(
            "idm.internet.download.manager",
            "idm.internet.download.manager.plus",
            "idm.internet.download.manager.lite"
        )
        for (pkg in packages) {
            try {
                packageManager.getPackageInfo(pkg, 0)
                return pkg
            } catch (e: Exception) {
                // अगला पैकेज चेक करें
            }
        }
        return null
    }

    // 1DM Check + सीधे 1DM में लिंक खोलो
    private fun checkAndOpen1DM(url: String) {
        val pkg = find1DMPackage()
        if (pkg != null) {
            val uri = Uri.parse(url)
            val attempts = listOf(
                Intent(Intent.ACTION_VIEW, uri).setPackage(pkg),
                Intent(Intent.ACTION_VIEW).setDataAndType(uri, "video/*").setPackage(pkg)
            )
            var opened = false
            for (intent in attempts) {
                try {
                    startActivity(intent)
                    opened = true
                    break
                } catch (e: Exception) {
                    // अगला तरीका आज़माएँ
                }
            }
            if (!opened) {
                Toast.makeText(
                    this,
                    "1DM में लिंक नहीं खुला, Regular Download चुनें",
                    Toast.LENGTH_LONG
                ).show()
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

    // In-App Download: फ़ाइल ऐप के अपने फ़ोल्डर में उतरती है और Downloads पेज में दिखती है
    private fun startInAppDownload(url: String, title: String) {
        try {
            val fileName = DownloadStore.makeFileName(title, url)
            DownloadStore.clearExisting(this, fileName)

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(title)
                setDescription("डाउनलोड हो रही है...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(
                    this@DownloadActivity,
                    Environment.DIRECTORY_MOVIES,
                    fileName
                )
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val id = dm.enqueue(request)

            DownloadStore.add(
                this,
                DownloadItem(id, title, fileName, System.currentTimeMillis())
            )

            Toast.makeText(
                this,
                "डाउनलोड शुरू! ऊपर Downloads आइकन में देखें।",
                Toast.LENGTH_LONG
            ).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "त्रुटि: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
