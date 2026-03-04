package com.greenguide.greenguide

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Connects to your XML
        setContentView(R.layout.activity_main)

        val myWebView: WebView = findViewById(R.id.greenGuideWebView)

        // Enables your Google/FB login & AI logic
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true

        // Keeps the app inside the window
        myWebView.webViewClient = WebViewClient()

        // Loads your GreenGuide HTML from the assets folder
        myWebView.loadUrl("file:///android_asset/index.html")
    }
}
