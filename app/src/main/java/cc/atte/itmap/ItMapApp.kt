package cc.atte.itmap

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.realm.Realm

class ItMapApp: Application() {
    init {
        instance = this
    }

    companion object {
        private lateinit var instance: ItMapApp

        private fun getPreference(): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(instance)
        fun getPreferenceInt(key: String, defValue: Int = 0): Int =
            getPreference().getInt(key, defValue)
        fun putPreferenceInt(key: String, value: Int) {
            getPreference().edit { putInt(key, value) }
        }
        fun getPreferenceString(key: String, defValue: String = ""): String =
            getPreference().getString(key, null) ?: defValue
        fun putPreferenceString(key: String, value: String) {
            getPreference().edit { putString(key, value) }
        }
        fun getPreferenceBoolean(key: String, defValue: Boolean = false): Boolean =
            getPreference().getBoolean(key, defValue)
        fun putPreferenceBoolean(key: String, value: Boolean) {
            getPreference().edit { putBoolean(key, value) }
        }
    }

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        Realm.getDefaultInstance().use { realm ->
            val now = System.currentTimeMillis()
            val yesterday = now / 1000.0 - 60*60*24
            realm.executeTransaction {
                realm.where(ItMapLog::class.java)
                    .findAll().deleteAllFromRealm()
                realm.where(ItMapRecord::class.java)
                    .lessThan("timestamp", yesterday).findAll().deleteAllFromRealm()
            }
        }

        ItMapLog.appendDebug("initialized.")
    }
}