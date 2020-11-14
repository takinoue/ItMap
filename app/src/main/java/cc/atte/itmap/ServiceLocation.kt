package cc.atte.itmap

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.*
import io.realm.Realm
import io.realm.Sort
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ServiceLocation : Service() {
    companion object {
        const val CHANNEL_ID = "itmap_channel"
        const val CHANNEL_NAME = "itmap"
        const val CHANNEL_DESCRIPTION = "itmap status"
    }

    private lateinit var fusedClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        AppMain.isService = true
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private var server = ""
    private var account = ""
    private var keyword = ""

    private var recordTiming = 1
    private var uploadTiming = 2

    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    private val apiService by lazy { JsonApiService.create(server) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogModel.debug("onStartCommand($intent, $flags, $startId)")

        server = AppMain.Preference.getString(DialogSetting.KEY_SERVER)
        if ("" == server) {
            server = JsonApiService.BASE_URL
            AppMain.Preference.putString(DialogSetting.KEY_SERVER, server)
        }
        account = AppMain.Preference.getString(DialogSetting.KEY_ACCOUNT)
        if ("" == account) {
            account = UUID.randomUUID().toString()
            AppMain.Preference.putString(DialogSetting.KEY_ACCOUNT, account)
        }
        keyword = AppMain.Preference.getString(DialogSetting.KEY_KEYWORD)
        if ("" == keyword) {
            keyword = UUID.randomUUID().toString()
            AppMain.Preference.putString(DialogSetting.KEY_KEYWORD, keyword)
        }

        recordTiming =
            AppMain.Preference.getInt(DialogSetting.KEY_RECORD_TIMING, 1)
        uploadTiming =
            AppMain.Preference.getInt(DialogSetting.KEY_UPLOAD_TIMING, 2)

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
        LogModel.debug("onDestroy()")
        stopLocationUpdates()
        AppMain.isService = false
        super.onDestroy()
    }

    private fun startLocationUpdates() {
        if (PackageManager.PERMISSION_GRANTED
            != checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            LogModel.appendDebug("Location access prohibited")
            val notification = createNotificationProhibited()
            NotificationManagerCompat.from(this).notify(2, notification)
        } else
            fusedClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotificationRunning(): Notification {
        val openIntent = Intent(this, ActMain::class.java).let {
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
            LogModel.debug("recording")
            for (location in locationResult.locations) {
                val newId = RecordModel.append(
                    location.time / 1000.0,
                    location.longitude, location.latitude, location.altitude
                )
                LogModel.appendDebug(
                    "Record[%03d] = { %.3f, %.3f }"
                        .format(newId, location.longitude, location.latitude)
                )
                if (appendCounter++ % uploadTiming == 0)
                    uploadSchedule = true
            }
            // Uploading
            if (!uploadSchedule) return
            LogModel.debug("uploading")
            val realm = Realm.getDefaultInstance()
            val record = realm.where(RecordModel::class.java)
                .equalTo("upload", false)
                .sort("id", Sort.DESCENDING).limit(60).findAll()
            val message = AppMain.Preference.getString(DialogMessage.KEY_MESSAGE)
            val apiModel = JsonApiModel.Request(
                keyword, message.takeIf { it != "" },
                record.map {
                    JsonApiModel.Request.Coordinate(
                        it.timestamp, it.longitude, it.latitude, it.altitude
                    )
                }
            )
            val callback = object : Callback<JsonApiModel.Response> {
                override fun onFailure(
                    call: Call<JsonApiModel.Response>, t: Throwable
                ) {
                    LogModel.appendError("upload failed")
                }
                override fun onResponse(
                    call: Call<JsonApiModel.Response>,
                    response: Response<JsonApiModel.Response>
                ) {
                    if (!response.isSuccessful) return Unit.also {
                        val errorResponse = response.errorBody()?.string()
                        LogModel.appendError("server failed: $errorResponse")
                    }
                    if (record.size != response.body()?.success?.count)
                        LogModel.appendDebug("size mismatched between request and response")
                    AppMain.instance.realmExecute {
                        record.setBoolean("upload", true)
                    }
                    LogModel.debug("upload successfully")
                }
            }
            apiService.postData(account, apiModel).enqueue(callback)
        }
    }
}