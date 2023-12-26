package me.itzvirtual.btguard.service

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import me.itzvirtual.btguard.logic.PingEvents
import me.itzvirtual.btguard.logic.PingManager

@SuppressLint("MissingPermission")
class BluetoothWatchtowerService : Service(), PingEvents {

	private val binder: IBinder = LocalBinder()
	private lateinit var bluetoothLeScanner: BluetoothLeScanner
	lateinit var pingManager: PingManager

	override fun onCreate() {
		super.onCreate()
		Log.d("Service", "onCreate")
		val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
		bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner
		pingManager = PingManager()
		pingManager.eventBus.register(this)
		startBleScan()
	}

	private val scanCallback = object : ScanCallback() {
		override fun onScanResult(callbackType: Int, result: ScanResult?) {
			if (result != null) {
				pingManager.pingDevice(result.device.address)
			}
		}
	}

	private fun startBleScan() {
		Log.d("Service", "scan started")
		// bluetoothLeScanner.startScan(scanCallback)
		bluetoothLeScanner.startScan(
			listOf(ScanFilter.Builder().build()),
			ScanSettings.Builder().setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
				.setMatchMode(ScanSettings.MATCH_MODE_STICKY).build(),
			scanCallback
		)
	}

	private fun stopBleScan() {
		bluetoothLeScanner.stopScan(scanCallback)
	}

	inner class LocalBinder : Binder() {
		fun getService(): BluetoothWatchtowerService = this@BluetoothWatchtowerService
	}

	override fun onBind(intent: Intent?): IBinder {
		return binder
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		return START_STICKY
	}
}
