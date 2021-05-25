package cat.copernic.bookswap.veurellibre

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentVeureLlibreBinding
import cat.copernic.bookswap.utils.UsuariDC
import coil.api.load
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class VeureLlibre : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var args: VeureLlibreArgs
    private lateinit var binding: FragmentVeureLlibreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_veure_llibre, container, false)

        mostrarDadesLlibre()

        mostrarImatgePicasso()

        consultaPuntuacioUsuariLlibre()

        binding.bttnWhatsapp.setOnClickListener {
            contactarWhatsApp()
        }
        binding.bttnMail.setOnClickListener {
            contactarMail()
        }

        binding.bttnValorar.setOnClickListener { view: View ->
            view.findNavController().navigate(VeureLlibreDirections.actionVeureLlibreToValoracionsFragment(
                args.titol,
                args.assignatura,
                args.editorial,
                args.curs,
                args.estat,
                args.foto,
                args.id,
                args.mail))

        }


        return binding.root
    }

    private fun contactarWhatsApp() {
        //Consulta per extreure el les dades del usuari que ven el llibre segons el mail
        db.collection("usuaris").whereEqualTo("mail", args.mail).get()
            .addOnSuccessListener { document ->
                val usuari = document.toObjects(UsuariDC::class.java)
                val telefonUsuari = usuari[0].telefon
                Log.i("telefonUsuari", telefonUsuari)
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.type = "text/plain"
                sendIntent.setPackage("com.whatsapp")
                //assignem el mail del destinatari
                sendIntent.putExtra(Intent.ACTION_DIAL, Uri.parse(telefonUsuari))
                /*val callIntent: Intent = Uri.parse(telefonUsuari).let { number ->
                    Intent(Intent.ACTION_DIAL, number)

                }*/
                startActivity(sendIntent)
            }

    }

    //funcio que obre una aplicacio externa per contactar amb l'usuari
    private fun contactarMail() {
        val args = VeureLlibreArgs.fromBundle(requireArguments())
        //guardem el mail del destinatari amb el format correcte
        val mail = args.mail
        val formatMail = mail.replace("%","@")
        val to = formatMail
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        //assignem el mail del destinatari
        sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        //assignem l'assumpte del mail amb el titol del llibre
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, args.titol)
        //assignem el missatge per defecte
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hola,\n" + "M'agradaria rebre informació d'aquest llibre: \n" + args.titol)
        startActivity(sendIntent)
        //si l'usuari no te instal.lat un gestor de correu
        try {
            startActivity(Intent.createChooser(sendIntent, "Enviar email..."))

        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "No tens gestor de correu instal.lats.", Toast.LENGTH_SHORT
            ).show()
        }

    }


    private fun mostrarDadesLlibre() {

        // Agafem els argumetns del fragment LlistatLlibres.kt
        args = VeureLlibreArgs.fromBundle(requireArguments())

        binding.apply {
            edAssignatura.text = args.assignatura
            editCurs.text = args.curs
            editEditorial.text = args.editorial
            txtTitolAfegirLlibre.text = args.titol

        }
    }

    private fun mostrarImatgePicasso(){

        //Descarregar la imatge amb picasso
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${args.foto}")

        //Monstrar la imatge
        imageRef.downloadUrl.addOnSuccessListener { url ->
            binding.imageViewLlibre.load(url)
        }.addOnFailureListener {

        }
    }

    private fun consultaPuntuacioUsuariLlibre(){

        Log.d("valoraciousuari",args.mail)

        //Consulta per extreure el les dades del usuari que ven el llibre segons el mail
        db.collection("usuaris").whereEqualTo("mail", args.mail).get()
            .addOnSuccessListener { document ->
                    val usuari = document.toObjects(UsuariDC::class.java)
                    val valoracioUsuari = usuari[0].valoracio
                    //assignem el valor de la puntuacio que te l'usuari -1 perque compta des des 0
                    binding.ratingBarPuntuacio.rating = valoracioUsuari.toFloat() - 1
                    Log.i("valoraciousuari",valoracioUsuari.toString())
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

}