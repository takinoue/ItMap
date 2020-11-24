package cc.atte.itmap

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.realm.Realm
import io.realm.log.RealmLog

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
    }
}