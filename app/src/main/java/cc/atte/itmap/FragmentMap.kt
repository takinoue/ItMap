package cc.atte.itmap

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cc.atte.itmap.databinding.FragmentMapBinding

class FragmentMap : Fragment() {
    private var dummy: Int = 0

    companion object {
        private const val ARG_DUMMY = "arg_dummy"

        @JvmStatic
        fun newInstance(dummy: Int) =
            FragmentMap().apply {
                arguments = Bundle().also {
                    it.putInt(ARG_DUMMY, dummy)
                }
            }
    }

    private lateinit var actCtx: ActMain

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actCtx = context as ActMain
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dummy = it.getInt(ARG_DUMMY)
        }
    }

    private lateinit var binding: FragmentMapBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        LogModel.debug("MapFragment.onCreateView")
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        binding = FragmentMapBinding.bind(view)

        //binding.htmlMap.webViewClient = WebViewClient()
        var address = AppMain.Preference.getString(DialogAddress.KEY_ADDRESS)
        if ("" == address) {
            val server = AppMain.Preference.getString(DialogSetting.KEY_SERVER)
            val account = AppMain.Preference.getString(DialogSetting.KEY_ACCOUNT)
            if ("" == server || "" == account)
                Toast.makeText(activity, "setting required", Toast.LENGTH_SHORT).show()
            address = "$server$account.html"
            AppMain.Preference.putString(DialogAddress.KEY_ADDRESS, address)
        }

        @SuppressLint("SetJavaScriptEnabled")
        binding.htmlMap.settings.javaScriptEnabled = true
        binding.htmlMap.loadUrl(address)

        return view
    }
}