package cat.copernic.bookswap.login

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentLoginBinding
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginFragment : Fragment() {

    private val AUTH_REQUEST_CODE = 2002

    lateinit var binding: FragmentLoginBinding

    private var telefon = ""
    private var poblacio = ""

    //instancia a firebase
    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        login()

        edTextInvisibles()

        visibilitatNavigationBotton()

        binding.bttnGuardar.setOnClickListener {

            telefon = binding.edTxtTelefon.text.toString()
            poblacio = binding.edTxtPoblacio.text.toString()

            if (!(telefon.isEmpty() || poblacio.isBlank())) {

                val regex = Regex(pattern = """\d{9}""")
                if (regex.matches(input = telefon)) {

                    comprovarUsuari()

                    edTextInvisibles()

                    val navBar: BottomNavigationView =
                        requireActivity().findViewById(R.id.nav_view)
                    navBar.visibility = View.VISIBLE

                }
            }
        }

        return binding.root
    }

    //Funció de login
    private fun login() {
        //Diferents proveidors de registre i login
        val proveedors = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )
        //Obra l'activity del registre i login
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(proveedors)
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
            // Obté el nom i adreça electrònica del usuari
            val mail = user.email
            val nom = user.displayName

            //Truca a la funció que comprova si l'usuari ja està a la base de dades
            comprovarBBDD(mail, nom)

        }
    }

    //Funció que comprova si l'usuari ja està a la base de dades
    private fun comprovarBBDD(mail: String, nom: String) {

        //Consulta de la taula usuaris (where mail == mail)
        db.collection("usuaris").whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                val usuarisDC = document.toObjects(UsuarisDC::class.java)

                //En el casa que no hi hagi cap mail guardat d'aquest usuari a la bbdd, s'afageix amb
                //la funció guardarUsuariBBDD
                if (usuarisDC.isNullOrEmpty()) {
                    guardarUsuariBBDD(mail, nom)
                }
            }
    }

    //Insert a la taula usuaris del mail i el nom
    private fun guardarUsuariBBDD(mail: String, nom: String) {
        val usuaris = hashMapOf(
            "mail" to mail,
            "nom" to nom,
            "telefon" to telefon,
            "poblacio" to poblacio
        )

        db.collection("usuaris").add(usuaris)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    ContentValues.TAG,
                    "DocumentSnapshot added with ID: ${documentReference.id}"
                )
                val navBar: BottomNavigationView =
                    requireActivity().findViewById(R.id.nav_view)
                navBar.visibility = View.VISIBLE

            }.addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    private fun visibilitatNavigationBotton() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Obté adreça electrònica del usuari
            val mail = user.email

            db.collection("usuaris").whereEqualTo("mail", mail).get()
                .addOnSuccessListener { document ->
                    val usuarisDC = document.toObjects(UsuarisDC::class.java)
                    if (usuarisDC.isNullOrEmpty()) {

                        val navBar: BottomNavigationView =
                            requireActivity().findViewById(R.id.nav_view)
                        navBar.visibility = View.GONE

                        edTextVisibles()

                        Snackbar.make(
                            requireActivity().findViewById(R.id.myNavHostFragment),
                            "getString(R.string.something_went_wrong)",
                            Snackbar.LENGTH_LONG
                        ).show()

                    } else {
                        Log.d("entraa","entraa")
                        val navBar: BottomNavigationView =
                            requireActivity().findViewById(R.id.nav_view)
                        navBar.visibility = View.VISIBLE
                    }
                }
        }
    }
    private fun edTextInvisibles(){
        binding.apply {
            edTxtTelefon.isVisible = false
            edTxtPoblacio.isVisible = false
            txtTelefon.isVisible = false
            txtPoblacio.isVisible = false
            bttnGuardar.isVisible = false

            txtObligacio.isVisible = false

            txtBenvinguda.isVisible = true
        }
    }

    private fun edTextVisibles(){
        binding.apply {
            edTxtTelefon.isVisible = true
            edTxtPoblacio.isVisible = true
            txtTelefon.isVisible = true
            txtPoblacio.isVisible = true
            bttnGuardar.isVisible = true

            txtObligacio.isVisible = true

            txtBenvinguda.isVisible = false
        }
    }
}

data class UsuarisDC(
    var mail: String = "",
    var nom: String = "",
    var telefon: String = "",
    var poblacio: String = ""
)