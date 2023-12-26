package me.itzvirtual.btguard.plugins

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import me.itzvirtual.btguard.R
import me.itzvirtual.btguard.databinding.DialogLayoutBinding
import me.itzvirtual.btguard.databinding.ItemDeviceBinding
import me.itzvirtual.btguard.logic.BluetoothDeviceState
import me.itzvirtual.btguard.logic.PingEvents
import me.itzvirtual.btguard.logic.PingManager

class DeviceAdapter(private val pingManager: PingManager) :
	RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>(), PingEvents {

	init {
		pingManager.eventBus.register(this)
	}

	inner class DeviceViewHolder(val binding: ItemDeviceBinding) :
		RecyclerView.ViewHolder(binding.root)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
		val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return DeviceViewHolder(binding)
	}

	override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
		val device = pingManager.devices.values.elementAt(position)

		with(holder.binding) {
			deviceAddress.text = device.address
			deviceStatus.text = if (device.online) "Online" else "Offline"
		}
		holder.itemView.setOnClickListener {
			showDialog(it.context, device)
		}
	}

	override fun getItemCount(): Int {
		return pingManager.devices.size
	}

	override fun onDevicePing(device: BluetoothDeviceState, justFound: Boolean) {
		Log.d("DeviceAdapter", "onDevicePing")
		if (!justFound) return;
		runOnUiThread {
			notifyItemChanged(pingManager.devices.values.indexOf(device))
		}
	}

	override fun onDeviceLost(device: BluetoothDeviceState) {
		Log.d("DeviceAdapter", "onDeviceLost")
		runOnUiThread {
			notifyItemChanged(pingManager.devices.values.indexOf(device))
		}
	}

	override fun onDeviceAdded(device: BluetoothDeviceState, insertedAt: Int) {
		Log.d("DeviceAdapter", "onDeviceAdded")
		runOnUiThread {
			notifyItemInserted(insertedAt)
		}
	}

	override fun onDeviceRemoved(device: BluetoothDeviceState, removedFrom: Int) {
		Log.d("DeviceAdapter", "onDeviceRemoved")
		runOnUiThread {
			notifyItemRemoved(removedFrom)
		}
	}

	fun showDialog(context: Context, device: BluetoothDeviceState? = null) {
		val dialogView = DialogLayoutBinding.inflate(LayoutInflater.from(context))
		val dialogBuilder = AlertDialog.Builder(context)
			.setView(dialogView.root)
			.setTitle(
				if (device == null) context.getString(R.string.add_device) else context.getString(
					R.string.edit_device
				)
			)

		dialogView.editDeviceName.setText(device?.name ?: "")
		dialogView.editDeviceAddress.setText(device?.address ?: "")
		dialogView.editMaxRetries.setText(device?.maxRetries?.toString() ?: "5")

		dialogBuilder.setPositiveButton("OK") { _, _ ->
			val tempDevice = device?.copy() ?: BluetoothDeviceState("", "", 0)
			tempDevice.let {
				it.address = dialogView.editDeviceAddress.text.toString()
				it.name = dialogView.editDeviceName.text.toString()
				it.maxRetries = maxOf(3, dialogView.editMaxRetries.text.toString().toInt())
			}
			if (tempDevice.address != device?.address) pingManager.removeDevice(device)
			pingManager.addDevice(tempDevice) // "exists" check not needed
		}

		dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
			dialog.dismiss()
		}

		if (device != null)
			dialogBuilder.setNeutralButton("Delete") { _, _ ->
				pingManager.removeDevice(device)
			}

		val alertDialog = dialogBuilder.create()
		alertDialog.show()
	}

	fun runOnUiThread(action: Runnable) {
		if (Thread.currentThread() == Looper.getMainLooper().thread) {
			// If already on the main thread, execute the action immediately
			action.run()
		} else {
			// If not on the main thread, post the action to be executed on the main thread
			Handler(Looper.getMainLooper()).post(action)
		}
	}

}
