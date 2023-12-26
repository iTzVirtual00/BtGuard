package me.itzvirtual.btguard.plugins

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.itzvirtual.btguard.MainActivity
import me.itzvirtual.btguard.logic.BluetoothDeviceState
import me.itzvirtual.btguard.logic.PingEvents
import me.itzvirtual.btguard.logic.PingManager

class Persistence(val pingManager: PingManager, val context: MainActivity) : PingEvents {
	init {
		pingManager.eventBus.register(this)
	}

	override fun onDeviceAdded(device: BluetoothDeviceState, insertedAt: Int) {
		saveData()
	}

	override fun onDeviceRemoved(device: BluetoothDeviceState, removedFrom: Int) {
		saveData()
	}

	override fun onDeviceDataChanged(device: BluetoothDeviceState, index: Int) {
		saveData()
	}

	fun saveData() {
		context.settings!!.devices.clear()
		context.settings!!.devices.addAll(pingManager.devices.values)
		val fileOutputStream = context.openFileOutput("devices.json", Context.MODE_PRIVATE)
		fileOutputStream.write(Json.encodeToString(context.settings!!).toByteArray())
		fileOutputStream.close()
	}
}