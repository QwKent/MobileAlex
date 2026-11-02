package com.example.alex.api.models

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("device_id") val deviceId: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("token") val token: String,
    @SerializedName("rotated") val rotated: Boolean = false
)