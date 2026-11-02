package com.example.alex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alex.data.DeviceRepository
import com.example.alex.data.models.Schedule
import com.example.alex.databinding.ActivitySchedulesBinding
import com.example.alex.databinding.ItemScheduleBinding
import kotlinx.coroutines.launch

class SchedulesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySchedulesBinding
    private lateinit var repo: DeviceRepository
    private lateinit var adapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySchedulesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = DeviceRepository.get(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAdd.setOnClickListener { showAddDialog() }

        adapter = ScheduleAdapter(
            onToggle = { schedule, isOn ->
                lifecycleScope.launch {
                    schedule.on = isOn
                    val cur = repo.appState.value.currentDevice() ?: return@launch
                    repo.updateDevice(cur)
                }
            },
            onLongClick = { schedule -> confirmDelete(schedule) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        lifecycleScope.launch {
            repo.load()
            refresh()
        }
    }

    private fun refresh() {
        val items = repo.appState.value.currentDevice()?.schedules ?: emptyList()
        if (items.isEmpty()) {
            binding.empty.visibility = View.VISIBLE
            binding.recycler.visibility = View.GONE
        } else {
            binding.empty.visibility = View.GONE
            binding.recycler.visibility = View.VISIBLE
        }
        adapter.submit(items)
    }

    private fun showAddDialog() {
        val view = layoutInflater.inflate(com.example.alex.R.layout.dialog_schedule, null)
        AlertDialog.Builder(this)
            .setTitle("Новое расписание")
            .setView(view)
            .setPositiveButton("Создать") { _, _ ->
                val time = view.findViewById<EditText>(com.example.alex.R.id.inputTime).text.toString().trim()
                val name = view.findViewById<EditText>(com.example.alex.R.id.inputName).text.toString().trim()
                val days = view.findViewById<EditText>(com.example.alex.R.id.inputDays).text.toString().trim()
                if (time.isEmpty() || name.isEmpty()) return@setPositiveButton
                lifecycleScope.launch {
                    repo.addSchedule(Schedule(
                        time = time,
                        name = name,
                        days = days.ifEmpty { "Ежедневно" }
                    ))
                    refresh()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDelete(schedule: Schedule) {
        AlertDialog.Builder(this)
            .setTitle("Удалить расписание?")
            .setMessage("«${schedule.time} · ${schedule.name}» будет удалено.")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    val cur = repo.appState.value.currentDevice() ?: return@launch
                    cur.schedules.removeAll { it.id == schedule.id }
                    repo.updateDevice(cur)
                    refresh()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}

class ScheduleAdapter(
    private val onToggle: (Schedule, Boolean) -> Unit,
    private val onLongClick: (Schedule) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.VH>() {

    private val items = mutableListOf<Schedule>()

    fun submit(list: List<Schedule>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class VH(private val b: ItemScheduleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: Schedule) {
            b.scheduleTime.text = s.time
            b.scheduleName.text = s.name
            b.scheduleDays.text = s.days
            b.toggleSchedule.setOnCheckedChangeListener(null)
            b.toggleSchedule.isChecked = s.on
            b.toggleSchedule.setOnCheckedChangeListener { _, isOn -> onToggle(s, isOn) }
            b.root.setOnLongClickListener { onLongClick(s); true }
        }
    }
}