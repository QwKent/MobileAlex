package com.example.alex.util

import org.json.JSONObject

object JsonUtils {

    fun parseStatus(json: String): Pair<String, String?>? {
        return try {
            val obj = JSONObject(json)
            val status = obj.optString("status", "")
            val reason = obj.optString("reason", "").ifEmpty { null }
            if (status.isEmpty()) null else status to reason
        } catch (e: Exception) {
            null
        }
    }
}