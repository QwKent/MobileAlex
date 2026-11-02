package com.example.alex.bluetooth

sealed class BleState {
    object Idle : BleState()
    object Scanning : BleState()
    data class DevicesFound(val devices: List<AlexDevice>) : BleState()
    data class Connecting(val device: AlexDevice) : BleState()
    object Connected : BleState()
    data class WifiSent(val success: Boolean) : BleState()
    data class DeviceStatus(val status: String, val reason: String? = null) : BleState()
    data class Error(val message: String) : BleState()
    object Disconnected : BleState()
}