package cc.atte.itmap

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where

open class ItMapRecord(
    @PrimaryKey
    var id: Long = 0L,

    var upload: Boolean = false,

    var timestamp: Double = Double.MIN_VALUE,
    var longitude: Double = Double.MIN_VALUE,
    var latitude: Double = Double.MIN_VALUE,
    var altitude: Double = Double.MIN_VALUE,
): RealmObject() {
    companion object {
        private fun <T> use(block: (Realm) -> T): T =
            Realm.getDefaultInstance().use(block)

        private fun exec(block: (Realm) -> Unit): Unit =
            use { db -> db.executeTransaction(block) }

        fun append(timestamp: Double,
                   longitude: Double, latitude: Double, altitude: Double): Long {
            val newRecord = ItMapRecord(
                0,false,
                timestamp, longitude, latitude, altitude)
            exec { db ->
                val query = db.where<ItMapRecord>()
                val maxId = query.max("id")?.toLong() ?: 0L
                newRecord.id = maxId + 1
                db.copyToRealm(newRecord)
            }
            return newRecord.id
        }
    }
}