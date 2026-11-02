package com.example.alex.api.models

import com.google.gson.annotations.SerializedName

data class HistoryItem(
    @SerializedName("id") val id: Long,
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String
)