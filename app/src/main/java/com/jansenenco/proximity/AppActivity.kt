package com.jansenenco.proximity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class AppActivity : AppCompatActivity() {

    internal var buttonID: Int = 0
    internal lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        buttonID = intent.getIntExtra("id", 0)
        changeAppViewBasedOnId()

        buttonBack = findViewById(R.id.buttonBack)
        buttonBack.setOnClickListener{
            finish()
        }
    }

    private fun changeAppViewBasedOnId() {
        when (buttonID) {
            2131165221 -> changeAppContent("Google Chrome", "2 tot 4 cm")
            2131165222 -> changeAppContent("Gmail", "5 tot 7 cm")
            2131165224 -> changeAppContent("Phone", "8 tot 10 cm")
        }
    }

    private fun changeAppContent(appName: String, password: String) {
        val viewAppName: TextView = findViewById(R.id.appName)
        viewAppName.text = appName

        val viewPassword: TextView = findViewById(R.id.password)
        viewPassword.text = password
    }
}