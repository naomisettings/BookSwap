package cat.copernic.bookswap.modificarllibre

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentAfegirLlibreBinding
import cat.copernic.bookswap.databinding.FragmentModificarLlibreBinding
import cat.copernic.bookswap.utils.Llibres
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ModificarLlibre : Fragment() {
    //instancia a firebase
    val db = FirebaseFirestore.getInstance()
    private lateinit var binding: FragmentModificarLlibreBinding
    private lateinit var args: ModificarLlibreArgs


    private lateinit var spinnerModificarEstat: Spinner
    private lateinit var spinnerModificarCurs: Spinner
    private lateinit var spinnerModificarAssignatura: Spinner
    private var estat = ""
    private var curs = ""
    private var assignatura=""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_modificar_llibre, container, false)
        //rebre dades del llibre seleccionat
        args = ModificarLlibreArgs.fromBundle(requireArguments())
        var idLlibre = args.id
        Log.i("llibre", idLlibre)
        //funcio per carregar les dades del llibre seleccionat
        mostrarLlibre()


        binding.btnActualitzarLlibre.setOnClickListener {
            actualitzarLlibre(args.id)
            view?.findNavController()?.navigate(R.id.action_modificarLlibre_to_meusLlibres)
        }


        return binding.root
    }

    private fun actualitzarLlibre(idLlibre:String) {
        //guardem el id del llibre
        var idLlibre = args.id
       // Log.d("id", idLlibre)
       //agafem el llibre de la coleccio amb el seu ID
        Log.i("idLlibre", idLlibre)

                     //agafem el id del tiquet
                     val sfDocRef = db.collection("llibres").document(idLlibre)

                    Log.i("idLlibre", idLlibre)

                     //agafem els valors dels spinners
                     assignatura = spinnerModificarAssignatura.toString()
                     curs = spinnerModificarCurs.selectedItem.toString()
                     estat = spinnerModificarEstat.selectedItem.toString()

                     //actualitzem les dades
                     db.runTransaction { transaction ->
                         val snapshot = transaction.get(sfDocRef)
                         transaction.update(sfDocRef,"titol", binding.editTextTitolModificar.text.toString())
                         transaction.update(sfDocRef, "assignatura", assignatura)
                         transaction.update(sfDocRef, "editorial", binding.editTextEditorialModificar.text.toString())
                         transaction.update(sfDocRef, "curs", curs)
                         transaction.update(sfDocRef, "estat", estat)
                         // transaction.update(sfDocRef, "foto", foto)

                     }.addOnSuccessListener {
                         Log.d("TAG", "Transaction success!")
                         view?.let {
                             Snackbar.make(
                                 it,
                                 "Llibre actualitzat",
                                 Snackbar.LENGTH_LONG
                             ).show()
                         }
                     }.addOnFailureListener { e ->
                         Log.w("TAG2", "Transaction failure.", e)
                         view?.let {

                             Snackbar.make(it, "Error a l'actualitzar el llibre", Snackbar.LENGTH_LONG)
                                 .show()
                         }

                     }









    }

    //funcio que mostra les dades del llibre seleccionat
    private fun mostrarLlibre() {
        //rebre dades del llibre seleccionat
        //val args = ModificarLlibreArgs.fromBundle(requireArguments())
        //guardem els valors als editText
        binding.editTextTitolModificar.setText(args.titol)
        binding.editTextEditorialModificar.setText(args.editorial)
        Log.i("idmostrar", args.id)


        //inicialitzem spinner estat
        spinnerModificarEstat = binding.spnModificarEstat
        //carreguem els possibles estats a l'spinner
        context?.let {
            ArrayAdapter.createFromResource(it,R.array.estat, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    //guardem la posicio del valor
                    val positionSpnEstat = adapter.getPosition(args.estat)
                    spinnerModificarEstat.adapter = adapter
                    //assignem la posicio perque ens mostri el valor
                    spinnerModificarEstat.setSelection(positionSpnEstat)
                }
        }
        //inicialitzem spinner curs
        spinnerModificarCurs = binding.spnModificarCurs
        //carreguem els possibles estats a l'spinner
        context?.let {
            ArrayAdapter.createFromResource(it,R.array.cursos, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    //guardem la posicio del valor
                    val positionSpnCurs = adapter.getPosition(args.curs)
                    spinnerModificarCurs.adapter = adapter
                    //assignem la posicio perque ens mostri el valor
                    spinnerModificarCurs.setSelection(positionSpnCurs)
                }
        }
        //inicialitzem spinner assignatura
        spinnerModificarAssignatura = binding.spnModificarAssignatura
        //carreguem els possibles estats a l'spinner
        context?.let {
            ArrayAdapter.createFromResource(it,R.array.assignatures, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    //guardem la posicio del valor
                    val positionSpnAssignatura = adapter.getPosition(args.assignatura)
                    spinnerModificarAssignatura.adapter = adapter
                    //assignem la posicio perque ens mostri el valor
                    spinnerModificarAssignatura.setSelection(positionSpnAssignatura)
                }
        }



        //per carregar la imatge del llibre selecionat
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${args.foto}")

        imageRef.downloadUrl.addOnSuccessListener { url->
            binding.imageViewModificarLlibre.load(url)
        }.addOnFailureListener {  }

        Log.i("idmostrar", args.id)

    }


}