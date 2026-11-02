package com.example.alex.api.models

import com.google.gson.annotations.SerializedName

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("checks") val checks: Map<String, String>? = null
)