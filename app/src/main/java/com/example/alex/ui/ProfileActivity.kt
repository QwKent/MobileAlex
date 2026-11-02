package com.example.alex.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.alex.api.ApiClient
import com.example.alex.api.models.UserUpdateRequest
import com.example.alex.data.DeviceRepository
import com.example.alex.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var repo: DeviceRepository
    private var deviceToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = DeviceRepository.get(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveProfile() }

        lifecycleScope.launch {
            deviceToken = repo.getDeviceToken()

            val state = repo.appState.value
            binding.inputName.setText(state.userName)
            binding.inputAddress.setText(state.userAddress)

            if (deviceToken.isNotEmpty()) {
                loadProfileFromServer()
            }
        }
    }

    private fun loadProfileFromServer() {
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val profile = ApiClient.service.getProfile("Bearer $deviceToken")
                binding.inputName.setText(profile.name ?: "")
                binding.inputAddress.setText(profile.address ?: "")
                repo.setProfile(profile.name ?: "", profile.address ?: "")
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Ошибка загрузки с сервера", e)
            } finally {
                binding.progress.visibility = View.GONE
            }
        }
    }

    private fun saveProfile() {
        val name = binding.inputName.text.toString().trim()
        val address = binding.inputAddress.text.toString().trim()

        lifecycleScope.launch {
            repo.setProfile(name, address)

            if (deviceToken.isEmpty()) {
                Toast.makeText(this@ProfileActivity, "Сохранено локально", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            binding.progress.visibility = View.VISIBLE
            try {
                ApiClient.service.updateProfile(
                    "Bearer $deviceToken",
                    UserUpdateRequest(name = name, address = address)
                )
                Toast.makeText(this@ProfileActivity, "Сохранено на сервере", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Ошибка сохранения на сервер", e)
                Toast.makeText(this@ProfileActivity, "Локально сохранено, сервер недоступен", Toast.LENGTH_LONG).show()
                finish()
            } finally {
                binding.progress.visibility = View.GONE
            }
        }
    }
}