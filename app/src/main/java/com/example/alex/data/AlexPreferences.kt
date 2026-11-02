package com.example.alex.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.alex.data.models.AlexSpeaker
import com.example.alex.data.models.AppState
import com.example.alex.data.models.Scenario
import com.example.alex.data.models.Schedule
import com.example.alex.data.models.SmartDevice
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore by preferencesDataStore(name = "alex_state")

class AlexPreferences(private val context: Context) {

    companion object {
        private val KEY_STATE = stringPreferencesKey("app_state_json")
        private val KEY_DEVICE_TOKEN = stringPreferencesKey("device_token")
    }

    suspend fun load(): AppState {
        val json = context.dataStore.data.first()[KEY_STATE] ?: return AppState()
        return try {
            parseState(json)
        } catch (e: Exception) {
            AppState()
        }
    }

    suspend fun save(state: AppState) {
        context.dataStore.edit { it[KEY_STATE] = serializeState(state) }
    }

    suspend fun saveDeviceToken(token: String) {
        context.dataStore.edit { it[KEY_DEVICE_TOKEN] = token }
    }

    suspend fun getDeviceToken(): String {
        return context.dataStore.data.first()[KEY_DEVICE_TOKEN] ?: ""
    }

    suspend fun clearDeviceToken() {
        context.dataStore.edit { it.remove(KEY_DEVICE_TOKEN) }
    }

    private fun serializeState(state: AppState): String {
        val root = JSONObject()
        root.put("currentDeviceId", state.currentDeviceId)
        root.put("userName", state.userName)
        root.put("userAddress", state.userAddress)

        val devicesArr = JSONArray()
        state.devices.forEach { d ->
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("name", d.name)
            obj.put("room", d.room)
            obj.put("ssid", d.ssid)
            obj.put("online", d.online)
            obj.put("micOn", d.micOn)

            val sh = JSONArray()
            d.smarthome.forEach { s ->
                sh.put(JSONObject().apply {
                    put("id", s.id); put("name", s.name)
                    put("room", s.room); put("type", s.type); put("on", s.on)
                })
            }
            obj.put("smarthome", sh)

            val sc = JSONArray()
            d.scenarios.forEach { s ->
                sc.put(JSONObject().apply {
                    put("id", s.id); put("name", s.name); put("actions", s.actions)
                })
            }
            obj.put("scenarios", sc)

            val sch = JSONArray()
            d.schedules.forEach { s ->
                sch.put(JSONObject().apply {
                    put("id", s.id); put("time", s.time)
                    put("name", s.name); put("days", s.days); put("on", s.on)
                })
            }
            obj.put("schedules", sch)

            devicesArr.put(obj)
        }
        root.put("devices", devicesArr)
        return root.toString()
    }

    private fun parseState(json: String): AppState {
        val root = JSONObject(json)
        val state = AppState()
        state.currentDeviceId = root.optLong("currentDeviceId", 0L)
        state.userName = root.optString("userName", "")
        state.userAddress = root.optString("userAddress", "")

        val devicesArr = root.optJSONArray("devices") ?: JSONArray()
        for (i in 0 until devicesArr.length()) {
            val o = devicesArr.getJSONObject(i)
            val d = AlexSpeaker(
                id = o.optLong("id"),
                name = o.optString("name"),
                room = o.optString("room"),
                ssid = o.optString("ssid"),
                online = o.optBoolean("online", true),
                micOn = o.optBoolean("micOn", true)
            )
            val sh = o.optJSONArray("smarthome") ?: JSONArray()
            for (j in 0 until sh.length()) {
                val s = sh.getJSONObject(j)
                d.smarthome.add(SmartDevice(
                    id = s.optLong("id"), name = s.optString("name"),
                    room = s.optString("room"), type = s.optString("type", "light"),
                    on = s.optBoolean("on", false)
                ))
            }
            val sc = o.optJSONArray("scenarios") ?: JSONArray()
            for (j in 0 until sc.length()) {
                val s = sc.getJSONObject(j)
                d.scenarios.add(Scenario(
                    id = s.optLong("id"), name = s.optString("name"),
                    actions = s.optString("actions")
                ))
            }
            val sch = o.optJSONArray("schedules") ?: JSONArray()
            for (j in 0 until sch.length()) {
                val s = sch.getJSONObject(j)
                d.schedules.add(Schedule(
                    id = s.optLong("id"), time = s.optString("time"),
                    name = s.optString("name"), days = s.optString("days", "Ежедневно"),
                    on = s.optBoolean("on", true)
                ))
            }
            state.devices.add(d)
        }
        return state
    }
}