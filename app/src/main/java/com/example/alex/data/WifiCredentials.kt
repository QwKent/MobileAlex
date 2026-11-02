package com.example.alex.data

data class WifiCredentials(
    val ssid: String,
    val password: String
) {
    fun toJson(): String {
        val escapedSsid = ssid.replace("\\", "\\\\").replace("\"", "\\\"")
        val escapedPassword = password.replace("\\", "\\\\").replace("\"", "\\\"")
        return """{"ssid":"$escapedSsid","password":"$escapedPassword"}"""
    }
}