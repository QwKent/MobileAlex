package com.example.alex.data.models

data class SmartDevice(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val room: String,
    val type: String = "light",
    var on: Boolean = false
)