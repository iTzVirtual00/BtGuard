package me.itzvirtual.btguard.plugins

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import me.itzvirtual.btguard.databinding.SettingsDialogLayoutBinding
import me.itzvirtual.btguard.logic.PingManager

class SettingsDialog(private val pingManager: PingManager) {

	fun showDialog(context: Context) {
		val dialogView = SettingsDialogLayoutBinding.inflate(LayoutInflater.from(context))
		val dialogBuilder = AlertDialog.Builder(context)
			.setView(dialogView.root)
			.setTitle(
				"Settings"
			)

		dialogView.editRetryDelay.setText(pingManager.delay.toString())

		dialogBuilder.setPositiveButton("OK") { _, _ ->
			pingManager.delay = dialogView.editRetryDelay.text.toString().toLong()
		}

		dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
			dialog.dismiss()
		}

		dialogBuilder.create().show()
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
