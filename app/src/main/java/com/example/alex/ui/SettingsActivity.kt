package com.example.alex.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.alex.data.DeviceRepository
import com.example.alex.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var repo: DeviceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = DeviceRepository.get(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.rowWifi.setOnClickListener {
            startActivity(Intent(this, WifiActivity::class.java).apply {
                putExtra("setup_mode", false)
            })
        }
        binding.rowProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.rowDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("Удалить все данные и колонки?")
                .setPositiveButton("Удалить") { _, _ ->
                    lifecycleScope.launch {
                        repo.clearAll()
                        startActivity(Intent(this@SettingsActivity, WelcomeActivity::class.java))
                        finishAffinity()
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }
}