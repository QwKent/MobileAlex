package com.example.alex.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.alex.databinding.ActivityStatusBinding

class StatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGo.setOnClickListener {
            try {
                startActivity(Intent(this, HomeActivity::class.java))
                finishAffinity()
            } catch (e: Exception) {
                Log.e("StatusActivity", "Ошибка перехода в Home", e)
            }
        }
    }
}