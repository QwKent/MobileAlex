package com.example.alex.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.alex.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            startActivity(Intent(this, DiscoveryActivity::class.java))
        }

        animateIn()
    }

    private fun animateIn() {
        binding.logo.alpha = 0f
        binding.logo.scaleX = 0.5f
        binding.logo.scaleY = 0.5f
        binding.logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .setStartDelay(100)
            .start()

        val delay = 300L
        val views = listOf<View>(binding.title, binding.subtitle, binding.btnStart)
        views.forEachIndexed { i, v ->
            v.alpha = 0f
            v.translationY = 30f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(delay + i * 120L)
                .start()
        }
    }
}