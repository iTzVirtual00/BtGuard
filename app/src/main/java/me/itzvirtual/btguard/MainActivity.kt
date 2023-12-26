package me.itzvirtual.btguard

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.itzvirtual.btguard.databinding.ActivityDeviceListBinding
import me.itzvirtual.btguard.db.AppSettings
import me.itzvirtual.btguard.logic.BluetoothDeviceState
import me.itzvirtual.btguard.logic.PingManager
import me.itzvirtual.btguard.notifications.Notifications
import me.itzvirtual.btguard.plugins.BleNotificationService
import me.itzvirtual.btguard.plugins.DeviceAdapter
import me.itzvirtual.btguard.plugins.Persistence
import me.itzvirtual.btguard.service.BluetoothWatchtowerService


@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

	private lateinit var deviceAdapter: DeviceAdapter
	private lateinit var binding: ActivityDeviceListBinding
	private lateinit var wtService: BluetoothWatchtowerService
	private lateinit var pingManager: PingManager
	lateinit var settings: AppSettings
	private lateinit var notifications: Notifications

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		notifications = Notifications(this)
		settings = getAppSettings()
		Intent(this, BluetoothWatchtowerService::class.java).also { intent ->
			bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
		}
	}

	private val serviceConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			val binder = service as BluetoothWatchtowerService.LocalBinder
			wtService = binder.getService()
			registerPlugins(wtService)
		}

		override fun onServiceDisconnected(name: ComponentName?) {}
	}

	private fun registerPlugins(wtService: BluetoothWatchtowerService) {
		Log.d("main", "Registering plugins")
		pingManager = wtService.pingManager
		for (device in settings.devices)
			pingManager.addDevice(device)

		Persistence(pingManager, this)

		binding = ActivityDeviceListBinding.inflate(layoutInflater)

		setContentView(binding.root)
		deviceAdapter = DeviceAdapter(pingManager)
		binding.recyclerView.layoutManager = LinearLayoutManager(this)
		binding.recyclerView.adapter = deviceAdapter
		binding.fab.setOnClickListener {
			deviceAdapter.showDialog(it.context)
		}

		val serviceIntent = Intent(this, BleNotificationService::class.java)
		startService(serviceIntent)
	}

	private fun getAppSettings(): AppSettings {
		var settings: AppSettings
		try {
			val fileInputStream = openFileInput("devices.json")
			val data = fileInputStream.bufferedReader().use { it.readText() }
			settings = Json.decodeFromString(data)
		} catch (e: Exception) {
			settings = AppSettings(
				settings = AppSettings.Settings(retryDelay = 2000),
				devices = mutableListOf(BluetoothDeviceState("24:4C:AB:F7:7D:D2", "esp32", 5))
			)
			val fileOutputStream = openFileOutput("devices.json", Context.MODE_PRIVATE)
			fileOutputStream.write(Json.encodeToString(settings).toByteArray())
			fileOutputStream.close()
		}
		return settings
	}
}
