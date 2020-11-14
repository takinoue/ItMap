package cc.atte.itmap

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where

open class RecordModel(
    @PrimaryKey
    var id: Long = 0L,

    var upload: Boolean = false,

    var timestamp: Double = Double.MIN_VALUE,
    var longitude: Double = Double.MIN_VALUE,
    var latitude: Double = Double.MIN_VALUE,
    var altitude: Double = Double.MIN_VALUE,
): RealmObject() {
    companion object {
        fun append(timestamp: Double,
                   longitude: Double, latitude: Double, altitude: Double): Long {
            val newRecord = RecordModel(
                0,false,
                timestamp, longitude, latitude, altitude)
            AppMain.instance.realmExecute { db ->
                val query = db.where<RecordModel>()
                val maxId = query.max("id")?.toLong() ?: 0L
                newRecord.id = maxId + 1
                db.copyToRealm(newRecord)
            }
            return newRecord.id
        }
    }
}