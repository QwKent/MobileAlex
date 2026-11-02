package com.example.alex.api.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("hardware_id") val hardwareId: String? = null
)