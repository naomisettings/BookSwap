package cat.copernic.bookswap.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import cat.copernic.bookswap.utils.Llibres
import com.google.firebase.firestore.FirebaseFirestore

object Deletes {

    //Delete per esborrar un llibre, rep com a paràmetre la id del llibre que es deplaça a un costat
    fun esborrarLlibre(idLlibre: String): MutableLiveData<Boolean> {

        //Inicialitza la base de dades
        val db = FirebaseFirestore.getInstance()
        //La funció retorna un boolan
        val esborrat = MutableLiveData<Boolean>()

        db.collection("llibres")
            .get().addOnSuccessListener {

                val llibreConsulta = it.toObjects(Llibres::class.java)
                it?.forEachIndexed { index, element ->

                    //Extreu la id del document
                    val llibresId = element.id

                    //En el cas que la id coincideixi amb algún llibre de la colecció s'esborra
                    if (llibreConsulta[index].id == idLlibre) {
                        val sfDocRefLlibre = db.collection("llibres").document(llibresId)
                        db.runTransaction { transaction ->

                            //Esborrem el llibre del Firestore
                            transaction.delete(sfDocRefLlibre)
                        }.addOnSuccessListener {
                            Log.d("TAG", "Transaction success!")

                            //Assigna el boolean per retornar
                            esborrat.value = true

                        }.addOnFailureListener {
                            //Assigna el boolean per retornar
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
