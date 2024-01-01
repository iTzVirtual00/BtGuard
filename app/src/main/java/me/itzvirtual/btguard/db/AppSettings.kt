package me.itzvirtual.btguard.db

import kotlinx.serialization.Serializable
import me.itzvirtual.btguard.logic.BluetoothDeviceState

@Serializable
data class AppSettings(
	var settings: Settings = Settings(),
	var devices: MutableList<BluetoothDeviceState> = mutableListOf()
) {
	@Serializable
	data class Settings(var retryDelay: Long = 2000)
}