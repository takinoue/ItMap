package cc.atte.itmap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cc.atte.itmap.databinding.ActivityMainBinding
import io.realm.Realm

class ActMain : AppCompatActivity() {

    lateinit var realm: Realm
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        LogModel.debug("onCreate($savedInstanceState)")

        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.radioLog.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) return@setOnCheckedChangeListener
            val f = FragmentLog.newInstance(0)
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container, f, "LOG").commit()
        }

        binding.radioMap.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) return@setOnCheckedChangeListener
            val f = FragmentMap.newInstance(0)
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container, f, "MAP").commit()
        }

        binding.radioRecord.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) return@setOnCheckedChangeListener
            val f = FragmentRecord.newInstance(0)
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container, f, "RECORD").commit()
        }

        binding.toggleRecord.isChecked = AppMain.isService
        binding.toggleRecord.setOnCheckedChangeListener { _, isChecked ->
            val intent = Intent(this, ServiceLocation::class.java)
            if (isChecked) {
                startForegroundService(intent)
            } else {
                stopService(intent)
            }
        }

        createNotificationChannel()

        if (savedInstanceState == null)
            binding.radioLog.performClick()
    }

    override fun onDestroy() {
        LogModel.debug("onDestroy()")
        realm.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.optionSetting -> true.also {
            DialogSetting().show(supportFragmentManager,null)
        }
        R.id.optionMessage -> true.also {
            DialogMessage().show(supportFragmentManager,null)
        }
        R.id.optionShareUrl -> true.also {
            val server = AppMain.Preference.getString(DialogSetting.KEY_SERVER)
            val account = AppMain.Preference.getString(DialogSetting.KEY_ACCOUNT)
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_TEXT, "$server$account.html")
            startActivity(Intent.createChooser(i, "Share URL"))
        }
        R.id.optionShareRecord -> true.also {
            val recordData = realm.where(RecordModel::class.java).sort("id").findAll()
            val recordLine = recordData.map {
                "0:%.6f,%.6f,%.3f,ts=%.3f\n"
                    .format(it.longitude, it.latitude, it.altitude, it.timestamp)
            }
            val recordText = recordLine.reversed().joinToString("")
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_TEXT, recordText)
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "ItMap")
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
        R.id.optionDeleteLog -> true.also {
            if (AppMain.isService)
                Toast.makeText(this, "Stop Recording", Toast.LENGTH_SHORT).show()
            else
                realm.executeTransactionAsync {  db-> db.delete(LogModel::class.java) }
        }
        R.id.optionDeleteRecord -> true.also {
            if (AppMain.isService)
                Toast.makeText(this, "Stop Recording", Toast.LENGTH_SHORT).show()
            else
                realm.executeTransactionAsync {  db-> db.delete(RecordModel::class.java) }
        }
        R.id.optionPrivacy -> true.also {
            DialogPrivacy().show(supportFragmentManager,null)
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun createNotificationChannel() {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            ServiceLocation.CHANNEL_ID,
            ServiceLocation.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = ServiceLocation.CHANNEL_DESCRIPTION
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

}