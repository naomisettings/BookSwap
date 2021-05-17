package cat.copernic.bookswap.modificarusuari

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


class ModificarUsuari : Fragment() {

    private lateinit var binding: FragmentModificarUsuariBinding

    private val db = FirebaseFirestore.getInstance()
    //Guarda les dades del usuari connectat a la constant user
    private val user = Firebase.auth.currentUser

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

    private fun mostrarDades() {

        //Guarda el mail del usuari que ha fet login
        val mail = user?.email.toString()

        //Consulta per extreure el les dades del usuari segons el mail
            db.collection("usuaris").whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                document?.forEach {
                    val usuari = document.toObjects(UsuariDC::class.java)

                    //Mostra les dades del usuari als editText
                    binding.apply {
                        editTextNomModificar.setText(usuari[0].nom)
                        editTextPoblacioModificar.setText(usuari[0].poblacio)
                        editTextTelefonModificar.setText(usuari[0].telefon)
                        editTextContrasenyaModificar.setText("******")
                    }

                    //Extreu la id del document
                    val usuariId = it.id

                    binding.bttnActualitzar.setOnClickListener {

                        alertaModificarDades(usuariId)


                        //Actualitza la contrasenya de l'usuari
                        modificarContrasenya()
                    }

                    binding.bttnBaixaUsuari.setOnClickListener{
                        alertaBaixaUsuari(usuariId)
                    }

                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun modificarDades(usuariId: String) {

        //agafem l'usuari de la collecio amb el seu ID a aquesta variable
        val sfDocRef = db.collection("usuaris").document(usuariId)


        db.runTransaction { transaction ->

            transaction.update(
                sfDocRef,
                "nom",
                binding.editTextNomModificar.text.toString()
            )
            transaction.update(
                sfDocRef,
                "poblacio",
                binding.editTextPoblacioModificar.text.toString()
            )
            transaction.update(
                sfDocRef,
                "telefon",
                binding.editTextTelefonModificar.text.toString()
            )

            null
        }.addOnSuccessListener {
            Log.d("TAG", "Transaction success!")
            view?.let {
                Snackbar.make(
                    it,
                    "Dades modificades correctament",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
            .addOnFailureListener { e ->
                Log.w("TAG2", "Transaction failure.", e)
                view?.let {
                    Snackbar.make(it, "Error al modificar les dades", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    private fun modificarContrasenya(){
        val contrasenya = binding.editTextContrasenyaModificar.text.toString()

        user?.let {
            if (contrasenya != "******") {
                // Modifica la contrasenya
                user.updatePassword(contrasenya)
            }
        }
    }

    private fun baixaUsuari(usuariId: String) {
        val sfDocRef = db.collection("usuaris").document(usuariId)

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

    private fun alertaModificarDades(usuariId: String) {

        val dialog = context?.let {
            AlertDialog.Builder(it)
                .setIcon(R.drawable.bookswaplogo)
                .setTitle(user!!.email)
                .setMessage(R.string.alert_modificar_dades)
                .setNegativeButton(R.string.acceptar) { view, _ ->

                    //Truca a la funció modificar dades
                    modificarDades(usuariId)
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
    private fun alertaBaixaUsuari(usuariId: String) {
        val dialog = context?.let {
            AlertDialog.Builder(ContextThemeWrapper(it, R.style.AlertDialogCustom))
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
}
