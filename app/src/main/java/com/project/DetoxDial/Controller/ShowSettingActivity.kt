package com.project.DetoxDial.Controller

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.project.DetoxDial.Models.PreferencesManager
import com.project.DetoxDial.R
import java.util.*

class ShowSettingActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocate()
        setContentView(R.layout.activity_show_setting)

        val actionBar = supportActionBar
        actionBar!!.title = "SETTING"


        val btn_changeLang = findViewById<Button>(R.id.btn_changelang)
        btn_changeLang.setOnClickListener {
            showChangeLang()
        }


        val btn_animation= findViewById<Button>(R.id.Animation1)
        btn_animation.setOnClickListener {
            showAnimation()
        }


        val btn_DoNot = findViewById<Button>(R.id.btn_DontDisturb)
        btn_DoNot.setOnClickListener {
            val settingdnd = Intent()
            settingdnd.component = ComponentName("com.android.settings", "com.android.settings.Settings\$ZenModeSettingsActivity")
            startActivity(settingdnd)
        }

        val btn_SMS = findViewById<Button>(R.id.btn_msg_package)
        btn_SMS.setOnClickListener {
            val prefs = PreferencesManager.getPreferencesInstance(this)
            prefs.setSmsEnabled(false)
            prefs.setSmsPackageName("")
            Toast.makeText(this,"Default sms app is resetted ",Toast.LENGTH_SHORT).show()

            /*
            val i = Intent(this, InstalledAppsActivity::class.java)
            startActivityForResult(i, )
            Toast.makeText(this, SMS_APP_PACKAGE_NAME_RESULT, Toast.LENGTH_SHORT).show()*/

        }

    }



    /** Show dialogbox with the different language profiles **/

    private fun showChangeLang(){

        val listItems = arrayOf("العربية", "中國人","Dansk","English","Français","Deutsch","Español")

        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setTitle("Choose Language")
        mBuilder.setSingleChoiceItems(listItems,-1) { dialog, which ->
            if (which == 0) {
                setLocate("ar")

                recreate()
                Toast.makeText(this, "تم اختيار اللغة العربية", Toast.LENGTH_SHORT).show()
                val refresh = Intent(this, MainActivity::class.java)
                startActivity(refresh)

            } else if (which == 1) {
                setLocate("zh")
                recreate()
                Toast.makeText(this, "Chinese selected.", Toast.LENGTH_SHORT).show()
                val refresh = Intent(this, MainActivity::class.java)
                startActivity(refresh)

            } else if (which == 2) {
                setLocate("da")
                recreate()
                Toast.makeText(this, "Danish selected.", Toast.LENGTH_SHORT).show()
                val refresh = Intent(this, MainActivity::class.java)
                startActivity(refresh)

            } else if (which == 3) {
                setLocate("en")
                recreate()
                Toast.makeText(this, "English selected.", Toast.LENGTH_SHORT).show()
                val refresh = Intent(this, MainActivity::class.java)
                startActivity(refresh)

            } else if (which == 4) {
                setLocate("fr")
                recreate()
                Toast.makeText(this, "French selected.", Toast.LENGTH_SHORT).show()
                val refresh = Intent(this, MainActivity::class.java)
                startActivity(refresh)

            } else if (which == 5) {
                setLocate("de")
                recreate()
                Toast.makeText(this, "Deutsch selected.", Toast.LENGTH_SHORT).show()
                val refresh = Intent(this, MainActivity::class.java)
                startActivity(refresh)

            } else if (which == 6) {
                setLocate("es")
                recreate()
                Toast.makeText(this, "Spanish selected.", Toast.LENGTH_SHORT).show()
                val refresh = Intent(this, MainActivity::class.java)
                startActivity(refresh)

            }
            dialog.dismiss()

        }
        val mDialog = mBuilder.create()

        mDialog.show()
    }

    /** Set language profile **/

    private fun setLocate(Lang: String) {

        val locale = Locale(Lang)

        Locale.setDefault(locale)

        val config = Configuration()

        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My Lang", Lang)
        editor.apply()
        //loadLocate()
    }

    /** Load Language profile **/

    private fun loadLocate() {
        val sharedPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE)
        val language = sharedPreferences.getString("My Lang", "")
        if (language != null) {
            setLocate(language)
        }
    }

    /** function to turn animation on/off */
    fun showAnimation(){

        val listItems = arrayOf("ON","OFF")
        val prefs: PreferencesManager = PreferencesManager.getPreferencesInstance(this)
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setTitle("Animation")
        mBuilder.setSingleChoiceItems(listItems,-1) { dialog, tilstand ->
            when (tilstand) {
                0 -> {prefs.setShowAimation(true)
                        Toast.makeText(this, "Animation turned on.", Toast.LENGTH_SHORT).show()
                    val refresh = Intent(this, MainActivity::class.java)
                    startActivity(refresh)
                }
                1 -> {prefs.setShowAimation(false)
                    Toast.makeText(this, "Animation turned off.", Toast.LENGTH_SHORT).show()
                    val refresh = Intent(this, MainActivity::class.java)
                    startActivity(refresh)
                }
            }
            dialog.dismiss()
        }
        val mDialog = mBuilder.create()

        mDialog.show()
    }







}