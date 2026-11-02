package com.example.alex.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.alex.data.WifiCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BleManager(private val context: Context) {

    private val tag = "BleManager"

    private val _state = MutableStateFlow<BleState>(BleState.Idle)
    val state: StateFlow<BleState> = _state.asStateFlow()

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var gatt: BluetoothGatt? = null
    private var wifiCharacteristic: BluetoothGattCharacteristic? = null
    private var statusCharacteristic: BluetoothGattCharacteristic? = null

    private val foundDevices = mutableMapOf<String, AlexDevice>()
    private val handler = Handler(Looper.getMainLooper())

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = device.name ?: return
            if (!name.startsWith(BleConstants.DEVICE_NAME_PREFIX)) return

            val alexDevice = AlexDevice(
                name = name,
                address = device.address,
                rssi = result.rssi,
                device = device
            )
            foundDevices[device.address] = alexDevice
            _state.value = BleState.DevicesFound(foundDevices.values.toList())
        }

        override fun onScanFailed(errorCode: Int) {
            _state.value = BleState.Error("Ошибка сканирования: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                g.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _state.value = BleState.Disconnected
                gatt?.close()
                gatt = null
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _state.value = BleState.Error("Не удалось найти сервисы")
                return
            }
            val service = g.getService(BleConstants.SERVICE_UUID)
            if (service == null) {
                _state.value = BleState.Error("Сервис не найден")
                return
            }
            val wifiCh = service.getCharacteristic(BleConstants.WIFI_CHAR_UUID)
            val statusCh = service.getCharacteristic(BleConstants.STATUS_CHAR_UUID)

            if (wifiCh == null) {
                _state.value = BleState.Error("Характеристика Wi-Fi не найдена")
                return
            }
            wifiCharacteristic = wifiCh
            statusCharacteristic = statusCh

            if (statusCh != null) {
                g.setCharacteristicNotification(statusCh, true)
                val descriptor = statusCh.getDescriptor(BleConstants.CCCD_UUID)
                if (descriptor != null) {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    g.writeDescriptor(descriptor)
                }
            }
            _state.value = BleState.Connected
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == BleConstants.STATUS_CHAR_UUID) {
                val status = String(value, Charsets.UTF_8)
                parseStatus(status)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            val value = characteristic.value ?: return
            onCharacteristicChanged(g, characteristic, value)
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (characteristic.uuid == BleConstants.WIFI_CHAR_UUID) {
                _state.value = BleState.WifiSent(status == BluetoothGatt.GATT_SUCCESS)
            }
        }
    }

    private fun parseStatus(json: String) {
        try {
            val obj = org.json.JSONObject(json)
            val status = obj.optString("status", "")
            val reason = obj.optString("reason", "")
            _state.value = BleState.DeviceStatus(status, reason.ifEmpty { null })
        } catch (e: Exception) {
            Log.e(tag, "Bad status JSON: $json", e)
        }
    }

    fun startScan() {
        val scanner = adapter?.bluetoothLeScanner
        if (scanner == null) {
            _state.value = BleState.Error("Bluetooth недоступен")
            return
        }
        foundDevices.clear()
        _state.value = BleState.Scanning

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(null, settings, scanCallback)

        handler.postDelayed({
            scanner.stopScan(scanCallback)
            val list = foundDevices.values.toList()
            _state.value = if (list.isEmpty()) BleState.Idle else BleState.DevicesFound(list)
        }, BleConstants.SCAN_TIMEOUT_MS)
    }

    fun stopScan() {
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    fun connect(device: BluetoothDevice) {
        _state.value = BleState.Connecting(
            AlexDevice(
                name = device.name ?: "",
                address = device.address,
                rssi = 0,
                device = device
            )
        )
        gatt = device.connectGatt(context, false, gattCallback)
    }

    fun sendWifi(credentials: WifiCredentials) {
        val g = gatt ?: run {
            _state.value = BleState.Error("Нет подключения")
            return
        }
        val ch = wifiCharacteristic ?: run {
            _state.value = BleState.Error("Характеристика Wi-Fi не найдена")
            return
        }
        val data = credentials.toJson().toByteArray(Charsets.UTF_8)
        ch.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        @Suppress("DEPRECATION")
        ch.value = data
        @Suppress("DEPRECATION")
        g.writeCharacteristic(ch)
    }

    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _state.value = BleState.Disconnected
    }

    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true
}