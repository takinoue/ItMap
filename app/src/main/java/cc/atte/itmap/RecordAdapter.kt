package cc.atte.itmap

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import java.util.*

class RecordAdapter(data: OrderedRealmCollection<RecordModel>)
    : RealmRecyclerViewAdapter<RecordModel, RecordAdapter.ViewHolder>(data, true) {

    init {
        setHasStableIds(true)
    }

    class ViewHolder(cell: View) : RecyclerView.ViewHolder(cell) {
        val upload: CheckBox = cell.findViewById(R.id.recordUpload)
        val timestamp: TextView = cell.findViewById(R.id.recordTimestamp)
        val longitude: TextView = cell.findViewById(R.id.recordLongitude)
        val latitude: TextView = cell.findViewById(R.id.recordLatitude)
        val altitude: TextView = cell.findViewById(R.id.recordAltitude)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = getItem(position) ?: return
        holder.upload.isChecked = record.upload
        holder.timestamp.text = DateFormat.format(
            "kk:mm:ss", Date((record.timestamp * 1000).toLong()))
        holder.longitude.text = "%.6f".format(record.longitude)
        holder.latitude.text = "%.6f".format(record.latitude)
        holder.altitude.text = "%.1fm".format(record.altitude)
    }

    override fun getItemId(position: Int): Long =
        getItem(position)?.id ?: 0L

}