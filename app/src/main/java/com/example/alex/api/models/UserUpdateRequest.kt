package com.example.alex.api.models

import com.google.gson.annotations.SerializedName

data class UserUpdateRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("address") val address: String? = null
)