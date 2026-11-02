package com.example.alex.data.models

data class AlexSpeaker(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val room: String,
    val ssid: String,
    var online: Boolean = true,
    var micOn: Boolean = true,
    val smarthome: MutableList<SmartDevice> = mutableListOf(),
    val scenarios: MutableList<Scenario> = mutableListOf(),
    val schedules: MutableList<Schedule> = mutableListOf()
)