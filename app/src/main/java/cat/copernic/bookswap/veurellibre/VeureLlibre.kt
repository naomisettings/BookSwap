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
import cat.copernic.bookswap.llistatllibres.LlistatLlibres
import cat.copernic.bookswap.llistatllibres.LlistatLlibresArgs
import cat.copernic.bookswap.modificarllibre.ModificarLlibreArgs
import cat.copernic.bookswap.utils.Llibre
import cat.copernic.bookswap.utils.Llibres
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class VeureLlibre : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var args: LlistatLlibresArgs

    //guardem les dades del usuari identificat
    private val user = Firebase.auth.currentUser

    private lateinit var binding: FragmentVeureLlibreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_veure_llibre, container, false)

        mostrarDadesLlibre()


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

}