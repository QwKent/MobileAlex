package com.example.alex.api.models

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)