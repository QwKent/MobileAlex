package com.example.alex.data

import android.content.Context
import com.example.alex.bluetooth.BleManager
import com.example.alex.bluetooth.BleState
import com.example.alex.data.models.AlexSpeaker
import com.example.alex.data.models.AppState
import com.example.alex.data.models.Scenario
import com.example.alex.data.models.Schedule
import com.example.alex.data.models.SmartDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    val ble: BleManager = BleManager(appContext)
    val bleState: StateFlow<BleState> = ble.state

    private val prefs = AlexPreferences(appContext)

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    suspend fun load() {
        _appState.value = prefs.load()
    }

    suspend fun persist() {
        prefs.save(_appState.value)
    }

    suspend fun saveDeviceToken(token: String) {
        prefs.saveDeviceToken(token)
    }

    suspend fun getDeviceToken(): String {
        return prefs.getDeviceToken()
    }

    suspend fun clearDeviceToken() {
        prefs.clearDeviceToken()
    }

    suspend fun addDevice(device: AlexSpeaker) {
        val s = _appState.value
        s.devices.add(device)
        s.currentDeviceId = device.id
        persist()
        _appState.value = s.copy()
    }

    suspend fun setCurrentDevice(id: Long) {
        val s = _appState.value
        s.currentDeviceId = id
        persist()
        _appState.value = s.copy()
    }

    suspend fun updateDevice(device: AlexSpeaker) {
        val s = _appState.value
        val idx = s.devices.indexOfFirst { it.id == device.id }
        if (idx >= 0) {
            s.devices[idx] = device
            persist()
            _appState.value = s.copy()
        }
    }

    suspend fun setProfile(name: String, address: String) {
        val s = _appState.value
        s.userName = name
        s.userAddress = address
        persist()
        _appState.value = s.copy()
    }

    suspend fun addSmartDevice(device: SmartDevice) {
        val s = _appState.value
        val cur = s.currentDevice() ?: return
        cur.smarthome.add(device)
        persist()
        _appState.value = s.copy()
    }

    suspend fun addScenario(scenario: Scenario) {
        val s = _appState.value
        val cur = s.currentDevice() ?: return
        cur.scenarios.add(scenario)
        persist()
        _appState.value = s.copy()
    }

    suspend fun addSchedule(schedule: Schedule) {
        val s = _appState.value
        val cur = s.currentDevice() ?: return
        cur.schedules.add(schedule)
        persist()
        _appState.value = s.copy()
    }

    suspend fun clearAll() {
        _appState.value = AppState()
        persist()
        prefs.clearDeviceToken()
    }

    companion object {
        @Volatile private var instance: DeviceRepository? = null
        fun get(context: Context): DeviceRepository =
            instance ?: synchronized(this) {
                instance ?: DeviceRepository(context).also { instance = it }
            }
    }
}