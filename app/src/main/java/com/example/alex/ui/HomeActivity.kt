package com.example.alex.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.alex.api.ApiClient
import com.example.alex.data.DeviceRepository
import com.example.alex.data.models.AlexSpeaker
import com.example.alex.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var repo: DeviceRepository
    private lateinit var carouselAdapter: SpeakerCarouselAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(binding.root)

            repo = DeviceRepository.get(this)

            binding.btnSettings.setOnClickListener {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            binding.btnMenu.setOnClickListener {
                startActivity(Intent(this, DevicesActivity::class.java))
            }
            binding.cardMic.setOnClickListener { toggleMic() }
            binding.cardMusic.setOnClickListener { toggleMusic() }
            binding.cardAlarm.setOnClickListener {
                startActivity(Intent(this, SchedulesActivity::class.java))
            }
            binding.rowScenes.setOnClickListener {
                startActivity(Intent(this, ScenesActivity::class.java))
            }
            binding.rowDevices.setOnClickListener {
                startActivity(Intent(this, SchedulesActivity::class.java))
            }

            carouselAdapter = SpeakerCarouselAdapter { speaker -> onSpeakerClick(speaker) }
            binding.carousel.adapter = carouselAdapter
            binding.carousel.offscreenPageLimit = 1
            binding.carousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val s = repo.appState.value.devices.getOrNull(position) ?: return
                    lifecycleScope.launch { repo.setCurrentDevice(s.id) }
                    updateSpeakerUI(s)
                }
            })

            lifecycleScope.launch {
                try {
                    repo.load()
                    val state = repo.appState.value
                    if (state.devices.isEmpty()) {
                        startActivity(Intent(this@HomeActivity, WelcomeActivity::class.java))
                        finish()
                        return@launch
                    }
                    carouselAdapter.submit(state.devices)
                    val idx = state.devices.indexOfFirst { it.id == state.currentDeviceId }.coerceAtLeast(0)
                    binding.carousel.setCurrentItem(idx, false)
                    updateGreeting()
                    updateSpeakerUI(state.devices[idx])
                    checkServerHealth()
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Ошибка загрузки state", e)
                }
            }
        } catch (e: Exception) {
            Log.e("HomeActivity", "FATAL в onCreate", e)
            e.printStackTrace()
        }
    }

    private fun checkServerHealth() {
        binding.serverStatus.visibility = View.VISIBLE
        binding.serverStatus.text = "Проверяем сервер…"
        binding.serverStatus.setTextColor(getColor(com.example.alex.R.color.text_secondary))

        lifecycleScope.launch {
            try {
                val response = ApiClient.service.health()
                if (response.status == "ok") {
                    binding.serverStatus.text = "Сервер онлайн"
                    binding.serverStatus.setTextColor(getColor(com.example.alex.R.color.success))
                } else {
                    binding.serverStatus.text = "Сервер: ${response.status}"
                    binding.serverStatus.setTextColor(getColor(com.example.alex.R.color.warning))
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Сервер недоступен", e)
                binding.serverStatus.text = "Сервер офлайн"
                binding.serverStatus.setTextColor(getColor(com.example.alex.R.color.error))
            }
        }
    }

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val hello = when {
            hour < 6 -> "Доброй ночи"
            hour < 12 -> "Доброе утро"
            hour < 18 -> "Добрый день"
            else -> "Добрый вечер"
        }
        val name = repo.appState.value.userName
        binding.greeting.text = if (name.isNotEmpty()) "$hello, $name" else hello
    }

    private fun updateSpeakerUI(speaker: AlexSpeaker) {
        binding.greetingSub.text = speaker.name

        binding.micState.text = if (speaker.micOn) "Включён" else "Выключен"
        binding.cardMic.setBackgroundResource(
            if (speaker.micOn) com.example.alex.R.drawable.bg_card_active
            else com.example.alex.R.drawable.bg_card
        )

        val schedules = speaker.schedules
        when {
            schedules.isEmpty() -> {
                binding.alarmState.text = "Нет"
                binding.cardAlarm.setBackgroundResource(com.example.alex.R.drawable.bg_card)
            }
            schedules.any { it.on } -> {
                binding.alarmState.text = "Включены"
                binding.cardAlarm.setBackgroundResource(com.example.alex.R.drawable.bg_card_active)
            }
            else -> {
                binding.alarmState.text = "Выключены"
                binding.cardAlarm.setBackgroundResource(com.example.alex.R.drawable.bg_card)
            }
        }
    }

    private fun toggleMic() {
        val state = repo.appState.value
        val cur = state.currentDevice() ?: return
        cur.micOn = !cur.micOn
        lifecycleScope.launch {
            repo.updateDevice(cur)
            updateSpeakerUI(cur)
        }
    }

    private fun toggleMusic() {
        val playing = binding.musicState.text == "Играет"
        binding.musicState.text = if (playing) "Выключена" else "Играет"
        binding.cardMusic.setBackgroundResource(
            if (!playing) com.example.alex.R.drawable.bg_card_active
            else com.example.alex.R.drawable.bg_card
        )
    }

    private fun onSpeakerClick(speaker: AlexSpeaker) {
        val intent = Intent(this, DeviceDetailActivity::class.java)
        intent.putExtra("speaker_id", speaker.id)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            try {
                repo.load()
                val state = repo.appState.value
                if (state.devices.isNotEmpty()) {
                    carouselAdapter.submit(state.devices)
                    val idx = state.devices.indexOfFirst { it.id == state.currentDeviceId }.coerceAtLeast(0)
                    binding.carousel.setCurrentItem(idx, false)
                    updateGreeting()
                    updateSpeakerUI(state.devices[idx])
                    checkServerHealth()
                } else {
                    startActivity(Intent(this@HomeActivity, WelcomeActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Ошибка в onResume", e)
            }
        }
    }
}