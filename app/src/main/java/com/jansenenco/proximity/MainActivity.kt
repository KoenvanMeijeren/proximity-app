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
import android.os.CountDownTimer
import android.view.WindowManager
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var timer: Long = 0
    private var input: String = ""
    private var determineSensorHasBeenCalled: Boolean = false

    internal lateinit var countDownTimer: CountDownTimer
    internal val initialCountDown: Long = 2000
    internal val countDownInterval: Long = 1000

    private lateinit var buttonChrome: Button
    private lateinit var buttonGmail: Button
    private lateinit var buttonPhone: Button
    private lateinit var resetInputButton: Button

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor

    private val packageNames = mapOf(
        "KKK" to "nl.dumpert",
        "LKK" to "com.android.chrome",
        "KLK" to "com.android.settings",
        "KKL" to "com.android.dialer",
        "LLL" to "com.oneplus.gallery",
        "LLK" to "com.google.android.gm",
        "LKL" to "com.google.android.calendar",
        "KLL" to "com.snapchat.android"
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
        resetInputButton = findViewById(R.id.resetInputButton)

        buttonChrome.setOnClickListener {
            changePage(buttonChrome.id)
        }

        buttonGmail.setOnClickListener {
            changePage(buttonGmail.id)
        }

        buttonPhone.setOnClickListener {
            changePage(buttonPhone.id)
        }

        resetInputButton.setOnClickListener {
            resetInput()
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
                determineSensorHasBeenCalled = true
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                window.attributes = params

                timer = startTimer()
            } else if (determineSensorHasBeenCalled) {
                addSensorInputToString(stopTimer(timer) / 1000)
                showCurrentCodeOnScreen()
                if (stringIsOfExpectedLength(3) && codeIsInArray()) {
                    val pm: PackageManager = applicationContext.packageManager
                    val allApps: List<PackageInfo> = pm.getInstalledPackages(PackageManager.GET_META_DATA)

                    for (allApp in allApps) {
                        if(allApp.packageName == packageNames.getOrDefault(input, "com.android.chrome")){
                            proximitySensorMessage.text = getString(R.string.successfulOpening)
                            val appIntent: Intent? = pm.getLaunchIntentForPackage(packageNames.getValue(input))
                            initializeIntent(appIntent!!)
                            resetInput()
                            break
                        } else {
                            proximitySensorMessage.text = getString(R.string.failedOpening)
                        }
                    }
                }
            }
        }
    }

    private fun startTimer(): Long {
        return System.currentTimeMillis()
    }

    private fun stopTimer(startCurrentMillis: Long): Long {
        return System.currentTimeMillis() - startCurrentMillis
    }

    private fun showLength() { //eerst alleen timer laten zien, later alleen kort of lang

    }

    private fun addSensorInputToString(time: Long) {
        if (time > 2) {
            input += "L"
            return
        }
        input += "K"
        return
    }

    private fun resetInput () {
        input = ""
        proximitySensorMessage.text = ""
    }

    private fun stringIsOfExpectedLength(length: Int): Boolean {
        return input.length == length
    }

    private fun codeIsInArray(): Boolean {
        return packageNames.containsKey(input)
    }

    private fun showCurrentCodeOnScreen() {
        proximitySensorMessage.text = input
    }

    private fun initializeIntent(intent: Intent) {
        val activities: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
        val isIntentSafe: Boolean = activities.isNotEmpty()

        if (isIntentSafe) {
            startActivity(intent)
        }
    }
}
