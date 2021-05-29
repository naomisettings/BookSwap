package cat.copernic.bookswap.viewmodel

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap

object Inserts {

    //Insretar usuari registat al cloud firestore
    fun insertarUsuari(usuari: HashMap<String, out Any>): MutableLiveData<Boolean> {

        val insertat = MutableLiveData<Boolean>()

        FirebaseFirestore.getInstance().collection("usuaris").add(usuari)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    ContentValues.TAG,
                    "DocumentSnapshot added with ID: ${documentReference.id}"
                )
                insertat.value = true
            }.addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
                insertat.value = false
            }
        return insertat
    }
}
