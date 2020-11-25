package cc.atte.itmap

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import cc.atte.itmap.databinding.DialogPrivacyBinding
import com.google.android.gms.location.LocationServices

class DialogPrivacy: DialogFragment() {
    companion object {
        const val KEY_PRIVACY_RADIUS = "privacy_radius"
        const val KEY_PRIVACY_LATITUDE = "privacy_latitude"
        const val KEY_PRIVACY_LONGITUDE = "privacy_longitude"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        LogModel.debug("onCreateDialog($savedInstanceState)")
        super.onCreateDialog(savedInstanceState)

        val builder = activity?.let {
            AlertDialog.Builder(it)
        } ?: error("null activity")

        val inflater = requireActivity().layoutInflater
        val binding = DialogPrivacyBinding.inflate(inflater)

        val radius = AppMain.Preference.getInt(KEY_PRIVACY_RADIUS, 0)
        binding.editPrivacyRadius.setText(radius.toString())
        val latitude = AppMain.Preference.getDouble(KEY_PRIVACY_LATITUDE, 0.0)
        binding.editPrivacyLatitude.setText(latitude.toString())
        val longitude = AppMain.Preference.getDouble(KEY_PRIVACY_LONGITUDE, 0.0)
        binding.editPrivacyLongitude.setText(longitude.toString())

        binding.buttonPrivacyFetchLocation.setOnClickListener {
            val act = activity ?: error("null activity")
            val fusedClient = LocationServices.getFusedLocationProviderClient(act)
            if (PackageManager.PERMISSION_GRANTED
                != act.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(act, "location access required", Toast.LENGTH_SHORT).show()
            } else fusedClient.lastLocation.addOnSuccessListener {
                binding.editPrivacyLatitude.setText(it?.latitude?.toString())
                binding.editPrivacyLongitude.setText(it?.longitude?.toString())
            }
        }

        binding.buttonPrivacyCheckLocation.setOnClickListener {
            val lat = binding.editPrivacyLatitude.text.toString()
            val lng = binding.editPrivacyLongitude.text.toString()
            val uri = Uri.parse("geo:0,0?q=$lat,$lng(Here)")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        builder.setView(binding.root)
            .setTitle("Privacy")
            .setMessage("no recording in following area")
            .setPositiveButton("OK") { _, _ ->
                val newRadius = binding.editPrivacyRadius.text.toString().toInt()
                if (radius != newRadius)
                    AppMain.Preference.putInt(KEY_PRIVACY_RADIUS, newRadius)
                val newLatitude = binding.editPrivacyLatitude.text.toString().toDouble()
                if (latitude != newLatitude && -90.0 <= newLatitude && newLatitude <= 90.0)
                    AppMain.Preference.putDouble(KEY_PRIVACY_LATITUDE, newLatitude)
                val newLongitude = binding.editPrivacyLongitude.text.toString().toDouble()
                if (longitude != newLongitude && -180.0 <= newLongitude && newLongitude <= 180.0)
                    AppMain.Preference.putDouble(KEY_PRIVACY_LONGITUDE, newLongitude)
            }
            .setNegativeButton("CANCEL") { _, _ -> }

        return builder.create()
    }
}