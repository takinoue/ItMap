package cc.atte.itmap

import android.util.Log
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import io.realm.kotlin.where
import java.util.*

open class ItMapLog(
    @PrimaryKey
    var id: Long = 0L,

    @Required
    var date: Date = Date(),
    @Required
    var message: String = ""
): RealmObject() {
    companion object {
        private fun <T> use(block: (Realm) -> T): T =
            Realm.getDefaultInstance().use(block)

        private fun exec(block: (Realm) -> Unit): Unit =
            use { db -> db.executeTransaction(block) }

        fun debug(msg: String) =
            Log.d("ItMap", msg)

        fun error(msg: String) =
            Log.e("ItMap", msg)

        fun append(message: String): Unit =
            exec { db ->
                val date = Date()
                val query = db.where<ItMapLog>()
                val maxId = query.max("id")?.toLong() ?: 0L
                db.copyToRealm(ItMapLog(maxId + 1, date, message))
            }

        fun appendDebug(message: String): Unit =
            append(message).also { debug(message) }

        fun appendError(message: String): Unit =
            append(message).also { error(message) }
    }
}