package com.example.alex.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alex.api.ApiClient
import com.example.alex.api.models.HistoryItem
import com.example.alex.data.DeviceRepository
import com.example.alex.databinding.ActivityHistoryBinding
import com.example.alex.databinding.ItemHistoryBinding
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter
    private lateinit var repo: DeviceRepository
    private var deviceToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = DeviceRepository.get(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnClear.setOnClickListener { confirmClear() }

        adapter = HistoryAdapter()
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        lifecycleScope.launch {
            deviceToken = repo.getDeviceToken()
            if (deviceToken.isEmpty()) {
                binding.progress.visibility = View.GONE
                binding.empty.visibility = View.VISIBLE
                binding.emptyText.text = "Токен не задан. Настройте колонку."
                binding.btnClear.visibility = View.GONE
                return@launch
            }
            loadHistory()
        }
    }

    private fun loadHistory() {
        binding.progress.visibility = View.VISIBLE
        binding.empty.visibility = View.GONE
        binding.recycler.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = ApiClient.service.history("Bearer $deviceToken", 50)
                binding.progress.visibility = View.GONE
                if (response.items.isEmpty()) {
                    binding.empty.visibility = View.VISIBLE
                    binding.btnClear.visibility = View.GONE
                } else {
                    binding.recycler.visibility = View.VISIBLE
                    binding.btnClear.visibility = View.VISIBLE
                    adapter.submit(response.items)
                }
            } catch (e: Exception) {
                Log.e("HistoryActivity", "Ошибка загрузки", e)
                binding.progress.visibility = View.GONE
                binding.empty.visibility = View.VISIBLE
                binding.emptyText.text = "Не удалось загрузить: ${e.message}"
                binding.btnClear.visibility = View.GONE
            }
        }
    }

    private fun confirmClear() {
        AlertDialog.Builder(this)
            .setTitle("Очистить историю?")
            .setMessage("Все запросы будут удалены с сервера. Это нельзя отменить.")
            .setPositiveButton("Очистить") { _, _ -> clearHistory() }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun clearHistory() {
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = ApiClient.service.clearHistory("Bearer $deviceToken")
                binding.progress.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(this@HistoryActivity, "История очищена", Toast.LENGTH_SHORT).show()
                    loadHistory()
                } else {
                    Toast.makeText(this@HistoryActivity, "Ошибка: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("HistoryActivity", "Ошибка очистки", e)
                binding.progress.visibility = View.GONE
                Toast.makeText(this@HistoryActivity, "Не удалось очистить: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.VH>() {

    private val items = mutableListOf<HistoryItem>()

    fun submit(list: List<HistoryItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class VH(private val b: ItemHistoryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: HistoryItem) {
            b.historyText.text = item.content.ifEmpty { "—" }
            b.historyRole.text = when (item.role) {
                "user" -> "Вы"
                "assistant" -> "ALEX"
                else -> item.role
            }
            b.historyRole.setTextColor(
                b.root.context.getColor(
                    if (item.role == "user") com.example.alex.R.color.primary
                    else com.example.alex.R.color.success
                )
            )
            b.historyTime.text = item.createdAt
        }
    }
}