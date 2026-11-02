package com.example.alex.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alex.api.ApiClient
import com.example.alex.api.models.RegisterRequest
import com.example.alex.data.DeviceRepository
import com.example.alex.data.WifiCredentials
import com.example.alex.data.models.AlexSpeaker
import com.example.alex.databinding.ActivityWifiBinding
import com.example.alex.util.WifiNetwork
import com.example.alex.util.WifiScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WifiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWifiBinding
    private lateinit var repo: DeviceRepository
    private lateinit var adapter: WifiAdapter

    private var simulated = false
    private var setupMode = true
    private var scanJob: Job? = null
    private var selectedSsid: String = ""

    companion object {
        private const val LOCATION_REQ = 2001
        private const val TAG = "WifiActivity"
        private const val REGISTRATION_SECRET = "88128812"
    }

    private val roomLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val room = result.data?.getStringExtra("room") ?: "Дом"
            registerDeviceAndCreate(room)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = DeviceRepository.get(this)
        simulated = intent.getBooleanExtra("simulated", false)
        setupMode = intent.getBooleanExtra("setup_mode", true)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnRetry.setOnClickListener { scanWifi() }

        adapter = WifiAdapter { network -> onWifiSelected(network) }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        if (hasLocationPermission()) {
            scanWifi()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQ
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun scanWifi() {
        scanJob?.cancel()
        binding.scanLine.visibility = View.VISIBLE
        binding.empty.visibility = View.GONE
        adapter.clear()

        scanJob = lifecycleScope.launch {
            try {
                WifiScanner.scan(this@WifiActivity).collect { list ->
                    Log.d(TAG, "Найдено сетей: ${list.size}")
                    binding.scanLine.visibility = View.GONE
                    if (list.isEmpty()) {
                        binding.empty.visibility = View.VISIBLE
                    } else {
                        binding.empty.visibility = View.GONE
                        adapter.submit(list)
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка сканирования", e)
                binding.scanLine.visibility = View.GONE
                binding.empty.visibility = View.VISIBLE
            }
        }
    }

    private fun onWifiSelected(network: WifiNetwork) {
        if (network.secured) {
            showPasswordDialog(network)
        } else {
            selectedSsid = network.ssid
            sendWifi(network.ssid, "")
        }
    }

    private fun showPasswordDialog(network: WifiNetwork) {
        val view = layoutInflater.inflate(com.example.alex.R.layout.dialog_password, null)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(network.ssid)
            .setView(view)
            .setPositiveButton("Подключить") { _, _ ->
                val pwd = view.findViewById<android.widget.EditText>(
                    com.example.alex.R.id.pwdInput
                ).text.toString()
                selectedSsid = network.ssid
                sendWifi(network.ssid, pwd)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun sendWifi(ssid: String, password: String) {
        if (!simulated) {
            repo.ble.sendWifi(WifiCredentials(ssid, password))
        }
        finishSetup(ssid)
    }

    private fun finishSetup(ssid: String) {
        if (setupMode) {
            roomLauncher.launch(Intent(this, RoomActivity::class.java))
        } else {
            finish()
        }
    }

    private fun registerDeviceAndCreate(room: String) {
        binding.scanLine.visibility = View.VISIBLE

        lifecycleScope.launch {
            var token: String? = null

            try {
                val response = ApiClient.service.registerDevice(
                    secret = REGISTRATION_SECRET,
                    request = RegisterRequest(
                        name = "ALEX · $room",
                        hardwareId = null
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        token = body.token
                        repo.saveDeviceToken(body.token)
                        Log.d(TAG, "Токен получен и сохранён")
                    }
                } else {
                    Log.e(TAG, "Ошибка регистрации: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Сеть недоступна при регистрации", e)
            }

            binding.scanLine.visibility = View.GONE

            val dev = AlexSpeaker(
                name = "ALEX · $room",
                room = room,
                ssid = selectedSsid,
                online = true
            )
            repo.addDevice(dev)

            if (token == null) {
                Toast.makeText(
                    this@WifiActivity,
                    "Колонка добавлена, но токен не получен. Проверьте сеть.",
                    Toast.LENGTH_LONG
                ).show()
            }

            startActivity(Intent(this@WifiActivity, StatusActivity::class.java))
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQ) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanWifi()
            } else {
                Toast.makeText(
                    this,
                    "Без разрешения на геолокацию Android не даёт сканировать Wi-Fi",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanJob?.cancel()
    }
}