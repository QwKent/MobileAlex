package com.example.alex.api.models

import com.google.gson.annotations.SerializedName

data class HistoryResponse(
    @SerializedName("items") val items: List<HistoryItem>,
    @SerializedName("next_before_id") val nextBeforeId: Long? = null
)