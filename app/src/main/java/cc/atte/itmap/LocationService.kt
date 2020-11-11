package cc.atte.itmap

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import io.realm.Realm
import io.realm.Sort
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LocationService : Service() {
    companion object {
        const val CHANNEL_ID = "itmap_channel"
        const val CHANNEL_NAME = "itmap"
        const val CHANNEL_DESCRIPTION = "itmap status"

        fun isRunning(context: Context): Boolean {
            return LocalBroadcastManager.getInstance(context)
                .sendBroadcast(Intent("PingPong"))
        }
    }

    private val broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) { /* nop */ }
    }

    private lateinit var fusedClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(broadcastReceiver, IntentFilter("PingPong"))
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private var server = ""
    private var account = ""
    private var keyword = ""

    private var recordTiming = 1
    private var uploadTiming = 2

    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private val apiService by lazy { ItMapApiService.create(server) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ItMapLog.debug("onStartCommand($intent, $flags, $startId)")

        server = ItMapApp.getPreferenceString(SettingDialogFragment.KEY_SERVER)
        if ("" == server) {
            server = ItMapApiService.BASE_URL
            ItMapApp.putPreferenceString(SettingDialogFragment.KEY_SERVER, server)
        }
        account = ItMapApp.getPreferenceString(SettingDialogFragment.KEY_ACCOUNT)
        if ("" == account) {
            account = UUID.randomUUID().toString()
            ItMapApp.putPreferenceString(SettingDialogFragment.KEY_ACCOUNT, account)
        }
        keyword = ItMapApp.getPreferenceString(SettingDialogFragment.KEY_KEYWORD)
        if ("" == keyword) {
            keyword = UUID.randomUUID().toString()
            ItMapApp.putPreferenceString(SettingDialogFragment.KEY_KEYWORD, keyword)
        }

        recordTiming =
            ItMapApp.getPreferenceInt(SettingDialogFragment.KEY_RECORD_TIMING, 1)
        uploadTiming =
            ItMapApp.getPreferenceInt(SettingDialogFragment.KEY_UPLOAD_TIMING, 2)

        locationRequest = createLocationRequest()
        locationCallback = createLocationCallback()

        val notification = createNotificationRunning()
        startForeground(1, notification)
        startLocationUpdates()

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        ItMapLog.debug("onDestroy()")
        stopLocationUpdates()
        super.onDestroy()
    }

    private fun startLocationUpdates() {
        if (PackageManager.PERMISSION_GRANTED
            != checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ItMapLog.appendDebug("Location access prohibited")
            val notification = createNotificationProhibited()
            NotificationManagerCompat.from(this).notify(2, notification)
        } else
            fusedClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotificationRunning(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_my_notification)
            .setContentTitle("Recording location")
            .setContentText("Tap to check log messages")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
        return notificationBuilder.build()
    }

    private fun createNotificationProhibited(): Notification {
        val openIntent = Intent().let {
            it.data = Uri.parse("package:${packageName}")
            it.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            PendingIntent.getActivity(this, 0, it, 0)
        }
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_my_notification)
            .setContentTitle("Location access prohibited")
            .setContentText("Tap to grant location permission")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
        return notificationBuilder.build()
    }

    private fun createLocationRequest() = LocationRequest.create().apply {
        interval = recordTiming * 60 * 1000L
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun createLocationCallback() = object: LocationCallback() {
        private var appendCounter = 0
        override fun onLocationResult(locationResult: LocationResult?) {
            // Recording
            locationResult ?: return
            var uploadSchedule = false
            ItMapLog.debug("recording")
            for (location in locationResult.locations) {
                val newId = ItMapRecord.append(
                    location.time / 1000.0,
                    location.longitude, location.latitude, location.altitude
                )
                ItMapLog.appendDebug(
                    "Record[%03d] = { %.3f, %.3f }"
                        .format(newId, location.longitude, location.latitude)
                )
                if (appendCounter++ % uploadTiming == 0)
                    uploadSchedule = true
            }
            // Uploading
            if (!uploadSchedule) return
            ItMapLog.debug("uploading")
            val realm = Realm.getDefaultInstance()
            val record = realm.where(ItMapRecord::class.java)
                .equalTo("upload", false)
                .sort("id", Sort.DESCENDING).limit(60).findAll()
            val message = ItMapApp.getPreferenceString(MessageDialogFragment.KEY_MESSAGE)
            val apiModel = ItMapApiModel.Request(
                keyword, message.takeIf { it != "" },
                record.map {
                    ItMapApiModel.Request.Coordinate(
                        it.timestamp, it.longitude, it.latitude, it.altitude
                    )
                }
            )
            val callback = object : Callback<ItMapApiModel.Response> {
                override fun onFailure(
                    call: Call<ItMapApiModel.Response>, t: Throwable
                ) {
                    ItMapLog.appendError("upload failed")
                }
                override fun onResponse(
                    call: Call<ItMapApiModel.Response>,
                    response: Response<ItMapApiModel.Response>
                ) {
                    if (!response.isSuccessful) return Unit.also {
                        val errorResponse = response.errorBody()?.string()
                        ItMapLog.appendError("server failed: $errorResponse")
                    }
                    if (record.size != response.body()?.success?.count)
                        ItMapLog.appendDebug("size mismatched between request and response")
                    realm.executeTransaction { record.setBoolean("upload", true) }
                    ItMapLog.debug("upload successfully")
                }
            }
            apiService.postData(account, apiModel).enqueue(callback)
        }
    }
}