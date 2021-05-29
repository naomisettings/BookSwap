package cat.copernic.bookswap.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import cat.copernic.bookswap.utils.Llibres
import com.google.firebase.firestore.FirebaseFirestore

object Deletes {

    fun esborrarLlibre(idLlibre: String): MutableLiveData<Boolean> {
        val db = FirebaseFirestore.getInstance()

        val esborrat = MutableLiveData<Boolean>()

        db.collection("llibres")
            .get().addOnSuccessListener {

                val llibreConsulta = it.toObjects(Llibres::class.java)
                it?.forEachIndexed { index, element ->
                    //Extreu la id del document
                    val llibresId = element.id

                    if (llibreConsulta[index].id == idLlibre) {
                        Log.i("idLlibreConsulta", llibreConsulta[index].id)

                        val sfDocRefLlibre = db.collection("llibres").document(llibresId)

                        //Esborra els llibres publicats per l'usuari
                        db.runTransaction { transaction ->
                            //esborrem el llibre del Firestore
                            transaction.delete(sfDocRefLlibre)
                        }.addOnSuccessListener {
                            Log.d("TAG", "Transaction success!")

                            esborrat.value = true

                        }.addOnFailureListener {
                            esborrat.value = false
                        }
                    }

                }

            }.addOnFailureListener{ exception ->
            Log.w(ContentValues.TAG, "Error getting documents: ", exception)
        }
        return esborrat
    }
}
