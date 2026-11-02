package com.example.alex.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.alex.databinding.ActivitySmartHomeBinding

class SmartHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivitySmartHomeBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnBack.setOnClickListener { finish() }
    }
}