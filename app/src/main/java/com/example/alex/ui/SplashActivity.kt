package com.example.alex.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.alex.data.DeviceRepository
import com.example.alex.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var repo: DeviceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = DeviceRepository.get(this)

        animateLogo()

        lifecycleScope.launch {
            repo.load()
            delay(1800)
            val state = repo.appState.value
            val next = if (state.devices.isEmpty()) WelcomeActivity::class.java
            else HomeActivity::class.java
            startActivity(Intent(this@SplashActivity, next))
            finish()
        }
    }

    private fun animateLogo() {
        binding.logo.scaleX = 0.5f
        binding.logo.scaleY = 0.5f
        binding.logo.alpha = 0f
        binding.logo.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(700)
            .setInterpolator(OvershootInterpolator(1.5f))
            .start()

        binding.logoText.alpha = 0f
        binding.logoText.translationY = 20f
        binding.logoText.animate()
            .alpha(1f).translationY(0f)
            .setDuration(500)
            .setStartDelay(500)
            .start()
    }
}