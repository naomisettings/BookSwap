package cat.copernic.bookswap.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cat.copernic.bookswap.utils.Llibre
import cat.copernic.bookswap.utils.Usuari
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


object Consultes {

    private val user = Firebase.auth.currentUser

    fun totsLlibres(): LiveData<MutableList<Llibre>> {

        val data = MutableLiveData<MutableList<Llibre>>()

        FirebaseFirestore.getInstance()
            .collection("llibres")
            .get().addOnSuccessListener {

                val llibresDC = it.toObjects(Llibre::class.java)
                data.value = llibresDC

            }.addOnFailureListener {exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
        return data
    }

    fun usuariMail(): LiveData<Usuari>{

        val usuari = MutableLiveData<Usuari>()

        FirebaseFirestore.getInstance()
            .collection("usuaris").whereEqualTo("mail", user?.email)
            .get()
            .addOnSuccessListener {

                val usuariDC = it.toObjects(Usuari::class.java)
                usuari.value = usuariDC[0]


            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)

            }
        return usuari
    }
}