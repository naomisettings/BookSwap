package cat.copernic.bookswap.login

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentLoginBinding
import cat.copernic.bookswap.utils.Usuari
import cat.copernic.bookswap.viewmodel.Consultes
import cat.copernic.bookswap.viewmodel.ViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

//Codi per l'activity auxiliar del login
private const val AUTH_REQUEST_CODE = 2002

class LoginFragment : Fragment(), AdapterView.OnItemSelectedListener {

    lateinit var binding: FragmentLoginBinding

    private val viewModel: ViewModel by viewModels()

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

        //Amagar menú inferior
        navBar.visibility = View.GONE

        //Funció pel registre o el login
        login()

        //Fer editTextInvisibles
        edTextInvisibles()

        //Inicialitzar spinner
        spinner = binding.spnPoblacio

        //Mostra el spinner de les poblacions
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

        //Botó per guardar dades
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
        //Obra l'activity for result del registre i login
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
            val usuari = hashMapOf(
                "mail" to mail,
                "nom" to nom,
                "telefon" to telefon,
                "poblacio" to poblacioStr,
                "valoracio" to 0,
                "comptador_valoracions" to 0,
                "admin" to false,
                "expulsat" to false
            )

            viewModel.insertarUsuari(usuari).observe(requireActivity(), { insertatOk ->
                Log.i("Usuari Insertat", insertatOk.toString())
            })
        }
    }

    //Segons si l'uauari ja ha introduït el telèfon i la població, mostrarà un missatge de
    //benvinguda o els editText per introduir aquestes dades.
    private fun visibilitatNavigationBotton() {

        val user = Firebase.auth.currentUser
        FirebaseFirestore.getInstance()
            .collection("usuaris").whereEqualTo("mail", user?.email)
            .get()
            .addOnSuccessListener {

                val usuariDC = it.toObjects(Usuari::class.java)
            //Comprova si el document està buit o hi ha dades
            if (!usuariDC.isEmpty()) {
                //L'adiministrador ha d'haber expulsat l'usuari al fragment EsborrarUsuaris
                if (usuariDC[0].expulsat) {
                    edTextVisibles()
                    //Fa invisible el botó guardar perque l'usuari no pugui entrar a l'aplicació
                    binding.bttnGuardar.visibility = View.INVISIBLE
                    alertaExpulsat()

                } else {
                    //Fa el navBar visible
                    edTextInvisibles()
                    val navBar: BottomNavigationView =
                        requireActivity().findViewById(R.id.nav_view)

                    navBar.visibility = View.VISIBLE
                }
            }else{
                edTextVisibles()
                binding.bttnGuardar.visibility = View.VISIBLE
            }
        }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)

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

    private fun alertaExpulsat() {

        val dialog = context?.let {
            AlertDialog.Builder(it)
                .setIcon(R.drawable.bookswaplogo)
                .setTitle(R.string.expulsatTitol)
                .setMessage(R.string.expulsat)
                .setNegativeButton(R.string.acceptar) { view, _ ->
                    view.dismiss()
                }
                .setPositiveButton(R.string.cancelar) { view, _ ->
                    view.cancel()
                }
                .setCancelable(false)
                .create()
        }
        dialog!!.show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }


}