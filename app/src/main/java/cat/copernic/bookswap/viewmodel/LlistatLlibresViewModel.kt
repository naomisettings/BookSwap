package cat.copernic.bookswap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.bookswap.utils.Llibre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LlistatLlibresViewModel : ViewModel() {

    fun fetchEventData(): LiveData<MutableList<Llibre>> {
        val mutableData = MutableLiveData<MutableList<Llibre>>()
        Consultes.LlibresConsulta().observeForever { eventList ->
            mutableData.value = eventList
        }

        return mutableData

    }


}