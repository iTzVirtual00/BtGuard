package me.itzvirtual.btguard.logic

open class PingEventBus : PingEvents {
	private val listeners = mutableListOf<PingEvents>()

	fun register(listener: PingEvents) {
		listeners.add(listener)
	}

	fun unregister(listener: PingEvents) {
		listeners.remove(listener)
	}

	override fun onDeviceLost(device: BluetoothDeviceState) {
		for (listener in listeners) {
			listener.onDeviceLost(device)
		}
	}

	override fun onDevicePing(device: BluetoothDeviceState, justFound: Boolean) {
		for (listener in listeners) {
			listener.onDevicePing(device, justFound)
		}
	}

	override fun onDeviceAdded(device: BluetoothDeviceState, insertedAt: Int) {
		for (listener in listeners) {
			listener.onDeviceAdded(device, insertedAt)
		}
	}

	override fun onDeviceRemoved(device: BluetoothDeviceState, removedFrom: Int) {
		for (listener in listeners) {
			listener.onDeviceRemoved(device, removedFrom)
		}
	}

	override fun onDeviceDataChanged(device: BluetoothDeviceState, index: Int) {
		for (listener in listeners) {
			listener.onDeviceDataChanged(device, index)
		}
	}

	override fun onDelayChanged(oldDelay: Long) {
		for (listener in listeners) {
			listener.onDelayChanged(oldDelay)
		}
	}
}
