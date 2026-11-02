package com.example.alex.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.alex.data.DeviceRepository
import com.example.alex.data.models.AlexSpeaker
import com.example.alex.databinding.ActivityDeviceDetailBinding
import kotlinx.coroutines.launch

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceDetailBinding
    private lateinit var repo: DeviceRepository
    private var speakerId: Long = 0L
    private var speaker: AlexSpeaker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = DeviceRepository.get(this)

        speakerId = intent.getLongExtra("speaker_id", 0L)

        binding.btnBack.setOnClickListener { finish() }
        binding.rowMic.setOnClickListener { toggleMic() }
        binding.rowWifi.setOnClickListener {
            val intent = Intent(this, WifiActivity::class.java)
            intent.putExtra("setup_mode", false)
            startActivity(intent)
        }
        binding.rowHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnForget.setOnClickListener { confirmForget() }

        loadSpeaker()
    }

    private fun loadSpeaker() {
        lifecycleScope.launch {
            repo.load()
            val found = repo.appState.value.devices.find { it.id == speakerId }
            if (found == null) {
                Toast.makeText(this@DeviceDetailActivity, "Колонка не найдена", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            speaker = found
            bind(found)
        }
    }

    private fun bind(s: AlexSpeaker) {
        binding.deviceName.text = s.name
        binding.deviceSub.text = "${s.room} · ${if (s.online) "Онлайн" else "Офлайн"}"
        binding.deviceSsid.text = if (s.ssid.isEmpty()) "Не настроено" else s.ssid
        binding.micState.text = if (s.micOn) "Включён" else "Выключен"
        binding.toggleMic.isChecked = s.micOn
    }

    private fun toggleMic() {
        val s = speaker ?: return
        s.micOn = !s.micOn
        binding.micState.text = if (s.micOn) "Включён" else "Выключен"
        binding.toggleMic.isChecked = s.micOn
        lifecycleScope.launch { repo.updateDevice(s) }
    }

    private fun confirmForget() {
        val s = speaker ?: return
        AlertDialog.Builder(this)
            .setTitle("Забыть колонку?")
            .setMessage("«${s.name}» будет удалена вместе со сценариями и расписаниями.")
            .setPositiveButton("Забыть") { _, _ ->
                lifecycleScope.launch {
                    val state = repo.appState.value
                    state.devices.removeAll { it.id == s.id }
                    if (state.currentDeviceId == s.id) {
                        state.currentDeviceId = state.devices.firstOrNull()?.id ?: 0L
                    }
                    repo.persist()
                    finish()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadSpeaker()
    }
}