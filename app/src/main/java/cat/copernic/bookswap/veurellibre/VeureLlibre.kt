package cat.copernic.bookswap.veurellibre

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentVeureLlibreBinding
import cat.copernic.bookswap.utils.UsuariDC
import cat.copernic.bookswap.viewmodel.ViewModel
import coil.api.load
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class VeureLlibre : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var args: VeureLlibreArgs
    private lateinit var binding: FragmentVeureLlibreBinding
    private val viewModel: ViewModel by viewModels()


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
        binding.bttnTelefon.setOnClickListener {
            contactarTelefon()
        }
        //enviem les dades del llibre actual al fragment valorar usuari
        binding.bttnValorar.setOnClickListener { view: View ->
            view.findNavController().navigate(
                VeureLlibreDirections.actionVeureLlibreToValoracionsFragment(
                    args.titol,
                    args.assignatura,
                    args.editorial,
                    args.curs,
                    args.estat,
                    args.foto,
                    args.id,
                    args.mail
                )
            )

        }
        return binding.root
    }

    //funcio trucar a l'usuari que ha publicat el llibre
    private fun contactarTelefon() {
        val mail = args.mail
        //enviem a la consulta mitjançant el viewModel el mail de l'usuari
        viewModel.usuariPublicat(mail).observe(requireActivity(), { usuari ->

            try {
                //assignem el telefon de l'usuari que ha publicat el llibre
                val telefonUsuari = "+34" + usuari.telefon
                //assignem un missatge per defecte amb el titol del llibre
                Log.i("telefonUsuari", telefonUsuari)
                val sendIntent = Intent()
                //enviem l'acció de trucada amb el telefon de l'usuari
                sendIntent.action = Intent.ACTION_DIAL
                sendIntent.setData(Uri.parse("tel:" + telefonUsuari))

                startActivity(sendIntent)

            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "No tens acces a les trucades.", Toast.LENGTH_SHORT
                ).show()
            }


        })
    }

    //funcio per enviar un WhatsApp a l'usuari que ha publicat el llibre
    private fun contactarWhatsApp() {
        val mail = args.mail
        //enviem a la consulta mitjançant el viewModel el mail de l'usuari
        viewModel.usuariPublicat(mail).observe(requireActivity(), { usuari ->
                try {
                    //assignem el telefon de l'usuari que ha publicat el llibre
                    val telefonUsuari = "+34" + usuari.telefon
                    //assignem un missatge per defecte amb el titol del llibre
                    val missatge =
                        "Hola,\n" + "M'agradaria rebre informació d'aquest llibre: \n" + args.titol
                    Log.i("telefonUsuari", telefonUsuari)
                    val sendIntent = Intent()
                    //enviem el missatge per whatsapp
                    sendIntent.action = Intent.ACTION_VIEW
                    sendIntent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + telefonUsuari + "&text=" + missatge))
                    startActivity(sendIntent)
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(
                        requireContext(),
                        "No tens acces al WhatsApp.", Toast.LENGTH_SHORT
                    ).show()
                }

            })

    }

    //funcio per enviar un mail a l'usuari que ha publicat el llibre
    private fun contactarMail() {
        val mail = args.mail
        //guardem el mail del destinatari amb el format correcte
        val formatMail = mail.replace("%", "@")
        val to = formatMail
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        //assignem el mail del destinatari
        sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        //assignem l'assumpte del mail amb el titol del llibre
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, args.titol)
        //assignem el missatge per defecte
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Hola,\n" + "M'agradaria rebre informació d'aquest llibre: \n" + args.titol
        )
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

    //funcio que mostre les dades del llibre seleccionat al fragment
    private fun mostrarDadesLlibre() {

        // Agafem els argumetns del fragment LlistatLlibres.kt
        args = VeureLlibreArgs.fromBundle(requireArguments())
        //carreguem les dades del llibre seleccionat al llistat
        binding.apply {
            edAssignatura.text = args.assignatura
            editCurs.text = args.curs
            editEditorial.text = args.editorial
            txtTitolAfegirLlibre.text = args.titol

        }
    }

    //funcio que mostra la imatge del llibre
    private fun mostrarImatgePicasso() {

        //Descarregar la imatge amb picasso
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${args.foto}")

        //Monstrar la imatge
        imageRef.downloadUrl.addOnSuccessListener { url ->
            binding.imageViewLlibre.load(url)
        }.addOnFailureListener {

        }
    }

    //funcio per veure la valoració de l'usuari
    private fun consultaPuntuacioUsuariLlibre() {
        val mail = args.mail
        Log.d("valoraciousuari", args.mail)
        //enviem a la consulta mitjançant el viewModel el mail de l'usuari
        viewModel.usuariPublicat(mail).observe(requireActivity(), { usuari ->
                val valoracioUsuari = usuari.valoracio
                //assignem el valor de la puntuacio que te l'usuari 
                binding.ratingBarPuntuacio.rating = valoracioUsuari.toFloat()
                Log.i("valoraciousuari", valoracioUsuari.toString())
            })
    }

}