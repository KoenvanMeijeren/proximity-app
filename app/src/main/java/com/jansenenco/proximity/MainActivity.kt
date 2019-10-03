@file:Suppress("DEPRECATION")

package com.jansenenco.proximity

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var buttonChrome: Button
    private lateinit var buttonGmail: Button
    private lateinit var buttonPhone: Button

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor

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
            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] in 0f..2f) {
                    proximitySensorMessage.text = ""
                }

                if (event.values[0] in 2f..4f) {
                    params.flags = params.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    window.attributes = params

                    val packageName = "com.android.chrome"
                    val pm = applicationContext.packageManager
                    val chromeIntent = pm.getLaunchIntentForPackage(packageName)

                    Thread.sleep(2)
                    println("App wordt geopend")
                    initializeIntent(chromeIntent!!)
                }
            }
        }
    }

    private fun initializeIntent(intent: Intent) {
        val activities: List<ResolveInfo> =
            packageManager.queryIntentActivities(intent, 0)
        val isIntentSafe: Boolean = activities.isNotEmpty()

        if (isIntentSafe) {
            startActivity(intent)
        }
    }
}
