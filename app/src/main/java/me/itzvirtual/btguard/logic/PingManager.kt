package me.itzvirtual.btguard.logic

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PingManager(val eventBus: PingEventBus = PingEventBus(), delay: Long) {
	private var coroutine: Job? = null
	private val coroutineScope = CoroutineScope(Dispatchers.Default)
	private val mutex = Mutex()
	var delay: Long = delay
		set(value) {
			val old = field
			field = value
			eventBus.onDelayChanged(old)
			restartWatchdog()
		}

	val devices: HashMap<String, BluetoothDeviceState> = hashMapOf()

	init {
		restartWatchdog()

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

	private suspend fun _pingDevice(device: BluetoothDeviceState) {
		mutex.withLock {
			found(device)
		}
	}

	fun pingDevice(address: String): Boolean {
		Log.d("PingManager", "got ping by $address")
		val device: BluetoothDeviceState = devices[address] ?: return false
		runBlocking {
			_pingDevice(device)
		}
		return true
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

	private suspend fun watchdogCycle() {
		mutex.withLock {
			devices.values.forEach { device: BluetoothDeviceState ->
				retried(device)
			}
		}
	}

	private fun restartWatchdog() {
		Log.d("watchdog", "watchdog restarted")
		coroutine?.cancel()
		coroutine = coroutineScope.launch {
			while (true) {
				val t = System.currentTimeMillis()
				watchdogCycle() // +1
				//delay(delay-System.currentTimeMillis()-t)
				delay(delay)
			}
		}
	}


}