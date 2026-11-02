package com.example.alex.bluetooth

import java.util.UUID

object BleConstants {
    val SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    val WIFI_CHAR_UUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    val STATUS_CHAR_UUID: UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb")
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    const val DEVICE_NAME_PREFIX = "ALEX-Setup"
    const val SCAN_TIMEOUT_MS = 15000L
    const val CONNECT_TIMEOUT_MS = 10000L
}