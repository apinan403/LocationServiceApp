//package com.example.locationserviceapp
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.ActivityManager
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.location.Address
//import android.location.Geocoder
//import android.location.LocationManager
//import android.os.Build
//import android.os.Bundle
//import android.provider.Settings
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import com.example.locationserviceapp.databinding.ActivityMainBinding
//import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.location.*
//import java.util.*
//
//open class MainActivity : AppCompatActivity() {
//    lateinit var bindingMainActivity: ActivityMainBinding
//    private val TAG = MainActivity::class.java.simpleName
//    private val LOCATION_REQUEST_CODE = 34
//    private lateinit var mFusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//    private lateinit var mLocationRequest: LocationRequest
//
//    //    private val UPDATE_INTERVAL: Long = 60000 // Every 60 seconds.
////    private val MAX_WAIT_TIME : Long = UPDATE_INTERVAL * 5 // Every 5 minutes.
//    private val UPDATE_INTERVAL: Long = 10000 // Every 10 seconds.
//    private val FASTEST_UPDATE_INTERVAL: Long = 10000 // Every 10 seconds.
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        bindingMainActivity = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(bindingMainActivity.root)
//
//        if (!checkPermission()) {
//            requestPermissions()
//        }
//
//        bindingMainActivity.BtnEnableService.setOnClickListener {
//            val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                showAlertLocation()
//            } else {
//                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//                getLocationUpdates()
//
//                bindingMainActivity.BtnEnableService.isEnabled = !bindingMainActivity.BtnEnableService.isEnabled
//                bindingMainActivity.BtnDisableService.isEnabled = !bindingMainActivity.BtnDisableService.isEnabled
//            }
//        }
//
//        bindingMainActivity.BtnDisableService.setOnClickListener {
//            stopLocationUpdates()
//
//        }
//        updateButtonsState()
//    }
//
//    private fun createLocationRequest() {
//        mLocationRequest = LocationRequest.create().apply {
//            interval = UPDATE_INTERVAL
//            fastestInterval = FASTEST_UPDATE_INTERVAL
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//    }
//
//    private fun showAlertLocation() {
//        val dialog = AlertDialog.Builder(this)
//        dialog.setTitle("Location settings is turn Off")
//        dialog.setMessage("Please enable location to use this application")
//        dialog.setPositiveButton("Settings") { _, _ ->
//            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            startActivity(myIntent)
//        }
//        dialog.setNegativeButton("Cancel") { _, _ -> }
//        dialog.setCancelable(false)
//        dialog.show()
//    }
//
//    private fun getLocationUpdates() {
//        val intent = Intent(this, LocationService::class.java)
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        createLocationRequest()
//        locationCallback = object : LocationCallback() {
//            @SuppressLint("MissingPermission", "SetTextI18n")
//            override fun onLocationResult(locationResult: LocationResult) {
//                if (locationResult.locations.isNotEmpty()) {
//                    val addresses: List<Address>?
//                    val geoCoder = Geocoder(applicationContext, Locale.getDefault())
//                    addresses = geoCoder.getFromLocation(
//                        locationResult.lastLocation.latitude,
//                        locationResult.lastLocation.longitude,
//                        1
//                    )
//                    if (addresses != null && addresses.isNotEmpty()) {
//                        val address: String = addresses[0].getAddressLine(0)
//                        val city: String = addresses[0].locality
//                        val state: String = addresses[0].adminArea
//                        val country: String = addresses[0].countryName
//                        val postalCode: String = addresses[0].postalCode
//                        val knownName: String = addresses[0].featureName
//
//                        Log.e("location", "$address $city $state $postalCode $country $knownName")
//                        bindingMainActivity.TvLocation.text = "Time : ${getCurrentTimeString()} \n" +
//                                "Latitude : ${locationResult.lastLocation.latitude} \n" +
//                                "Longtitude : ${locationResult.lastLocation.longitude} \n" +
//                                "City : $city \n" +
//                                "State : $state \n" +
//                                "Postal Code : $postalCode"
//                    }
//                    intent.action = LocationService().ACTION_START_FOREGROUND_SERVICE
//                    intent.putExtra("latlong", "${locationResult.lastLocation.latitude}, ${locationResult.lastLocation.longitude}")
//                    startService(intent)
//                }
//            }
//        }
//        startLocationUpdates()
//    }
//
//    // Start location updates
//    @SuppressLint("MissingPermission")
//    private fun startLocationUpdates() {
//        mFusedLocationClient.requestLocationUpdates(
//            mLocationRequest,
//            locationCallback,
//            mainLooper /*Looper*/
//        )
//    }
//
//    // Stop location updates
//    private fun stopLocationUpdates() {
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        mFusedLocationClient.removeLocationUpdates(locationCallback)
//
//        val intent = Intent(this, LocationService::class.java)
//        intent.action = LocationService().ACTION_STOP_FOREGROUND_SERVICE
//        startService(intent)
//
//        bindingMainActivity.BtnEnableService.isEnabled = !bindingMainActivity.BtnEnableService.isEnabled
//        bindingMainActivity.BtnDisableService.isEnabled = !bindingMainActivity.BtnDisableService.isEnabled
//    }
//
//    private fun updateButtonsState() {
//        if(isMyServiceRunning(LocationService::class.java)){
//            bindingMainActivity.BtnEnableService.isEnabled = false
//            bindingMainActivity.BtnDisableService.isEnabled = true
//            Toast.makeText(baseContext, "Service is already running", Toast.LENGTH_SHORT).show()
//        } else {
//            bindingMainActivity.BtnEnableService.isEnabled = true
//            bindingMainActivity.BtnDisableService.isEnabled = false
//            Toast.makeText(baseContext, "There is no service running, starting service..", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
//        try {
//            val manager =
//                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
//                if (serviceClass.name == service.service.className) {
//                    return true
//                }
//            }
//        } catch (e: Exception) {
//            return false
//        }
//        return false
//    }
//
//    private fun getCurrentTimeString(): String {
//        val calendar = Calendar.getInstance()
//        return "${calendar.get(Calendar.HOUR_OF_DAY)}:" +
//                "${calendar.get(Calendar.MINUTE)}:" +
//                "${calendar.get(Calendar.SECOND)}"
//    }
//
//    private fun checkPermission(): Boolean {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val fineLocationPermissionState = ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
//            val backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//
//            return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED &&
//                    backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED
//        } else {
//            val fineLocationPermissionState = ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
//            return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED
//        }
//    }
//
//    private fun requestPermissions() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Check Permissions Now
//            ActivityCompat.requestPermissions(
//                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                LOCATION_REQUEST_CODE
//            )
//        } else
//            Toast.makeText(this@MainActivity, "Location Permission Grant", Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == LOCATION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission was granted.
//                Toast.makeText(this@MainActivity, "Location Permission Grant", Toast.LENGTH_SHORT).show()
//            } else {
//                // If user interaction was interrupted, the permission request is cancelled and you
//                // receive empty arrays.
//                Log.i(this@MainActivity.TAG, "User interaction was cancelled.")
//                Toast.makeText(this@MainActivity, "Location Permission Denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
///*    fun updateButtonsState() {
//        if(startService(Intent(this, LocationService::class.java)) != null) {
//            bindingMainActivity.BtnEnableService.isEnabled = false
//            bindingMainActivity.BtnDisableService.isEnabled = true
//            Toast.makeText(getBaseContext(), "Service is already running", Toast.LENGTH_SHORT).show();
//        }
//        else {
//            bindingMainActivity.BtnEnableService.isEnabled = true
//            bindingMainActivity.BtnDisableService.isEnabled = false
//            Toast.makeText(getBaseContext(), "There is no service running, starting service..", Toast.LENGTH_SHORT).show();
//        }
//    }*/
//
//}
