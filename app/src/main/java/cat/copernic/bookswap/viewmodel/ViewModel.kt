package cat.copernic.bookswap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cat.copernic.bookswap.utils.Llibre
import cat.copernic.bookswap.utils.Usuari


class ViewModel : ViewModel() {

    fun totsLlibresVM(): LiveData<MutableList<Llibre>> {
        val mutableData = MutableLiveData<MutableList<Llibre>>()
        Consultes.totsLlibres().observeForever { eventList ->
            mutableData.value = eventList
        }

        return mutableData

    }

    fun usuari(): LiveData<Usuari>{
        val usuariMail = MutableLiveData<Usuari>()
        Consultes.usuariMail().observeForever { usuari ->
            usuariMail.value = usuari
        }

        return usuariMail

    }

    fun esborrarLlibre(idLlibre: String): MutableLiveData<Boolean> {
        val esborrat = MutableLiveData<Boolean>()
        Deletes.esborrarLlibre(idLlibre).observeForever{ fet ->
            esborrat.value = fet

        }
        return esborrat

    }
}
