package cc.atte.itmap

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
                arguments = Bundle().apply {
                    putInt(ARG_DUMMY, dummy)
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
        val server = AppMain.Preference.getString(DialogSetting.KEY_SERVER)
        val account = AppMain.Preference.getString(DialogSetting.KEY_ACCOUNT)
        if (server != "" && account != "") {
            binding.htmlMap.settings.javaScriptEnabled = true
            binding.htmlMap.loadUrl("$server$account.html")
        } else
            Toast.makeText(activity, "setting required", Toast.LENGTH_SHORT).show()

        return view
    }
}