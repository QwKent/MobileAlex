package com.example.alex.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class WifiNetwork(
    val ssid: String,
    val level: Int,
    val secured: Boolean,
    val signalBars: Int
)

object WifiScanner {

    private const val TAG = "WifiScanner"

    @Suppress("DEPRECATION")
    fun scan(context: Context): Flow<List<WifiNetwork>> = callbackFlow {
        val appContext = context.applicationContext
        val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        if (wifiManager == null) {
            Log.e(TAG, "WifiManager == null")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        if (!wifiManager.isWifiEnabled) {
            Log.w(TAG, "Wi-Fi выключен")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val results: List<ScanResult> = try {
                    wifiManager.scanResults ?: emptyList()
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException scanResults", e)
                    emptyList()
                }

                val mapped = results
                    .filter { it.SSID.isNotEmpty() }
                    .distinctBy { it.SSID }
                    .map { r ->
                        val bars = WifiManager.calculateSignalLevel(r.level, 4) + 1
                        WifiNetwork(
                            ssid = r.SSID,
                            level = r.level,
                            secured = r.capabilities.contains("WPA") ||
                                    r.capabilities.contains("WEP") ||
                                    r.capabilities.contains("SAE"),
                            signalBars = bars.coerceIn(1, 4)
                        )
                    }
                    .sortedByDescending { it.level }

                Log.d(TAG, "Сетей: ${mapped.size}")
                trySend(mapped)
            }
        }

        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        var registered = false

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                appContext.registerReceiver(receiver, filter)
            }
            registered = true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка registerReceiver", e)
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        try {
            val started = wifiManager.startScan()
            Log.d(TAG, "startScan: $started")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException startScan", e)
            trySend(emptyList())
        }

        awaitClose {
            if (registered) {
                try {
                    appContext.unregisterReceiver(receiver)
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "Ресивер уже отписан", e)
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка unregisterReceiver", e)
                }
            }
        }
    }
}