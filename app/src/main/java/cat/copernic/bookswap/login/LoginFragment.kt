package cat.copernic.bookswap.login

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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

//Codi per l'activity auxiliar del login
private const val AUTH_REQUEST_CODE = 2002

class LoginFragment : Fragment(), AdapterView.OnItemSelectedListener {

    lateinit var binding: FragmentLoginBinding

    //Telèfon i població extrets de l'usuari loginat
    private var telefon = ""
    private var poblacioStr: String = ""

    //instancia a firebase
    private val db = FirebaseFirestore.getInstance()

    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        //Visibilitat del menú inferior
        val navBar: BottomNavigationView =
            requireActivity().findViewById(R.id.nav_view)

        //Visibilitat del menú inferiro
        navBar.visibility = View.GONE

        login()

        //Fer editTextInvisibles
        edTextInvisibles()

        //Inicialitzar spinner
        spinner = binding.spnPoblacio

        //Crear l'adapter per el spinner
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.poblacions,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        }

        //Permet seleccionar un camp del spinner
        spinner.onItemSelectedListener = this

        botoGuardarDades()

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

            //Fa una consulta per saber si l'usuari està ja registrat
            visibilitatNavigationBotton()
        }
    }

    private fun botoGuardarDades() {

        binding.bttnGuardar.setOnClickListener {

            telefon = binding.edTxtTelefon.text.toString()

            //Comprova els edit text tenen valors
            if (telefon.isNotEmpty()) {

                //Comprova que el telèfon sigui un número (Es guarda com a String)
                val regex = Regex(pattern = """\d{9}""")
                if (regex.matches(input = telefon)) {


                    //Obté el camp seleccionat del spinner
                    poblacioStr = spinner.selectedItem.toString()

                    val positionSpinner = spinner.selectedItemPosition

                    //Comprovar que la posició del spinner no és zero perque la posició 0 és
                    //"seleccionar Població"
                    if (positionSpinner != 0) {
                        guardarUsuariBBDD()

                        //Fa una consulta per saber si l'usuari està ja registrat
                        visibilitatNavigationBotton()

                    } else {
                        Snackbar.make(
                            requireActivity().findViewById(R.id.myNavHostFragment),
                            getString(R.string.poblacioIncompleta),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
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
    }

    //Insert a la taula usuaris el mail, nom, telefon i població
    private fun guardarUsuariBBDD() {

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val mail = user.email
            val nom = user.displayName

            //El telèfon i la població s'extreu dels edit text declarats com a variables
            //a la classe
            val usuaris = hashMapOf(
                "mail" to mail,
                "nom" to nom,
                "telefon" to telefon,
                "poblacio" to poblacioStr,
                "valoracio" to 0,
                "comptador_valoracions" to 0
            )

            //Insert
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

    //Segons si l'uauari ja ha introduït el telèfon i la població, mostrarà un missatge de
//benvinguda o els editText per introduir aquestes dades.
    private fun visibilitatNavigationBotton() {

        //Saber quin usuari està loginat
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Obté adreça electrònica del usuari
            val mail = user.email

            db.collection("usuaris").whereEqualTo("mail", mail).get()
                .addOnSuccessListener { document ->

                    //Comprova si el document està buit o hi ha dades
                    if (document.isEmpty) {

                        //Fa els editTextVisibles (estaven invisibles)
                        edTextVisibles()

                    } else {

                        //Fa el navBar visible
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
            spnPoblacio.isVisible = false
            txtTelefon.isVisible = false
            txtPoblacioVeureLl.isVisible = false
            bttnGuardar.isVisible = false

            txtObligacio.isVisible = false

            txtBenvinguda.isVisible = true
        }
    }

    private fun edTextVisibles() {
        binding.apply {
            edTxtTelefon.isVisible = true
            spnPoblacio.isVisible = true
            txtTelefon.isVisible = true
            txtPoblacioVeureLl.isVisible = true
            bttnGuardar.isVisible = true

            txtObligacio.isVisible = true

            txtBenvinguda.isVisible = false

            val navBar: BottomNavigationView =
                requireActivity().findViewById(R.id.nav_view)

            navBar.visibility = View.GONE
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }


}