package me.itzvirtual.btguard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import me.itzvirtual.btguard.databinding.PermissionsActivityBinding

class PermissionActivity : AppCompatActivity() {

	private lateinit var binding: PermissionsActivityBinding
	private var done: Boolean = false
	private var requiredPermissions: MutableList<String> = emptyList<String>().toMutableList();

	private val permissionRequestCode = 1337 // You can choose any code

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		initRequiredPermissions()
		binding = PermissionsActivityBinding.inflate(layoutInflater)
		setContentView(binding.root)
		checkAndRequestPermissions()
	}

	private fun initRequiredPermissions() {
		// bluetooth
		if (Build.VERSION.SDK_INT <= 30) {
			requiredPermissions.add(Manifest.permission.BLUETOOTH)
			requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN)
		} else {
			requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
		}

		requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
		requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

		// notifications
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
		}

		// services
		requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE)
		if (Build.VERSION.SDK_INT >= 34) {
			requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE)
		}

		Log.d("permissions", requiredPermissions.toString())
	}

	private fun checkAndRequestPermissions() {
		done = true
		requiredPermissions.removeIf {
			checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
		}
		if (requiredPermissions.size == 0) {
			done()
			return
		}
		requestPermissions(requiredPermissions.toTypedArray(), permissionRequestCode)
	}

	private fun startNextActivity() {
		val intent = Intent(this, MainActivity::class.java)
		startActivity(intent)
	}


	private fun createPermissionView(permission: String, status: Int): View {
		val textView = TextView(this)
		textView.text = "Permission: $permission\nStatus: ${getStatusString(status)}"

		return textView
	}

	private fun requestPermission(permission: String) {
		ActivityCompat.requestPermissions(
			this,
			arrayOf(permission),
			permissionRequestCode
		)
	}

	private fun getStatusString(status: Int): String {
		return when (status) {
			PackageManager.PERMISSION_GRANTED -> "Granted"
			PackageManager.PERMISSION_DENIED -> "Denied"
			else -> "Unknown"
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		checkAndRequestPermissions()
	}

	private fun done() {
		setResult(RESULT_OK)
		startNextActivity()
		finish()
	}
}
