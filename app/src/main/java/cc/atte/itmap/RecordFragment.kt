package cc.atte.itmap

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.atte.itmap.databinding.FragmentRecordBinding
import io.realm.*

class RecordFragment : Fragment() {
    private var dummy: Int = 0

    companion object {
        private const val ARG_DUMMY = "arg_dummy"

        @JvmStatic
        fun newInstance(dummy: Int) =
            RecordFragment().apply {
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
    private lateinit var binding: FragmentRecordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_record, container, false)

        realm = actCtx.realm
        binding = FragmentRecordBinding.bind(view)

        val recordData = realm.where(ItMapRecord::class.java)
            .sort("id", Sort.DESCENDING).findAll()

        binding.recordHistoryLast.setOnClickListener {
            if (binding.recordHistoryList.canScrollVertically(-1))
                binding.recordHistoryList.smoothScrollToPosition(0)
        }

        binding.recordHistoryFirst.setOnClickListener {
            if (binding.recordHistoryList.canScrollVertically(1))
                binding.recordHistoryList.smoothScrollToPosition(recordData.size - 1)
        }

        binding.recordHistoryList.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                binding.recordHistoryLast.isEnabled = recyclerView.canScrollVertically(-1)
                binding.recordHistoryFirst.isEnabled = recyclerView.canScrollVertically(1)
            }
        })

        val adapter = ItMapRecordAdapter(recordData)
        val layoutManager = object: LinearLayoutManager(activity) {
            override fun onItemsAdded(
                recyclerView: RecyclerView,
                positionStart: Int, itemCount: Int
            ) {
                super.onItemsAdded(recyclerView, positionStart, itemCount)
                val autoTopPosition =
                    ItMapApp.getPreferenceBoolean(SettingDialogFragment.KEY_AUTO_TOP_RECORD)
                if (autoTopPosition) recyclerView.scrollToPosition(0)
            }
        }

        binding.recordHistoryList.adapter = adapter
        binding.recordHistoryList.layoutManager = layoutManager

        return view
    }
}