package cc.atte.itmap

import android.app.Dialog
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cc.atte.itmap.databinding.DialogSettingBinding

class DialogSetting: DialogFragment() {
    companion object {
        const val KEY_SERVER = "server"
        const val KEY_ACCOUNT = "account"
        const val KEY_KEYWORD = "keyword"
        const val KEY_RECORD_TIMING = "record_timing"
        const val KEY_UPLOAD_TIMING = "upload_timing"
        const val KEY_AUTO_CLEAN_DATA = "auto_clean_data"
        const val KEY_AUTO_FOLLOW_RECORD = "auto_follow_record"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        LogModel.debug("onCreateDialog($savedInstanceState)")
        super.onCreateDialog(savedInstanceState)

        val builder = activity?.let {
            AlertDialog.Builder(it)
        } ?: error("null activity")

        val inflater = requireActivity().layoutInflater
        val binding = DialogSettingBinding.inflate(inflater)

        val server = AppMain.Preference.getString(KEY_SERVER)
        binding.settingServer.setText(server)
        val account = AppMain.Preference.getString(KEY_ACCOUNT)
        binding.settingAccount.setText(account)
        val keyword = AppMain.Preference.getString(KEY_KEYWORD)
        binding.settingKeyword.setText(keyword)
        val recordTiming = AppMain.Preference.getInt(KEY_RECORD_TIMING, 1)
        binding.settingRecordTiming.setText(recordTiming.toString())
        val uploadTiming = AppMain.Preference.getInt(KEY_UPLOAD_TIMING, 2)
        binding.settingUploadTiming.setText(uploadTiming.toString())
        val autoCleanData = AppMain.Preference.getBoolean(KEY_AUTO_CLEAN_DATA)
        binding.settingAutoCleanData.isChecked = autoCleanData
        val autoFollowRecord = AppMain.Preference.getBoolean(KEY_AUTO_FOLLOW_RECORD)
        binding.settingAutoFollowRecord.isChecked = autoFollowRecord

        builder.setView(binding.root)
            .setTitle("Setting")
            .setMessage("effective with the next recording on")
            .setPositiveButton("OK") { _, _ ->
                val newServer = binding.settingServer.text.toString()
                if (!URLUtil.isValidUrl(newServer) || !newServer.endsWith("/"))
                    Toast.makeText(activity, "bad server url", Toast.LENGTH_SHORT).show()
                else if (server != newServer)
                    AppMain.Preference.putString(KEY_SERVER, newServer)
                val newAccount = binding.settingAccount.text.toString()
                if (!newAccount.all { it.isLetterOrDigit() || it in listOf('-', '_') })
                    Toast.makeText(activity, "bad account string", Toast.LENGTH_SHORT).show()
                else if (account != newAccount)
                    AppMain.Preference.putString(KEY_ACCOUNT, newAccount)
                val newKeyword = binding.settingKeyword.text.toString()
                if (keyword != newKeyword)
                    AppMain.Preference.putString(KEY_KEYWORD, newKeyword)
                val newRecordTiming = binding.settingRecordTiming.text.toString().toInt()
                if (recordTiming != newRecordTiming && 1 <= newRecordTiming)
                    AppMain.Preference.putInt(KEY_RECORD_TIMING, newRecordTiming)
                val newUploadTiming = binding.settingUploadTiming.text.toString().toInt()
                if (uploadTiming != newUploadTiming && 1 <= newUploadTiming)
                    AppMain.Preference.putInt(KEY_UPLOAD_TIMING, newUploadTiming)
                val newAutoCleanData = binding.settingAutoCleanData.isChecked
                if (autoCleanData != newAutoCleanData)
                    AppMain.Preference.putBoolean(KEY_AUTO_CLEAN_DATA, newAutoCleanData)
                val newAutoFollowRecord = binding.settingAutoFollowRecord.isChecked
                if (autoFollowRecord != newAutoFollowRecord)
                    AppMain.Preference.putBoolean(KEY_AUTO_FOLLOW_RECORD, newAutoFollowRecord)
            }
            .setNegativeButton("CANCEL") { _, _ -> }

        return builder.create()
    }
}