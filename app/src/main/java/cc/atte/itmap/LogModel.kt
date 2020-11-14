package cc.atte.itmap

import android.util.Log
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import io.realm.kotlin.where
import java.util.*

open class LogModel(
    @PrimaryKey
    var id: Long = 0L,

    @Required
    var date: Date = Date(),
    @Required
    var message: String = ""
): RealmObject() {
    companion object {
        fun debug(msg: String) =
            Log.d("ItMap", msg)

        fun error(msg: String) =
            Log.e("ItMap", msg)

        fun append(message: String): Unit =
            AppMain.instance.realmExecute { db ->
                val date = Date()
                val query = db.where<LogModel>()
                val maxId = query.max("id")?.toLong() ?: 0L
                db.copyToRealm(LogModel(maxId + 1, date, message))
            }

        fun appendDebug(message: String): Unit =
            append(message).also { debug(message) }

        fun appendError(message: String): Unit =
            append(message).also { error(message) }
    }
}