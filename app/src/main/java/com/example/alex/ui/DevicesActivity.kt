package com.example.alex.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alex.data.DeviceRepository
import com.example.alex.data.models.AlexSpeaker
import com.example.alex.databinding.ActivityDevicesBinding
import com.example.alex.databinding.ItemDeviceListBinding
import kotlinx.coroutines.launch

class DevicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDevicesBinding
    private lateinit var repo: DeviceRepository
    private lateinit var adapter: SpeakerListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = DeviceRepository.get(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, DiscoveryActivity::class.java))
        }

        adapter = SpeakerListAdapter(
            onClick = { speaker -> openDevice(speaker) },
            onLongClick = { speaker -> confirmDelete(speaker) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        lifecycleScope.launch {
            repo.load()
            refresh()
        }
    }

    private fun refresh() {
        val items = repo.appState.value.devices
        binding.count.text = if (items.isEmpty()) "Нет колонок"
        else "${items.size} ${if (items.size == 1) "колонка" else "колонки"}"
        if (items.isEmpty()) {
            binding.empty.visibility = View.VISIBLE
            binding.recycler.visibility = View.GONE
        } else {
            binding.empty.visibility = View.GONE
            binding.recycler.visibility = View.VISIBLE
        }
        adapter.submit(items)
    }

    private fun openDevice(speaker: AlexSpeaker) {
        val intent = Intent(this, DeviceDetailActivity::class.java)
        intent.putExtra("speaker_id", speaker.id)
        startActivity(intent)
    }

    private fun confirmDelete(speaker: AlexSpeaker) {
        AlertDialog.Builder(this)
            .setTitle("Удалить колонку?")
            .setMessage("«${speaker.name}» будет удалена вместе с её сценариями и расписаниями.")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    val state = repo.appState.value
                    state.devices.removeAll { it.id == speaker.id }
                    if (state.currentDeviceId == speaker.id) {
                        state.currentDeviceId = state.devices.firstOrNull()?.id ?: 0L
                    }
                    repo.persist()
                    refresh()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { refresh() }
    }
}

class SpeakerListAdapter(
    private val onClick: (AlexSpeaker) -> Unit,
    private val onLongClick: (AlexSpeaker) -> Unit
) : RecyclerView.Adapter<SpeakerListAdapter.VH>() {

    private val items = mutableListOf<AlexSpeaker>()

    fun submit(list: List<AlexSpeaker>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemDeviceListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class VH(private val b: ItemDeviceListBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: AlexSpeaker) {
            b.deviceName.text = s.name
            b.deviceSub.text = if (s.ssid.isEmpty()) "Wi-Fi не указан" else s.ssid
            b.statusDot.setBackgroundResource(
                if (s.online) com.example.alex.R.drawable.bg_dot_online
                else com.example.alex.R.drawable.bg_dot_offline
            )
            b.statusText.text = if (s.online) "Онлайн" else "Офлайн"
            b.root.setOnClickListener { onClick(s) }
            b.root.setOnLongClickListener { onLongClick(s); true }
        }
    }
}