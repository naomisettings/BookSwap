package cat.copernic.bookswap.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cat.copernic.bookswap.utils.Llibre
import com.google.firebase.firestore.FirebaseFirestore


object Consultes {


    fun LlibresConsulta(): LiveData<MutableList<Llibre>> {

        val data = MutableLiveData<MutableList<Llibre>>()

        FirebaseFirestore.getInstance()
            .collection("llibres")
            .get().addOnSuccessListener {

                val llibresDC = it.toObjects(Llibre::class.java)
                data.value = llibresDC

            }.addOnFailureListener{
                // Handle exceptions
            }
        return data
    }

}