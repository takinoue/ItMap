package cc.atte.itmap

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.realm.Realm
import io.realm.log.RealmLog
import kotlin.math.*

class AppMain: Application() {
    companion object {
        lateinit var instance: AppMain private set
        var isService: Boolean = false
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        if (Preference.getBoolean(DialogSetting.KEY_AUTO_CLEAN_DATA)) {
            val now = System.currentTimeMillis()
            val yesterday = now / 1000.0 - 60 * 60 * 24
            realmExecute { realm ->
                realm.delete(LogModel::class.java)
                realm.where(RecordModel::class.java)
                    .lessThanOrEqualTo("timestamp", yesterday)
                    .findAll().deleteAllFromRealm()
            }
            LogModel.append("old logs and records deleted.")
        }
        val ttt = Utility.hubeny(35.802739, 140.380034, 35.785796, 140.392265)
        LogModel.debug("$ttt m")
    }

    // Utilities

    fun <T> realmExecute(call: (Realm) -> T): T =
        Realm.getDefaultInstance().use { realm ->
            realm.beginTransaction()
            return try {
                val rv: T = call(realm)
                realm.commitTransaction()
                rv
            } catch (e: Throwable) {
                if (realm.isInTransaction)
                    realm.cancelTransaction()
                else
                    RealmLog.warn("Not currently in a transaction.")
                throw e
            }
        }

    object Preference {
        private fun get(): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(instance)
        fun getInt(key: String, defValue: Int = 0): Int =
            get().getInt(key, defValue)
        fun putInt(key: String, value: Int): Unit =
            get().edit { putInt(key, value) }
        fun getString(key: String, defValue: String = ""): String =
            get().getString(key, null) ?: defValue
        fun putString(key: String, value: String): Unit =
            get().edit { putString(key, value) }
        fun getBoolean(key: String, defValue: Boolean = false): Boolean =
            get().getBoolean(key, defValue)
        fun putBoolean(key: String, value: Boolean): Unit =
            get().edit { putBoolean(key, value) }
        fun getDouble(key: String, defValue: Double = 0.0): Double =
            java.lang.Double.longBitsToDouble(
                get().getLong(key, java.lang.Double.doubleToRawLongBits(defValue)))
        fun putDouble(key: String, value: Double): Unit =
            get().edit { putLong(key, java.lang.Double.doubleToRawLongBits(value)) }
    }

    object Utility {
        fun hubeny(lat1deg: Double, lng1deg: Double,
                   lat2deg: Double, lng2deg: Double): Double {
            val lat1 = lat1deg * PI / 180.0
            val lng1 = lng1deg * PI / 180.0
            val lat2 = lat2deg * PI / 180.0
            val lng2 = lng2deg * PI / 180.0

            val latMean = (lat1 + lat2) / 2.0
            val latDiff = lat1 - lat2
            val lngDiff = lng1 - lng2

            val xL = 1.0 - 0.00669438 * sin(latMean).pow(2)
            val xM = 6335439.327 / sqrt(xL.pow(3))
            val xN = 6378137.0 / sqrt(xL)

            val t1 = xM * latDiff
            val t2 = xN * cos(latMean) * lngDiff

            return sqrt((t1 * t1) + (t2 * t2))
        }
    }
}