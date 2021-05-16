package cat.copernic.bookswap.modificarusuari

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentAfegirLlibreBinding
import cat.copernic.bookswap.databinding.FragmentModificarUsuariBinding
import cat.copernic.bookswap.utils.UsuariDC
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class ModificarUsuari : Fragment() {

    val db = FirebaseFirestore.getInstance()
    private lateinit var binding: FragmentModificarUsuariBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_modificar_usuari, container, false)

        mostrarDades()

        binding.bttnSingOut.setOnClickListener {
            //SingOut
            Firebase.auth.signOut()
            //Obra el fragment principal
            findNavController().navigate(R.id.action_modificarUsuari_to_loginFragment)
        }

        binding.bttnActualitzar.setOnClickListener {

        }

        return binding.root
    }

    fun mostrarDades() {
        //Guarda les dades del usuari connectat a la constant user
        val user = Firebase.auth.currentUser

        //Guarda el mail del usuari que ha fet login
        val mail = user?.email.toString()

        //Consulta per extreure el nickname per guardar-lo al document tiquet
        val usuaris = db.collection("usuaris")
        val query = usuaris.whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val usuari = document.toObjects(UsuariDC::class.java)

                    binding.apply {
                        editTextNomModificar.setText(usuari[0].nom)
                        editTextPoblacioModificar.setText(usuari[0].poblacio)
                        editTextTelefonModificar.setText(usuari[0].telefon)
                        editTextContrasenyaModificar.setText("******")
                    }
                }
            }

            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

    }


}
