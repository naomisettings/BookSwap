package cat.copernic.bookswap.login

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentLoginBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private val AUTH_REQUEST_CODE = 2002
    private var user: FirebaseUser? = null

    //instancia a firebase
    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentLoginBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        binding.bttnRegistre.setOnClickListener() {
            findNavController().navigate(R.id.action_loginFragment_to_registreFragment)
        }

        login()

        comprovarUsuari()

        return binding.root
    }

    //Funció de login
    private fun login() {
        //Diferents proveidors de registre i login
        val proveedors = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        //Obra l'activity del registre i login
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(proveedors)
                .setLogo(R.drawable.bookswaplogo)
                .build(),
            AUTH_REQUEST_CODE
        )

    }

    //Comporvació del codi per obrir la onActiviyResult del login
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_REQUEST_CODE) {
            FirebaseAuth.getInstance().currentUser
        }
    }

    //Obtenir el nom i el mail de l'usuari que s'ha loginat o registrat.
    private fun comprovarUsuari() {

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Nom i adreça electrònica del usuari
            val mail = user.email
            val nom = user.displayName

            comprovarBBDD(mail, nom)

            //Obra el fragment principal
            findNavController().navigate(R.id.action_loginFragment_to_llistatLlibres)
        }
    }

    private fun comprovarBBDD(mail: String, nom: String) {

        db.collection("usuaris").whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                val usuarisDC = document.toObjects(UsuarisDC::class.java)

                if (usuarisDC.isNullOrEmpty()) {
                    guardarUsuariBBDD(mail, nom)
                }
            }
    }

    private fun guardarUsuariBBDD(mail: String, nom: String) {
        val usuaris = hashMapOf(
            "mail" to mail,
            "nom" to nom,
        )

        db.collection("usuaris").add(usuaris)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

            }.addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }
}

data class UsuarisDC(
    var mail: String = "",
    var nom: String = ""
)