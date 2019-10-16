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
            2131165220 -> changeAppContent("Google Chrome", "Lang - kort - kort")
            2131165221 -> changeAppContent("Gmail", "Kort - lang - kort")
            2131165223 -> changeAppContent("Phone", "Kort - kort - lang")
        }
    }

    private fun changeAppContent(appName: String, password: String) {
        val viewAppName: TextView = findViewById(R.id.appName)
        viewAppName.text = appName

        val viewPassword: TextView = findViewById(R.id.password)
        viewPassword.text = password
    }
}