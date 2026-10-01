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
    
