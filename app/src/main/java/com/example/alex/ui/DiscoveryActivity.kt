package com.example.alex.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alex.bluetooth.AlexDevice
import com.example.alex.bluetooth.BlePermissions
import com.example.alex.bluetooth.BleState
import com.example.alex.data.DeviceRepository
import com.example.alex.databinding.ActivityDiscoveryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DiscoveryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiscoveryBinding
    private lateinit var repo: DeviceRepository
    private lateinit var adapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiscoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = DeviceRepository.get(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRetry.setOnClickListener { startScan() }
        binding.btnHelp.setOnClickListener {
            Toast.makeText(this, "Проверьте: колонка включена, индикатор мигает синим", Toast.LENGTH_LONG).show()
        }
        binding.btnSimulate.setOnClickListener {
            repo.ble.stopScan()
            val intent = Intent(this, WifiActivity::class.java)
            intent.putExtra("device_name", "ALEX-Setup (BETA)")
            intent.putExtra("device_address", "00:00:00:00:00:00")
            intent.putExtra("simulated", true)
            intent.putExtra("setup_mode", true)
            startActivity(intent)
        }

        adapter = DeviceAdapter { device -> onDeviceSelected(device) }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        if (!BlePermissions.hasAll(this)) {
            BlePermissions.request(this)
        } else {
            startScan()
        }

        observeState()
    }

    private fun startScan() {
        if (!repo.ble.isBluetoothEnabled()) {
            Toast.makeText(this, "Включите Bluetooth", Toast.LENGTH_LONG).show()
            return
        }
        adapter.clear()
        binding.empty.visibility = View.VISIBLE
        repo.ble.startScan()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repo.bleState.collectLatest { s ->
                when (s) {
                    is BleState.Scanning -> {
                        binding.scanLine.visibility = View.VISIBLE
                        binding.empty.visibility = View.VISIBLE
                    }
                    is BleState.DevicesFound -> {
                        binding.scanLine.visibility = View.GONE
                        binding.empty.visibility = if (s.devices.isEmpty()) View.VISIBLE else View.GONE
                        adapter.submit(s.devices)
                    }
                    is BleState.Idle -> {
                        binding.scanLine.visibility = View.GONE
                        binding.empty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                    }
                    is BleState.Error -> {
                        binding.scanLine.visibility = View.GONE
                        Toast.makeText(this@DiscoveryActivity, s.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun onDeviceSelected(device: AlexDevice) {
        repo.ble.stopScan()
        repo.ble.connect(device.device)
        val intent = Intent(this, WifiActivity::class.java)
        intent.putExtra("device_name", device.name)
        intent.putExtra("device_address", device.address)
        intent.putExtra("setup_mode", true)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (BlePermissions.onResult(requestCode, grantResults)) {
            startScan()
        } else {
            Toast.makeText(this, "Разрешения Bluetooth обязательны", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        repo.ble.stopScan()
    }
}