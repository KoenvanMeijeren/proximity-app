@file:Suppress("DEPRECATION")

package com.jansenenco.proximity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var buttonChrome: Button
    private lateinit var buttonGmail: Button
    private lateinit var buttonPhone: Button

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor

    private val packageNames = arrayOf(
        "com.android.chrome",
        "com.google.android.calendar",
        "com.oneplus.gallery",
        "com.google.android.gm",
        "com.android.settings",
        "com.android.dialer"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeButtons()
        initializeProximitySensor()
    }

    private fun changePage(buttonId: Int) {
        val intent = Intent(this, AppActivity::class.java)
        intent.putExtra("id", buttonId)
        startActivity(intent)
    }

    private fun initializeButtons() {
        buttonChrome = findViewById(R.id.buttonChrome)
        buttonGmail = findViewById(R.id.buttonGmail)
        buttonPhone = findViewById(R.id.buttonPhone)

        buttonChrome.setOnClickListener {
            changePage(buttonChrome.id)
        }

        buttonGmail.setOnClickListener {
            changePage(buttonGmail.id)
        }

        buttonPhone.setOnClickListener {
            changePage(buttonPhone.id)
        }
    }

    private fun initializeProximitySensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        sensorManager.registerListener(
            proximitySensorEventListener,
            proximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private var proximitySensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

        }

        override fun onSensorChanged(event: SensorEvent) {
            val params = this@MainActivity.window.attributes
            if (event.sensor.type == Sensor.TYPE_PROXIMITY && event.values[0] == 0f) {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                window.attributes = params

                val randomNumber: Int = Random.nextInt(packageNames.size)
                val pm: PackageManager = applicationContext.packageManager
                val allApps: List<PackageInfo> = pm.getInstalledPackages(PackageManager.GET_META_DATA)

                for (app in allApps) {
                    if (app.packageName == packageNames[randomNumber]) {
                        proximitySensorMessage.text = getString(R.string.successfulOpening)
                        val appIntent: Intent? = pm.getLaunchIntentForPackage(packageNames[randomNumber])
                        initializeIntent(appIntent!!)
                        break
                    } else {
                        proximitySensorMessage.text = getString(R.string.failedOpening)
                    }
                }
            }
        }
    }

    private fun initializeIntent(intent: Intent) {
        val activities: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
        val isIntentSafe: Boolean = activities.isNotEmpty()

        if (isIntentSafe) {
            startActivity(intent)
        }
    }
}
