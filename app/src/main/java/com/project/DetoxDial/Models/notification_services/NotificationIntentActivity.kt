package com.project.DetoxDial.Models.notification_services

import android.os.Bundle
import android.content.Intent

class NotificationIntentActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.notification_intent_activity); //dummy layout
        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras != null && extras.getString("package") != null) {
                val packageName = extras.getString("package")
                if (packageName != null) {
                    NotificationHelper.getInstance(applicationContext)
                        ?.markNotificationDismissed(packageName)
                }
                launchApp(packageName)
            } else {
                //launchHomeScreen();
            }
        }
    }

    private fun launchApp(packageName: String?) {
        val intent: Intent
        val pm = packageManager
        intent = pm.getLaunchIntentForPackage(packageName!!)!!

        // ToDo: Getting null intent sometimes when service restart is implemented #291 (Google Play report)
        if (intent == null) {
            // Toast.makeText("Unable to open application").show();
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}