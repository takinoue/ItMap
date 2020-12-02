package cc.atte.itmap

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cc.atte.itmap.databinding.DialogAddressBinding

class DialogAddress: DialogFragment() {
    companion object {
        const val KEY_ADDRESS = "address"
        const val KEY_ADDRESS1 = "address1"
        const val KEY_ADDRESS2 = "address2"
        const val KEY_ADDRESS3 = "address3"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        LogModel.debug("onCreateDialog($savedInstanceState)")
        super.onCreateDialog(savedInstanceState)

        val builder = activity?.let {
            AlertDialog.Builder(it)
        } ?: error("null activity")

        val inflater = requireActivity().layoutInflater
        val binding = DialogAddressBinding.inflate(inflater)

        val address1 = AppMain.Preference.getString(KEY_ADDRESS1)
        binding.editAddress1.setText(address1)
        val address2 = AppMain.Preference.getString(KEY_ADDRESS2)
        binding.editAddress2.setText(address2)
        val address3 = AppMain.Preference.getString(KEY_ADDRESS3)
        binding.editAddress3.setText(address3)

        builder.setView(binding.root)
            .setTitle("Address")
            .setMessage("set map address")
            .setPositiveButton("OK") { _, _ ->
                val newAddress1 = binding.editAddress1.text.toString()
                if (address1 != newAddress1)
                    AppMain.Preference.putString(KEY_ADDRESS1, newAddress1)
                val newAddress2 = binding.editAddress2.text.toString()
                if (address2 != newAddress2)
                    AppMain.Preference.putString(KEY_ADDRESS2, newAddress2)
                val newAddress3 = binding.editAddress3.text.toString()
                if (address3 != newAddress3)
                    AppMain.Preference.putString(KEY_ADDRESS3, newAddress3)
            }
            .setNegativeButton("CANCEL") { _, _ -> }

        return builder.create()
    }
}