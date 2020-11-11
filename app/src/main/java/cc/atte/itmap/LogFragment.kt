package cc.atte.itmap

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.atte.itmap.databinding.FragmentLogBinding
import io.realm.*

class LogFragment : Fragment() {
    private var dummy: Int = 0

    companion object {
        private const val ARG_DUMMY = "arg_dummy"

        @JvmStatic
        fun newInstance(dummy: Int) =
            LogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_DUMMY, dummy)
                }
            }
    }

    private lateinit var actCtx: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actCtx = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dummy = it.getInt(ARG_DUMMY)
        }
    }

    private lateinit var realm: Realm
    private lateinit var binding: FragmentLogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log, container, false)

        realm = actCtx.realm
        binding = FragmentLogBinding.bind(view)

        val logData = realm.where(ItMapLog::class.java)
            .sort("id", Sort.DESCENDING).findAll()

        val adapter = ItMapLogAdapter(logData)
        val layoutManager = LinearLayoutManager(activity)

        binding.logLast.setOnClickListener {
            binding.logList.scrollToPosition(0)
        }
        binding.logFirst.setOnClickListener {
            binding.logList.scrollToPosition(logData.size - 1)
        }

        binding.logList.adapter = adapter
        binding.logList.layoutManager = layoutManager
        binding.logList.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                binding.logLast.isEnabled = recyclerView.canScrollVertically(-1)
                binding.logFirst.isEnabled = recyclerView.canScrollVertically(1)
            }
        })

        return view
    }
}