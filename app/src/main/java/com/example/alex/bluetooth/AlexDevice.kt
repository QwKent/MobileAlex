package com.example.alex.bluetooth

import android.bluetooth.BluetoothDevice

data class AlexDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val device: BluetoothDevice
) {
    val signalLevel: Int
        get() = when {
            rssi >= -60 -> 4
            rssi >= -70 -> 3
            rssi >= -80 -> 2
            else -> 1
        }
}