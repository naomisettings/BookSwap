package cat.copernic.bookswap.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cat.copernic.bookswap.utils.Llibre
import cat.copernic.bookswap.utils.Usuari
import cat.copernic.bookswap.utils.UsuariDC
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


object Consultes {

    private val user = Firebase.auth.currentUser

    //consulta per extreure tot els llibres
    fun totsLlibres(): LiveData<MutableList<Llibre>> {

        val data = MutableLiveData<MutableList<Llibre>>()

        FirebaseFirestore.getInstance()
            .collection("llibres")
            .get().addOnSuccessListener {

                val llibresDC = it.toObjects(Llibre::class.java)
                data.value = llibresDC

            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
        return data
    }

    //consulta per extreure els llibres de l'usuari identificat
    fun meusLlibres(): LiveData<MutableList<Llibre>> {

        val data = MutableLiveData<MutableList<Llibre>>()

        FirebaseFirestore.getInstance()
            .collection("llibres").whereEqualTo("mail", user?.email)
            .get().addOnSuccessListener {

                val llibresDC = it.toObjects(Llibre::class.java)
                data.value = llibresDC

            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
        return data
    }

    //consulta per extreure mail de l'usuari identificat
    fun usuariMail(): LiveData<Usuari> {

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

    //consulta per extreure les dades de l'usuari que ha publicat el llibre
    fun llibrePublicat(mailUsuari: String): LiveData<Usuari> {
        var mail = mailUsuari
        val usuari = MutableLiveData<Usuari>()
        FirebaseFirestore.getInstance()
            .collection("usuaris").whereEqualTo("mail", mail)
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
