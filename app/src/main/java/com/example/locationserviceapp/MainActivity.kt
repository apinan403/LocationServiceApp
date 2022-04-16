package com.example.locationserviceapp

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.locationserviceapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var bindingMainActivity: ActivityMainBinding
    private val TAG = MainActivity::class.java.simpleName
    private val LOCATION_REQUEST_CODE = 34
    lateinit var cacheServiceStat : SharedPreferences
    lateinit var service: LocationBoundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMainActivity.root)

        val intent = Intent(this, LocationService::class.java)
//        val intent = Intent(this, LocationBoundService::class.java)

        if (!checkPermission()) {
            requestPermissions()
        }

        updateButtonsState()

        bindingMainActivity.BtnEnableService.setOnClickListener {
            val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showAlertLocation()
            } else {
//                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                onClickBtn()
                intent.action = LocationService().ACTION_START_FOREGROUND_SERVICE
                startService(intent)
            }
        }

        bindingMainActivity.BtnDisableService.setOnClickListener {
//            unbindService(serviceConnection)
/*            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            messenger?.let {
                val message = Message.obtain(null, LocationBoundService.COMMAND_STOP_LOCATION_SERVICE)
                it.send(message)
            }*/
            onClickBtn()
            intent.action = LocationService().ACTION_STOP_FOREGROUND_SERVICE
            startService(intent)
        }

        LocalBroadcastManager.getInstance(this@MainActivity).registerReceiver(
            mMessageReceiver, IntentFilter("GPSLocationUpdates")
        )
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationBoundService.LocalBinder
            this@MainActivity.service = binder.getService()
        }
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getStringExtra("Status")
            bindingMainActivity.TvLocation.setText(message)
        }
    }

    private fun onClickBtn() {
        bindingMainActivity.BtnEnableService.isEnabled = !bindingMainActivity.BtnEnableService.isEnabled
        bindingMainActivity.BtnDisableService.isEnabled = !bindingMainActivity.BtnDisableService.isEnabled
    }

    private fun showAlertLocation() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Location settings is turn Off")
        dialog.setMessage("Please enable location to use this application")
        dialog.setPositiveButton("Settings") { _, _ ->
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
        }
        dialog.setNegativeButton("Cancel") { _, _ -> }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun updateButtonsState() {
/*        cacheServiceStat = getSharedPreferences(LocationBoundService().servicePerf, Context.MODE_PRIVATE)
        if(cacheServiceStat.getBoolean(LocationBoundService().servicePerf, false)){*/
        cacheServiceStat = getSharedPreferences(LocationService().servicePerf, Context.MODE_PRIVATE)
        if(cacheServiceStat.getBoolean(LocationService().servicePerf, false)){
            bindingMainActivity.BtnEnableService.isEnabled = false
            bindingMainActivity.BtnDisableService.isEnabled = true
            Toast.makeText(baseContext, "Service is already running", Toast.LENGTH_SHORT).show()
        } else {
            bindingMainActivity.BtnEnableService.isEnabled = true
            bindingMainActivity.BtnDisableService.isEnabled = false
            Toast.makeText(baseContext, "There is no service running, starting service..", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fineLocationPermissionState = ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            val backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)

            fineLocationPermissionState == PackageManager.PERMISSION_GRANTED &&
                    backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED
        } else {
            val fineLocationPermissionState = ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            fineLocationPermissionState == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        } else
            Toast.makeText(this@MainActivity, "Location Permission Grant", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Location Permission Grant", Toast.LENGTH_SHORT).show()
            } else {
                Log.i(this@MainActivity.TAG, "User interaction was cancelled.")
                Toast.makeText(this@MainActivity, "Location Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}