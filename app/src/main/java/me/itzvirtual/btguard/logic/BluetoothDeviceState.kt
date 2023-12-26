package me.itzvirtual.btguard.logic

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class BluetoothDeviceState(var address: String, var name: String, var maxRetries: Int) {
	@Transient
	var currentRetries: Int = maxRetries + 1

	@Transient
	var online: Boolean = false

	@Transient
	var lastPing: Long = -1
	override fun hashCode(): Int {
		return address.hashCode()
	}

	override fun equals(other: Any?): Boolean {
		return other is BluetoothDeviceState && other.hashCode() == this.hashCode()
	}
}