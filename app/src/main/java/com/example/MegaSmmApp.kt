package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MegaSmmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:600165504488:android:3ff6623e88973623a6405d")
                    .setApiKey("AIzaSyBgnjCzonHDVdb9Io72vaQ55QfpP9fPjVM")
                    .setProjectId("mega-smm-india")
                    .setStorageBucket("mega-smm-india.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("MegaSmmApp", "Firebase initialized successfully with explicit fallback options")
            } else {
                Log.d("MegaSmmApp", "Firebase already initialized by Provider")
            }
        } catch (e: Exception) {
            Log.e("MegaSmmApp", "Error during Firebase initialization", e)
        }
    }
}
