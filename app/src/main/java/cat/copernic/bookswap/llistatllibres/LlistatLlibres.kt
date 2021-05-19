package cat.copernic.bookswap.llistatllibres

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.SpinnerBindingAdapter
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.R
import cat.copernic.bookswap.utils.Adapter
import cat.copernic.bookswap.databinding.FragmentLlistatLlibresBinding
import cat.copernic.bookswap.utils.CellClickListener
import cat.copernic.bookswap.utils.Llibre
import cat.copernic.bookswap.utils.Llibres
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LlistatLlibres : Fragment() {

    private lateinit var binding: FragmentLlistatLlibresBinding

    private val db = FirebaseFirestore.getInstance()
    private var llibres: ArrayList<Llibre> = arrayListOf()

    private lateinit var spinner: Spinner

    //recylerView
    private lateinit var adapter: Adapter

    //guardem les dades del usuari identificat
    private val user = Firebase.auth.currentUser

    private lateinit var rvLlibres: RecyclerView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_llistat_llibres, container, false)


        rvLlibres = binding.rcyViewLlibres

        spinner = binding.spnPoblacio

        veureLlibres()



        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun veureLlibres() {

        //agafem el mail com a identificador unic de l'usuari
        val mail = user?.email.toString()

        llibres.clear()

        db.collection("llibres").get()
            .addOnSuccessListener { document ->
                if (document != null) {

                    val llibresDC = document.toObjects(Llibres::class.java)

                    for (i in 0 until llibresDC.size) {
                        val llib = Llibre(
                            assignatura = llibresDC[i].assignatura,
                            curs = llibresDC[i].curs,
                            editorial = llibresDC[i].editorial,
                            titol = llibresDC[i].titol,
                            estat = llibresDC[i].estat,
                            foto = llibresDC[i].foto
                        )
                        llibres.add(llib)
                    }

                    carregarLlibresRyclrView()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

    }

    private fun carregarLlibresRyclrView() {

        adapter = Adapter(llibres,
            CellClickListener { titol, assignatura, editorial, curs, estat, foto ->
                findNavController().navigate(
                    LlistatLlibresDirections.actionLlistatLlibresToVeureLlibre(
                        titol,
                        assignatura,
                        curs,
                        editorial,
                        estat,
                    )
                )
            })
        rvLlibres.adapter = adapter
        rvLlibres.layoutManager = LinearLayoutManager(this.context)

    }

}
