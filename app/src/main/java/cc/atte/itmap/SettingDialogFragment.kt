package cc.atte.itmap

import android.app.Dialog
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cc.atte.itmap.databinding.DialogFragmentSettingBinding

class SettingDialogFragment: DialogFragment() {
    companion object {
        const val KEY_SERVER = "server"
        const val KEY_ACCOUNT = "account"
        const val KEY_KEYWORD = "keyword"
        const val KEY_AUTO_TOP_RECORD = "auto_top_record"
        const val KEY_RECORD_TIMING = "record_timing"
        const val KEY_UPLOAD_TIMING = "upload_timing"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        ItMapLog.debug("onCreateDialog($savedInstanceState)")
        super.onCreateDialog(savedInstanceState)

        val builder = activity?.let {
            AlertDialog.Builder(it)
        } ?: error("null activity")

        val inflater = requireActivity().layoutInflater
        val binding = DialogFragmentSettingBinding.inflate(inflater)

        val server = ItMapApp.getPreferenceString(KEY_SERVER)
        binding.settingServer.setText(server)
        val account = ItMapApp.getPreferenceString(KEY_ACCOUNT)
        binding.settingAccount.setText(account)
        val keyword = ItMapApp.getPreferenceString(KEY_KEYWORD)
        binding.settingKeyword.setText(keyword)
        val autoTopRecord = ItMapApp.getPreferenceBoolean(KEY_AUTO_TOP_RECORD)
        binding.settingAutoTopRecord.isChecked = autoTopRecord
        val recordTiming = ItMapApp.getPreferenceInt(KEY_RECORD_TIMING, 1)
        binding.settingRecordTiming.setText(recordTiming.toString())
        val uploadTiming = ItMapApp.getPreferenceInt(KEY_UPLOAD_TIMING, 2)
        binding.settingUploadTiming.setText(uploadTiming.toString())

        builder.setView(binding.root)
            .setTitle("Setting")
            .setMessage("effective with the next recording on")
            .setPositiveButton("OK") { _, _ ->
                val newServer = binding.settingServer.text.toString()
                if (!URLUtil.isValidUrl(newServer) || !newServer.endsWith("/"))
                    Toast.makeText(activity, "bad server url", Toast.LENGTH_SHORT).show()
                else if (server != newServer)
                    ItMapApp.putPreferenceString(KEY_SERVER, newServer)
                val newAccount = binding.settingAccount.text.toString()
                if (!newAccount.all { it.isLetterOrDigit() || it in listOf('-', '_') })
                    Toast.makeText(activity, "bad account string", Toast.LENGTH_SHORT).show()
                else if (account != newAccount)
                    ItMapApp.putPreferenceString(KEY_ACCOUNT, newAccount)
                val newKeyword = binding.settingKeyword.text.toString()
                if (keyword != newKeyword)
                    ItMapApp.putPreferenceString(KEY_KEYWORD, newKeyword)
                val newAutoTopRecord = binding.settingAutoTopRecord.isChecked
                if (autoTopRecord != newAutoTopRecord)
                    ItMapApp.putPreferenceBoolean(KEY_AUTO_TOP_RECORD, newAutoTopRecord)
                val newRecordTiming = binding.settingRecordTiming.text.toString().toInt()
                if (recordTiming != newRecordTiming && 1 <= newRecordTiming)
                    ItMapApp.putPreferenceInt(KEY_RECORD_TIMING, newRecordTiming)
                val newUploadTiming = binding.settingUploadTiming.text.toString().toInt()
                if (uploadTiming != newUploadTiming && 2 <= newUploadTiming)
                    ItMapApp.putPreferenceInt(KEY_UPLOAD_TIMING, newUploadTiming)
            }
            .setNegativeButton("CANCEL") { _, _ -> }

        return builder.create()
    }
}