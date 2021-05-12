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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

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


        val navBar: BottomNavigationView =
            requireActivity().findViewById(R.id.nav_view)

        navBar.visibility = View.GONE

        login()

        edTextInvisibles()

        binding.bttnGuardar.setOnClickListener {

            telefon = binding.edTxtTelefon.text.toString()
            poblacio = binding.edTxtPoblacio.text.toString()

            if (!(telefon.isEmpty() || poblacio.isBlank())) {

                val regex = Regex(pattern = """\d{9}""")
                if (regex.matches(input = telefon)) {

                    guardarUsuariBBDD()
                    visibilitatNavigationBotton()

                } else {
                    Snackbar.make(
                        requireActivity().findViewById(R.id.myNavHostFragment),
                        getString(R.string.telefonoksnack),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(
                    requireActivity().findViewById(R.id.myNavHostFragment),
                    getString(R.string.dadesomplertes),
                    Snackbar.LENGTH_LONG
                ).show()
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

            visibilitatNavigationBotton()
        }
    }


    //Insert a la taula usuaris del mail i el nom
    private fun guardarUsuariBBDD() {

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val mail = user.email
            val nom = user.displayName

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

                }.addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }
        }
    }

    private fun visibilitatNavigationBotton() {
        Log.d("entraaa", "ss")
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Obté adreça electrònica del usuari
            val mail = user.email

            db.collection("usuaris").whereEqualTo("mail", mail).get()
                .addOnSuccessListener { document ->
                    //val usuarisDC = document.toObjects(UsuarisDC::class.java)
                    if (document.isEmpty) {


                        Thread.sleep(500)
                        edTextVisibles()

                    } else {
                        edTextInvisibles()
                        val navBar: BottomNavigationView =
                            requireActivity().findViewById(R.id.nav_view)

                        navBar.visibility = View.VISIBLE
                    }
                }
        }
    }

    private fun edTextInvisibles() {
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

    private fun edTextVisibles() {
        binding.apply {
            edTxtTelefon.isVisible = true
            edTxtPoblacio.isVisible = true
            txtTelefon.isVisible = true
            txtPoblacio.isVisible = true
            bttnGuardar.isVisible = true

            txtObligacio.isVisible = true

            txtBenvinguda.isVisible = false

            val navBar: BottomNavigationView =
                requireActivity().findViewById(R.id.nav_view)

            navBar.visibility = View.GONE
        }
    }
}

data class UsuarisDC(
    var mail: String = "",
    var nom: String = "",
    var telefon: String = "",
    var poblacio: String = ""
)