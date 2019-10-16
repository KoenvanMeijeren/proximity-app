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
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var shortInput: Boolean = true
    private val shortInputCode: String = "Kort "
    private val longInputCode: String = "Lang "

    private var input: String = ""
    private val inputLength: Int = 15
    private var sensorHasBeenCalled: Boolean = false

    private lateinit var countDownTimer: CountDownTimer
    private val initialCountDown: Long = 2000
    private val countDownInterval: Long = 1000

    private lateinit var buttonChrome: Button
    private lateinit var buttonGmail: Button
    private lateinit var buttonPhone: Button
    private lateinit var resetInputButton: Button

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor

    private val defaultPackage: String = "com.android.chrome"
    private val packageNames = mapOf(
        shortInputCode + shortInputCode + shortInputCode to "nl.dumpert",
        longInputCode + shortInputCode + shortInputCode to "com.android.chrome",
        shortInputCode + longInputCode + shortInputCode to "com.android.settings",
        shortInputCode + shortInputCode + longInputCode to "com.android.dialer",
        longInputCode + longInputCode + longInputCode to "com.oneplus.gallery",
        longInputCode + longInputCode + shortInputCode to "com.google.android.gm",
        longInputCode + shortInputCode + longInputCode to "com.google.android.calendar",
        shortInputCode + longInputCode + longInputCode to "com.snapchat.android"
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
            proximitySensorMessage.text = ""
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

    private fun initializeIntent(intent: Intent) {
        val activities: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)

        if (activities.isNotEmpty()) {
            startActivity(intent)
        }
    }

    private var proximitySensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

        }

        override fun onSensorChanged(event: SensorEvent) {
            val params = this@MainActivity.window.attributes
            if (event.sensor.type == Sensor.TYPE_PROXIMITY && event.values[0] == 0f) {
                sensorHasBeenCalled = true
                shortInput = true

                params.flags = params.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                window.attributes = params

                startCountDownTimer()
            } else if (sensorHasBeenCalled) {
                addInput()
                showCurrentCodeOnScreen()

                val packageManager: PackageManager = applicationContext.packageManager
                val installedApps: List<PackageInfo> =
                    packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

                openApp(packageManager, installedApps)
            }
        }
    }

    private fun startCountDownTimer() {
        countDownTimer = object : CountDownTimer(initialCountDown, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTimerMessage.text = getString(
                    R.string.shortInput, millisUntilFinished / 1000 + 1
                )
            }

            override fun onFinish() {
                countDownTimerMessage.text = getString(R.string.longInput)
                shortInput = false
            }
        }.start()
    }

    private fun addInput() {
        if (shortInput) {
            input += shortInputCode
            countDownTimer.cancel()
            return
        }

        input += longInputCode
    }

    private fun showCurrentCodeOnScreen() {
        proximitySensorMessage.text = input
        addToastMessage(getString(R.string.inputCode, input))
    }

    private fun openApp(packageManager: PackageManager, installedApps: List<PackageInfo>) {
        if (inputLength() && codeExists() && appIsInstalled(installedApps)) {
            initializeIntent(packageManager.getLaunchIntentForPackage(getAppName())!!)

            addToastMessage(getString(R.string.successfulOpening))
            resetInput()
        } else if (inputLength() && (!codeExists() || !appIsInstalled(installedApps))) {
            addToastMessage(getString(R.string.failedOpening))
            resetInput()
        }
    }

    private fun inputLength(): Boolean {
        return input.length == inputLength
    }

    private fun codeExists(): Boolean {
        return packageNames.containsKey(input)
    }

    private fun appIsInstalled(installedApps: List<PackageInfo>): Boolean {
        for (app in installedApps) {
            if (app.packageName == getAppName()) {
                return true
            }
        }

        return false
    }

    private fun getAppName(): String {
        return packageNames.getOrDefault(input, defaultPackage)
    }

    private fun addToastMessage(string: String) {
        Toast.makeText(this@MainActivity, string, Toast.LENGTH_LONG).show()
    }

    private fun resetInput() {
        input = ""
    }
}
