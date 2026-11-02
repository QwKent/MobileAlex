package com.example.alex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alex.data.DeviceRepository
import com.example.alex.data.models.Scenario
import com.example.alex.databinding.ActivityScenesBinding
import com.example.alex.databinding.ItemScenarioBinding
import kotlinx.coroutines.launch

class ScenesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScenesBinding
    private lateinit var repo: DeviceRepository
    private lateinit var adapter: ScenarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScenesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = DeviceRepository.get(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAdd.setOnClickListener { showAddDialog() }

        adapter = ScenarioAdapter { scenario -> runScenario(scenario) }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        lifecycleScope.launch {
            repo.load()
            refresh()
        }
    }

    private fun refresh() {
        val cur = repo.appState.value.currentDevice()
        val items = cur?.scenarios ?: emptyList()
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
        val view = layoutInflater.inflate(com.example.alex.R.layout.dialog_scenario, null)
        AlertDialog.Builder(this)
            .setTitle("Новый сценарий")
            .setView(view)
            .setPositiveButton("Создать") { _, _ ->
                val name = view.findViewById<EditText>(com.example.alex.R.id.inputScenName).text.toString().trim()
                val actions = view.findViewById<EditText>(com.example.alex.R.id.inputScenActions).text.toString().trim()
                if (name.isEmpty()) return@setPositiveButton
                lifecycleScope.launch {
                    repo.addScenario(Scenario(name = name, actions = actions))
                    refresh()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun runScenario(scenario: Scenario) {
        AlertDialog.Builder(this)
            .setMessage("Запустить сценарий «${scenario.name}»?")
            .setPositiveButton("Запустить") { _, _ -> }
            .setNegativeButton("Отмена", null)
            .show()
    }
}

class ScenarioAdapter(
    private val onClick: (Scenario) -> Unit
) : RecyclerView.Adapter<ScenarioAdapter.VH>() {

    private val items = mutableListOf<Scenario>()

    fun submit(list: List<Scenario>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemScenarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class VH(private val b: ItemScenarioBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: Scenario) {
            b.scenarioName.text = s.name
            b.scenarioActions.text = s.actions.ifEmpty { "Без действий" }
            b.root.setOnClickListener { onClick(s) }
        }
    }
}