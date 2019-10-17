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
    private val shortInputCode: String = "."
    private val longInputCode: String = "_"

    private var input: String = ""
    private val inputLength: Int = 3
    private val sensorCallBreakPoint: Float = 0f
    private var sensorHasBeenCalled: Boolean = false

    private lateinit var countDownTimer: CountDownTimer
    private val initialCountDown: Long = 2000
    private val countDownInterval: Long = 1000

    private lateinit var buttonChrome: Button
    private lateinit var buttonMail: Button
    private lateinit var buttonPhone: Button
    private lateinit var buttonWhatsApp: Button
    private lateinit var buttonDumpert: Button
    private lateinit var buttonSettings: Button
    private lateinit var buttonGallery: Button
    private lateinit var buttonSnapchat: Button
    private lateinit var resetInputButton: Button

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor

    private val defaultPackage: String = "com.android.chrome"
    private val packageNames = mapOf(
        shortInputCode + shortInputCode + shortInputCode to "com.android.chrome",
        longInputCode + shortInputCode + shortInputCode to "com.android.dialer",
        shortInputCode + longInputCode + shortInputCode to "com.whatsapp",
        shortInputCode + shortInputCode + longInputCode to "nl.dumpert",
        longInputCode + longInputCode + longInputCode to "com.android.settings",
        shortInputCode + longInputCode + longInputCode to "com.oneplus.gallery",
        longInputCode + shortInputCode + longInputCode to "com.google.android.gm",
        longInputCode + longInputCode + shortInputCode to "com.snapchat.android"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeButtons()
        initializeProximitySensor()
    }

    private fun initializeButtons() {
        buttonChrome = findViewById(R.id.buttonChrome)
        buttonMail = findViewById(R.id.buttonMail)
        buttonPhone = findViewById(R.id.buttonPhone)
        buttonWhatsApp = findViewById(R.id.buttonWhatsapp)
        buttonDumpert = findViewById(R.id.buttonDumpert)
        buttonSettings = findViewById(R.id.buttonSettings)
        buttonGallery = findViewById(R.id.buttonGallery)
        buttonSnapchat = findViewById(R.id.buttonSnapchat)
        resetInputButton = findViewById(R.id.resetInputButton)

        buttonChrome.setOnClickListener { changePage(buttonChrome.id) }
        buttonMail.setOnClickListener { changePage(buttonMail.id) }
        buttonPhone.setOnClickListener { changePage(buttonPhone.id) }
        buttonWhatsApp.setOnClickListener { changePage(buttonWhatsApp.id) }
        buttonDumpert.setOnClickListener { changePage(buttonDumpert.id) }
        buttonSettings.setOnClickListener { changePage(buttonSettings.id) }
        buttonGallery.setOnClickListener { changePage(buttonGallery.id) }
        buttonSnapchat.setOnClickListener { changePage(buttonSnapchat.id) }

        resetInputButton.setOnClickListener {
            resetInput()
            proximitySensorMessage.text = ""
        }
    }

    private fun changePage(buttonId: Int) {
        val intent = Intent(this, AppActivity::class.java)
        intent.putExtra("id", buttonId)
        startActivity(intent)
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
            if (event.sensor.type == Sensor.TYPE_PROXIMITY &&
                event.values[0] == sensorCallBreakPoint
            ) {
                sensorHasBeenCalled = true
                shortInput = true

                params.flags = params.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                window.attributes = params

                startCountDownTimer()
            } else if (sensorHasBeenCalled) {
                addInput()
                countDownTimer.cancel()

                showCurrentCodeOnScreen()
                openApp(applicationContext.packageManager)
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
            return
        }

        input += longInputCode
    }

    private fun showCurrentCodeOnScreen() {
        proximitySensorMessage.text = input
        addToastMessage(getString(R.string.inputCode, input))
    }

    private fun openApp(packageManager: PackageManager) {
        val installedApps: List<PackageInfo> =
            packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        if (inputOfExpectedSize() && codeExists() && appIsInstalled(installedApps)) {
            initializeIntent(packageManager.getLaunchIntentForPackage(getAppName())!!)

            addToastMessage(getString(R.string.successfulOpening, getReadableAppName(getAppName())))
            resetInput()
        } else if (inputOfExpectedSize() && (!codeExists() || !appIsInstalled(installedApps))) {
            addToastMessage(getString(R.string.failedOpening, getReadableAppName(getAppName())))
            resetInput()
        }
    }

    private fun inputOfExpectedSize(): Boolean {
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

    private fun getReadableAppName(packageName: String): CharSequence {
        return packageManager.getApplicationLabel(
            packageManager.getApplicationInfo(packageName, 0)
        )
    }

    private fun addToastMessage(string: String) {
        Toast.makeText(this@MainActivity, string, Toast.LENGTH_SHORT).show()
    }

    private fun resetInput() {
        input = ""
    }
}
