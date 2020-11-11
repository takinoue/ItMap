package cc.atte.itmap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cc.atte.itmap.databinding.ActivityMainBinding
import io.realm.Realm

class MainActivity : AppCompatActivity() {

    lateinit var realm: Realm
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ItMapLog.debug("onCreate($savedInstanceState)")

        super.onCreate(savedInstanceState)

        realm = Realm.getDefaultInstance()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.radioLog.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) return@setOnCheckedChangeListener
            val f = LogFragment.newInstance(0)
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container, f, "LOG").commit()
        }

        binding.radioMap.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) return@setOnCheckedChangeListener
            val f = MapFragment.newInstance(0)
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container, f, "MAP").commit()
        }

        binding.radioRecord.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) return@setOnCheckedChangeListener
            val f = RecordFragment.newInstance(0)
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.container, f, "RECORD").commit()
        }

        binding.toggleRecord.isChecked =
            LocationService.isRunning(this)
        binding.toggleRecord.setOnCheckedChangeListener { _, isChecked ->
            val intent = Intent(this, LocationService::class.java)
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
        ItMapLog.debug("onDestroy()")
        realm.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.optionShare -> true.also {
            val baseUrl = "https://www.atte.cc/itmap/"
            val account = ItMapApp.getPreferenceString(SettingDialogFragment.KEY_ACCOUNT)
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_TEXT, "$baseUrl$account.html")
            startActivity(Intent.createChooser(i, "Share URL"))
        }
        R.id.optionSetting -> true.also {
            SettingDialogFragment().show(supportFragmentManager,null)
        }
        R.id.optionMessage -> true.also {
            MessageDialogFragment().show(supportFragmentManager,null)
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun createNotificationChannel() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            LocationService.CHANNEL_ID,
            LocationService.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = LocationService.CHANNEL_DESCRIPTION
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

}