package cc.atte.itmap

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cc.atte.itmap.databinding.DialogMessageBinding

class DialogMessage: DialogFragment() {
    companion object {
        const val KEY_MESSAGE = "message"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        LogModel.debug("onCreateDialog($savedInstanceState)")
        super.onCreateDialog(savedInstanceState)

        val builder = activity?.let {
            AlertDialog.Builder(it)
        } ?: error("null activity")

        val inflater = requireActivity().layoutInflater
        val binding = DialogMessageBinding.inflate(inflater)

        val message = AppMain.Preference.getString(KEY_MESSAGE)
        binding.settingMessage.setText(message)

        builder.setView(binding.root)
            .setTitle("Message")
            .setPositiveButton("OK") { _, _ ->
                val newMessage = binding.settingMessage.text.toString()
                if (message != newMessage)
                    AppMain.Preference.putString(KEY_MESSAGE, newMessage)
            }
            .setNegativeButton("CANCEL") { _, _ -> }

        return builder.create()
    }
}