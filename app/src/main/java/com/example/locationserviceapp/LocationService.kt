package com.example.locationserviceapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import java.util.*

class LocationService : Service() {
    val channelId = "location"
    val channelName = "com.example.locationserviceapp"
    val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
    val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
    val servicePerf = "service"
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest


    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: throw Exception("No notification manager found")
    }

    private val UPDATE_INTERVAL: Long = 10000 // Every 10 seconds.
    private val FASTEST_UPDATE_INTERVAL: Long = 10000 // Every 10 seconds.
//    private val MAX_WAIT_TIME : Long = UPDATE_INTERVAL * 5 // Every 5 minutes.

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_START_FOREGROUND_SERVICE) {
            Log.e("serviceStat", "Temp Service start")
            getLocationUpdates()
        } else if (action == ACTION_STOP_FOREGROUND_SERVICE) {
            Log.e("serviceStat", "Temp Service stop")
            stopLocationUpdates()
            stopForeground(true)
            stopSelf()
        }

        return START_STICKY
    }

    //Get now location every 10 seconds
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create().apply {
//            smallestDisplacement = 100.0f
            interval = UPDATE_INTERVAL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fastestInterval = FASTEST_UPDATE_INTERVAL
            }
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun getLocationUpdates() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = "${locationResult.lastLocation.latitude}, ${locationResult.lastLocation.longitude}"
                    Handler(Looper.getMainLooper()).postDelayed({
                        setNotification(location)
                    }, 500)
                    sendMessageToActivity("Time : ${getCurrentTimeString()} \n" + location)
                }
            }
        }
        startLocationUpdates()
    }

    // Send intent putExtra lat and long to MainActivity
    private fun sendMessageToActivity(msg: String) {
        val intent = Intent("GPSLocationUpdates")
        intent.putExtra("Status", msg)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    // Start location updates
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            locationCallback,
            mainLooper /*Looper*/
        )
        serviceRunning(true)
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.removeLocationUpdates(locationCallback)
        serviceRunning(false)
    }

    private fun serviceRunning(running: Boolean) {
        val cacheServiceStat: SharedPreferences = getSharedPreferences(servicePerf, Context.MODE_PRIVATE)
        val cacheServiceStatEditor: SharedPreferences.Editor = cacheServiceStat.edit()

        cacheServiceStatEditor.putBoolean(servicePerf, running)
        cacheServiceStatEditor.apply()
    }

    // Get current time from device
    private fun getCurrentTimeString(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.HOUR_OF_DAY)}:" +
                "${calendar.get(Calendar.MINUTE)}:" +
                "${calendar.get(Calendar.SECOND)}"
    }

    // Create Notification
    private fun setNotification(location : String) {
        val builder = NotificationCompat.Builder(this, "location")
            .setContentTitle("Location Service")
            .setContentText(location)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        startForeground(1, builder.build())
    }
}

/*    companion object {
        val NOTIFICATION_ID = 123
    }*/

/*
val addresses: List<Address>?
val geoCoder = Geocoder(applicationContext, Locale.getDefault())
addresses = geoCoder.getFromLocation(
locationResult.lastLocation.latitude,
locationResult.lastLocation.longitude,
1
)
if (addresses != null && addresses.isNotEmpty()) {
    val address: String = addresses[0].getAddressLine(0)
    val city: String = addresses[0].locality
    val state: String = addresses[0].adminArea
    val country: String = addresses[0].countryName
    val postalCode: String = addresses[0].postalCode
    val knownName: String = addresses[0].featureName

    Log.e("location", "$address $city $state $postalCode $country $knownName")
    bindingMainActivity.TvLocation.text = "Time : ${getCurrentTimeString()} \n" +
            "Latitude : ${locationResult.lastLocation.latitude} \n" +
            "Longtitude : ${locationResult.lastLocation.longitude} \n" +
            "City : $city \n" +
            "State : $state \n" +
            "Postal Code : $postalCode"
}
*/


/*    fun updateButtonsState() {
        if(startService(Intent(this, LocationService::class.java)) != null) {
            bindingMainActivity.BtnEnableService.isEnabled = false
            bindingMainActivity.BtnDisableService.isEnabled = true
            Toast.makeText(getBaseContext(), "Service is already running", Toast.LENGTH_SHORT).show();
        }
        else {
            bindingMainActivity.BtnEnableService.isEnabled = true
            bindingMainActivity.BtnDisableService.isEnabled = false
            Toast.makeText(getBaseContext(), "There is no service running, starting service..", Toast.LENGTH_SHORT).show();
        }
    }*/