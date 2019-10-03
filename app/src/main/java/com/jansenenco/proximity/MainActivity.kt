@file:Suppress("DEPRECATION")

package com.jansenenco.proximity

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var buttonChrome: Button
    private lateinit var buttonGmail: Button
    private lateinit var buttonPhone: Button

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor

    private val packagesNames = arrayOf(
        "com.snapchat.android",
        "com.android.phone",
        "com.android.settings",
        "com.linkedin.android",
        "com.google.android.calendar",
        "com.android.chrome",
        "com.instagram.android",
        "com.spotify.music",
        "com.whatsapp",
        "com.google.android.youtube",
        "com.google.android.apps.maps",
        "com.facebook.katana"
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
            if (event.sensor.type == Sensor.TYPE_PROXIMITY && event.values[0] < 8f) {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                window.attributes = params

                val arraySize = packagesNames.size
                val randomNumber = Random.nextInt(arraySize)
                val pm = applicationContext.packageManager
                val appIntent: Intent? = pm.getLaunchIntentForPackage(packagesNames[randomNumber])

                Thread.sleep(2)
                initializeIntent(appIntent!!)
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
