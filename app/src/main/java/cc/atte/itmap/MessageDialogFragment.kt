package cc.atte.itmap

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cc.atte.itmap.databinding.DialogFragmentMessageBinding

class MessageDialogFragment: DialogFragment() {
    companion object {
        const val KEY_MESSAGE = "message"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        ItMapLog.debug("onCreateDialog($savedInstanceState)")
        super.onCreateDialog(savedInstanceState)

        val builder = activity?.let {
            AlertDialog.Builder(it)
        } ?: error("null activity")

        val inflater = requireActivity().layoutInflater
        val binding = DialogFragmentMessageBinding.inflate(inflater)

        val message = ItMapApp.getPreferenceString(KEY_MESSAGE)
        binding.settingMessage.setText(message)

        builder.setView(binding.root)
            .setTitle("Message")
            .setPositiveButton("OK") { _, _ ->
                val newMessage = binding.settingMessage.text.toString()
                if (message != newMessage)
                    ItMapApp.putPreferenceString(KEY_MESSAGE, newMessage)
            }
            .setNegativeButton("CANCEL") { _, _ -> }

        return builder.create()
    }
}