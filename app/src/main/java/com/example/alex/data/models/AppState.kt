package com.example.alex.data.models

data class AppState(
    val devices: MutableList<AlexSpeaker> = mutableListOf(),
    var currentDeviceId: Long = 0L,
    var userName: String = "",
    var userAddress: String = ""
) {
    fun currentDevice(): AlexSpeaker? =
        devices.find { it.id == currentDeviceId }
}