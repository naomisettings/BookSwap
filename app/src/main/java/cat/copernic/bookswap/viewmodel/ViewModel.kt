package cat.copernic.bookswap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cat.copernic.bookswap.utils.Llibre
import cat.copernic.bookswap.utils.Usuari
import java.util.HashMap


//Conté les consultes, updates i deletes fetes en ViewModel
class ViewModel : ViewModel() {

    //********** CONSULTES **********

    fun totsLlibresVM(): LiveData<MutableList<Llibre>> {
        val mutableData = MutableLiveData<MutableList<Llibre>>()
        Consultes.totsLlibres().observeForever { eventList ->
            mutableData.value = eventList
        }
        return mutableData
    }

    fun meusLlibresVM(): LiveData<MutableList<Llibre>> {
        val mutableData = MutableLiveData<MutableList<Llibre>>()
        Consultes.meusLlibres().observeForever { eventList ->
            mutableData.value = eventList
        }
        return mutableData
    }

    fun usuariLoginat(): LiveData<Usuari>{
        val usuariMail = MutableLiveData<Usuari>()
        Consultes.usuariLoginat().observeForever { usuari ->
            usuariMail.value = usuari
        }
        return usuariMail
    }

    fun usuariLlibrePublicat(mailUsuari: String): MutableLiveData<Usuari> {
        val mailPublicat = MutableLiveData<Usuari>()
        Consultes.usuariLlibrePublicat(mailUsuari).observeForever{ usuari ->
            mailPublicat.value = usuari
        }
        return mailPublicat
    }

    fun totsUsuaris(): LiveData<MutableList<Usuari>> {
        val usuaris = MutableLiveData<MutableList<Usuari>>()
        Consultes.totsUsuaris().observeForever { eventList ->
            usuaris.value = eventList
        }
        return usuaris
    }

    //********** DELETES **********

    fun esborrarLlibre(idLlibre: String): MutableLiveData<Boolean> {
        val esborrat = MutableLiveData<Boolean>()
        Deletes.esborrarLlibre(idLlibre).observeForever{ fet ->
            esborrat.value = fet
        }
        return esborrat

    }

    //********** UPDATES **********

    //********** INSERTS **********

    fun insertarUsuari(usuari: HashMap<String, out Any>): MutableLiveData<Boolean> {
        val esborrat = MutableLiveData<Boolean>()
        Inserts.insertarUsuari(usuari).observeForever{ fet ->
            esborrat.value = fet
        }
        return esborrat

    }

}
