package me.itzvirtual.btguard.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import me.itzvirtual.btguard.MainActivity
import me.itzvirtual.btguard.R
import me.itzvirtual.btguard.logic.BluetoothDeviceState

class Notifications(var context: Context) {
	private val notificationManager: NotificationManager =
		context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

	init {
		NotificationChannel("main", "Device lost", NotificationManager.IMPORTANCE_DEFAULT).also {
			it.description = "Sent when a device is lost"
			notificationManager.createNotificationChannel(it)
		}


		NotificationChannel("service", "Service", NotificationManager.IMPORTANCE_HIGH).also {
			it.description = "Permanent notification for background work"
			notificationManager.createNotificationChannel(it)
		}
	}

	fun deviceLost(device: BluetoothDeviceState) {
		val intent = Intent(context, MainActivity::class.java)
		val pendingIntent =
			PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
		val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, "main")
			.setSmallIcon(R.drawable.ic_disconnect)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setContentIntent(pendingIntent)
			.setChannelId("main")
			.setContentTitle(context.getString(me.itzvirtual.btguard.R.string.device_lost_title))
			.setContentText(device.name)
		notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
	}

	fun serviceNotification(): Notification {
		val intent = Intent(context, MainActivity::class.java)
		val pendingIntent =
			PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
		val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, "main")
			.setSmallIcon(R.drawable.ic_disconnect)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT).setContentIntent(pendingIntent)
			.setContentIntent(pendingIntent)
			.setChannelId("service")
			.setContentTitle(context.getString(me.itzvirtual.btguard.R.string.app_name))
			.setContentText(context.getString(me.itzvirtual.btguard.R.string.background_notification))
		return builder.build()
	}


}