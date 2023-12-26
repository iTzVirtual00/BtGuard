package me.itzvirtual.btguard.plugins

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.Binder
import android.os.IBinder
import android.util.Log
import me.itzvirtual.btguard.logic.BluetoothDeviceState
import me.itzvirtual.btguard.logic.PingEvents
import me.itzvirtual.btguard.notifications.Notifications
import me.itzvirtual.btguard.service.BluetoothWatchtowerService

class BleNotificationService : Service(), PingEvents {
	private val binder: IBinder = LocalBinder()
	private lateinit var wtService: BluetoothWatchtowerService
	private lateinit var notifications: Notifications

	private val serviceType = FOREGROUND_SERVICE_TYPE_SPECIAL_USE

	inner class LocalBinder : Binder() {
		fun getService(): BleNotificationService = this@BleNotificationService
	}

	override fun onBind(p0: Intent?): IBinder {
		return binder
	}

	private val serviceConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			val binder = service as BluetoothWatchtowerService.LocalBinder
			wtService = binder.getService()
			onServiceConnected()
		}

		override fun onServiceDisconnected(name: ComponentName?) {}
	}

	override fun onCreate() {
		super.onCreate()
		Intent(this, BluetoothWatchtowerService::class.java).also { intent ->
			bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
		}
	}

	fun onServiceConnected() {
		Log.d("NotificationService", "registering notification service")
		notifications = Notifications(this)
		wtService.pingManager.eventBus.register(this)
		// notifications.serviceNotification(this)
		startForeground(1337, notifications.serviceNotification(), serviceType)
	}

	override fun onDeviceLost(device: BluetoothDeviceState) {
		notifications.deviceLost(device)
	}

}