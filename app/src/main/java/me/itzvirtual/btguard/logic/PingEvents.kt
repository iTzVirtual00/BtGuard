package me.itzvirtual.btguard.logic

interface PingEvents {
	fun onDeviceLost(device: BluetoothDeviceState) {}
	fun onDevicePing(device: BluetoothDeviceState, justFound: Boolean) {}
	fun onDeviceAdded(device: BluetoothDeviceState, insertedAt: Int) {}
	fun onDeviceRemoved(device: BluetoothDeviceState, removedFrom: Int) {}
	fun onDeviceDataChanged(device: BluetoothDeviceState, index: Int) {}
	fun onDelayChanged(oldDelay: Long) {}
}
