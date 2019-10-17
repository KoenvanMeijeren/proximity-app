package com.jansenenco.proximity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class AppActivity : AppCompatActivity() {
    private var buttonID: Int = 0
    private lateinit var buttonBack: Button

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
            2131165220 -> changeAppContent("Google Chrome", ". . .")
            2131165223 -> changeAppContent("Mail", "_ . .")
            2131165225 -> changeAppContent("Telefoon", ". _ .")
            2131165228 -> changeAppContent("Whatsapp", ". . _")
            2131165221 -> changeAppContent("Dumpert", "_ _ _")
            2131165226 -> changeAppContent("Instellingen", ". _ _")
            2131165222 -> changeAppContent("Galerij", "_ . _")
            2131165227 -> changeAppContent("Snapchat", "_ _ .")
        }
    }

    private fun changeAppContent(appName: String, password: String) {
        val viewAppName: TextView = findViewById(R.id.appName)
        viewAppName.text = appName

        val viewPassword: TextView = findViewById(R.id.password)
        viewPassword.text = password
    }
}