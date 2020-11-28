package cc.atte.itmap

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragmentRecordVM : ViewModel() {
    var totalTime = MutableLiveData(0.0)
    var totalDistance = MutableLiveData(0.0)

    var elevationMin = MutableLiveData(0.0)
    var elevationMax = MutableLiveData(0.0)
    var elevationGain = MutableLiveData(0.0)
    var elevationLoss = MutableLiveData(0.0)
}