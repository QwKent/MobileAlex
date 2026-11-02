package com.example.alex.data.models

data class Scenario(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val actions: String = ""
)