package cat.copernic.bookswap.veurellibre

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentVeureLlibreBinding
import cat.copernic.bookswap.llistatllibres.LlistatLlibresArgs
import cat.copernic.bookswap.utils.UsuariDC
import coil.api.load
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class VeureLlibre : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var args: LlistatLlibresArgs
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


        return binding.root
    }
    private fun mostrarDadesLlibre() {

        // Agafem els argumetns del fragment LlistatLlibres.kt
        args = LlistatLlibresArgs.fromBundle(requireArguments())

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

                    Log.d("valoraciousuari",usuari[0].valoracio.toString())
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

}