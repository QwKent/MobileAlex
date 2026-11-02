package com.example.alex.data.models

data class Schedule(
    val id: Long = System.currentTimeMillis(),
    val time: String,
    val name: String,
    val days: String = "Ежедневно",
    var on: Boolean = true
)