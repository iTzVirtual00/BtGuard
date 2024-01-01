package me.itzvirtual.btguard.logic

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PingManager(val eventBus: PingEventBus = PingEventBus(), var delay: Long) {
	private val coroutineScope = CoroutineScope(Dispatchers.Default)
	private val mutex = Mutex()

	val devices: HashMap<String, BluetoothDeviceState> = hashMapOf()

	init {
		coroutineScope.launch {
			while (true) {
				val t = System.currentTimeMillis()
				watchdog() // +1
				//delay(delay-System.currentTimeMillis()-t)
				delay(delay)
			}
		}
	}

	fun addDevice(device: BluetoothDeviceState): Boolean {
		val t = devices.size
		devices[device.address] = device
		if (devices.size != t) eventBus.onDeviceAdded(device, devices.values.indexOf(device))
		else eventBus.onDeviceDataChanged(device, devices.values.indexOf(device))
		return true
	}

	fun removeDevice(device: BluetoothDeviceState?): Boolean {
		if (device == null) return false
		if (!devices.containsKey(device.address)) return false
		val removedFrom = devices.values.indexOf(device)
		devices.remove(device.address)
		eventBus.onDeviceRemoved(device, removedFrom)
		return true
	}

	private suspend fun _pingDevice(address: String) {
		val device: BluetoothDeviceState? = devices[address]
		if (device !is BluetoothDeviceState) return
		mutex.withLock {
			found(device)
		}
	}

	fun pingDevice(address: String) {
		Log.d("PingManager", "got ping by $address")
		runBlocking {
			_pingDevice(address)
		}
	}

	private fun found(device: BluetoothDeviceState) {
		Log.d("watchdog", "${device.address} found")
		val justFound = device.currentRetries >= device.maxRetries
		device.currentRetries = 0
		device.online = true
		device.lastPing = System.currentTimeMillis()
		eventBus.onDevicePing(device, justFound)
	}

	private fun retried(device: BluetoothDeviceState) {
		device.currentRetries = minOf(device.currentRetries + 1, device.maxRetries + 1)
		if (device.currentRetries > device.maxRetries) return// already lost
		Log.d("watchdog", "${device.address} retry(${device.currentRetries})")
		if (device.currentRetries == device.maxRetries) lost(device) // just lost
		// not lost yet
	}

	private fun lost(device: BluetoothDeviceState) {
		device.online = false
		device.lastPing = -1
		eventBus.onDeviceLost(device)
	}

	private suspend fun watchdog() {
		mutex.withLock {
			devices.values.forEach { device: BluetoothDeviceState ->
				retried(device)
			}
		}
	}


}