package cat.copernic.bookswap.modificarusuari

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentModificarUsuariBinding
import cat.copernic.bookswap.utils.UsuariDC
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class ModificarUsuari : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentModificarUsuariBinding

    private val db = FirebaseFirestore.getInstance()

    //Guarda les dades del usuari connectat a la constant user
    private val user = Firebase.auth.currentUser

    private lateinit var spinner: Spinner
    private var poblacio = ""

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_modificar_usuari, container, false)


        // Mostra les dades del usuari als editText perque l'usuari les modifiqui
        mostrarDades()

        binding.bttnSingOut.setOnClickListener {
            //SingOut
            Firebase.auth.signOut()
            //Obra el fragment principal
            findNavController().navigate(R.id.action_modificarUsuari_to_loginFragment)
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun mostrarDades() {

        //Guarda el mail del usuari que ha fet login
        val mail = user?.email.toString()

        //Consulta per extreure el les dades del usuari segons el mail
        db.collection("usuaris").whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                document?.forEach { doc ->
                    val usuari = document.toObjects(UsuariDC::class.java)

                    //Mostra les dades del usuari als editText
                    binding.apply {
                        editTextNomModificar.setText(usuari[0].nom)
                        editTextTelefonModificar.setText(usuari[0].telefon)
                        editTextContrasenyaModificar.setText("******")
                    }

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

                            //Obtenir la poblacio preestablerta per l'usuari
                            val posicioSpinner = adapter.getPosition(usuari[0].poblacio)

                            //Assignar l'adapter al spinner
                            spinner.adapter = adapter

                            //Assignar la posició de la població preestagblerta al spinner
                            spinner.setSelection(posicioSpinner)
                        }
                    }

                    //Permet seleccionar un camp del spinner
                    spinner.onItemSelectedListener = this

                    //Extreu la id del document
                    val usuariId = doc.id

                    binding.bttnActualitzar.setOnClickListener {

                        //Dins aquesta funció es truca a modificarDadesBBDD i a modificar contrasenya
                        alertaModificarDades(usuariId)
                    }

                    if (usuari[0].admin){
                        binding.bttnBaixaUsuari.visibility = View.INVISIBLE
                        binding.bttnEsborrarUsuaris.visibility = View.VISIBLE
                    }

                    binding.bttnBaixaUsuari.setOnClickListener {
                        alertaBaixaUsuari(usuariId)
                    }

                    binding.bttnEsborrarUsuaris.setOnClickListener{
                        findNavController().navigate(R.id.action_modificarUsuari_to_esborrarUsuaris)

                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun modificarDades(usuariId: String) {

        //agafem l'usuari de la collecio amb el seu ID a aquesta variable
        val sfDocRef = db.collection("usuaris").document(usuariId)

        //Obté la població del spinner
        poblacio = spinner.selectedItem.toString()

        db.runTransaction { transaction ->

            transaction.update(
                sfDocRef,
                "nom",
                binding.editTextNomModificar.text.toString()
            )
            transaction.update(
                sfDocRef,
                "poblacio",
                poblacio
            )
            transaction.update(
                sfDocRef,
                "telefon",
                binding.editTextTelefonModificar.text.toString()
            )

            null
        }.addOnSuccessListener {
            Log.d("TAG", "Transaction success!")
            /*view?.let {
                val snackbar = Snackbar.make(it, R.string.dadesCorrectes, Snackbar.LENGTH_LONG).apply {
                    val layoutParams = ActionBar.LayoutParams(this.view.layoutParams)
                    layoutParams.gravity = Gravity.BOTTOM
                    view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                    view.layoutParams = layoutParams
                }
                snackbar.setTextColor(Color.parseColor("#000000"))
                snackbar.show()
            }
             */

        }.addOnFailureListener { e ->
            Log.w("TAG2", "Transaction failure.", e)
        }

        db.collection("llibres").whereEqualTo("mail", user?.email).get()
            .addOnSuccessListener { doc ->

                doc?.forEach {
                    val sfDocRefLlibres = db.collection("llibres").document(it.id)

                    db.runTransaction { transaction ->

                        transaction.update(
                            sfDocRefLlibres,
                            "poblacio",
                            poblacio
                        )

                        null
                    }.addOnSuccessListener {
                        Log.d("TAG", "Transaction success!")
                    }.addOnFailureListener { exception ->
                        Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                    }
                }
            }
    }

    private fun modificarContrasenya() {
        val contrasenya = binding.editTextContrasenyaModificar.text.toString()

        user?.let {
            if (contrasenya != "******") {
                // Modifica la contrasenya
                user.updatePassword(contrasenya)
            }
        }
    }

    //Esborra els llibres publicats per l'usuari i el dona de baixa
    private fun baixaUsuari(usuariId: String) {

        val sfDocRef = db.collection("usuaris").document(usuariId)

        val mail = user?.email

        //Consulta per extreure el les dades dels llibres del usuari segons el mail
        db.collection("llibres").whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                document?.forEach {

                    //Extreu la id del document
                    val llibresId = it.id

                    val sfDocRefLlibres = db.collection("llibres").document(llibresId)

                    //Esborra els llibres publicats per l'usuari
                    db.runTransaction { transaction ->
                        //agafem el ID
                        val snapshot = transaction.get(sfDocRefLlibres)
                        //actualitzem el id amb el mail del usuari identificat i guardem els camps
                        snapshot.getString("mail")!!

                        //esborrem usuari del Firestore
                        transaction.delete(sfDocRefLlibres)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        // Esborra les dades del usuari i l'usuari
        db.runTransaction { transaction ->
            //agafem el ID
            val snapshot = transaction.get(sfDocRef)
            //actualitzem el id amb el mail del usuari identificat i guardem els camps
            snapshot.getString("mail")!!

            //esborrem usuari del Firestore
            transaction.delete(sfDocRef)

            //esborrem usuari del Authentification
            user?.delete()
        }

        findNavController().navigate(R.id.action_modificarUsuari_to_loginFragment)

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun alertaModificarDades(usuariId: String) {

        if (binding.editTextNomModificar.text.toString().isNotEmpty()) {
            //Comprova els edit text tenen valors
            val telefon = binding.editTextTelefonModificar.text.toString()
            if (telefon.isNotEmpty()) {

                //Comprova que el telèfon sigui un número (Es guarda com a String)
                val regex = Regex(pattern = """\d{9}""")
                if (regex.matches(input = telefon)) {

                    val positionSpinner = spinner.selectedItemPosition

                    //Comprovar que la posició del spinner no és zero perque la posició 0 és
                    //"seleccionar Població"
                    if (positionSpinner != 0) {


                        val dialog = context?.let {
                            AlertDialog.Builder(it)
                                .setIcon(R.drawable.bookswaplogo)
                                .setTitle(user!!.email)
                                .setMessage(R.string.alert_modificar_dades)
                                .setNegativeButton(R.string.acceptar) { view, _ ->

                                    //Truca a la funció modificar dades
                                    modificarDades(usuariId)
                                    //Actualitza la contrasenya de l'usuari
                                    modificarContrasenya()
                                    view.dismiss()
                                }
                                .setPositiveButton(R.string.cancelar) { view, _ ->
                                    view.cancel()
                                }
                                .setCancelable(false)
                                .create()
                        }

                        dialog!!.show()

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
        } else {
            Snackbar.make(
                requireActivity().findViewById(R.id.myNavHostFragment),
                getString(R.string.canvi_nom),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun alertaBaixaUsuari(usuariId: String) {

        val dialog = context?.let {
            AlertDialog.Builder(it)
                .setIcon(R.drawable.bookswaplogo)
                .setTitle(user!!.email)
                .setMessage(R.string.alert_baixa_dades)
                .setNegativeButton(R.string.acceptar) { view, _ ->

                    //Truca a la funció baixaUsuari
                    baixaUsuari(usuariId)
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
