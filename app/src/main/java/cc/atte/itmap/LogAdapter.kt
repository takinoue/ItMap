package cc.atte.itmap

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class LogAdapter(data: OrderedRealmCollection<LogModel>)
    : RealmRecyclerViewAdapter<LogModel, LogAdapter.ViewHolder>(data, true) {

    init {
        setHasStableIds(true)
    }

    class ViewHolder(cell: View) : RecyclerView.ViewHolder(cell) {
        val date: TextView = cell.findViewById(R.id.logDate)
        val message: TextView = cell.findViewById(R.id.logMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = getItem(position) ?: return
        holder.date.text = DateFormat.format("kk:mm:ss", log.date)
        holder.message.text = log.message
    }

    override fun getItemId(position: Int): Long =
        getItem(position)?.id ?: 0L

}