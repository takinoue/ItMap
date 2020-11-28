package cc.atte.itmap

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.atte.itmap.databinding.FragmentRecordBinding
import io.realm.*

class FragmentRecord : Fragment() {
    private var dummy: Int = 0

    companion object {
        private const val ARG_DUMMY = "arg_dummy"

        @JvmStatic
        fun newInstance(dummy: Int) =
            FragmentRecord().apply {
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

    private lateinit var realm: Realm
    private lateinit var binding: FragmentRecordBinding

    private val viewModel: FragmentRecordVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_record, container, false)

        realm = actCtx.realm
        binding = FragmentRecordBinding.bind(view)

        viewModel.totalTime.observe(viewLifecycleOwner, {
            binding.recordTotalTimeSecond.text = "%02d:%02d:%02d".format(
                (it / 3600).toInt(), (it / 60).toInt() % 60, it.toInt() % 60)
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, {
            binding.recordTotalDistanceMetre.text = "%.1fm".format(it)
        })

        viewModel.elevationMin.observe(viewLifecycleOwner, {
            binding.recordElevationMinMetre.text = "%.1fm".format(it)
        })
        viewModel.elevationMax.observe(viewLifecycleOwner, {
            binding.recordElevationMaxMetre.text = "%.1fm".format(it)
        })
        viewModel.elevationGain.observe(viewLifecycleOwner, {
            binding.recordElevationGainMetre.text = "%.1fm".format(it)
        })
        viewModel.elevationLoss.observe(viewLifecycleOwner, {
            binding.recordElevationLossMetre.text = "%.1fm".format(it)
        })

        val recordData = realm.where(RecordModel::class.java)
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

        val adapter = RecordAdapter(recordData)
        val layoutManager = object: LinearLayoutManager(activity) {
            override fun onItemsAdded(
                recyclerView: RecyclerView,
                positionStart: Int, itemCount: Int
            ) {
                super.onItemsAdded(recyclerView, positionStart, itemCount)
                val autoFollowRecord =
                    AppMain.Preference.getBoolean(DialogSetting.KEY_AUTO_FOLLOW_RECORD)
                if (autoFollowRecord) recyclerView.scrollToPosition(0)
            }
        }

        binding.recordHistoryList.adapter = adapter
        binding.recordHistoryList.layoutManager = layoutManager

        return view
    }
}