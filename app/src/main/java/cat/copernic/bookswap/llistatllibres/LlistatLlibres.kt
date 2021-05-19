package cat.copernic.bookswap.llistatllibres

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
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
    private var llibres = arrayListOf<Llibre>()

    //recylerView
    private lateinit var adapter: Adapter

    //guardem les dades del usuari identificat
    private val user = Firebase.auth.currentUser

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_llistat_llibres, container, false)


        val rvLlibres = binding.rcyViewLlibres

        veureLlibres(rvLlibres)



        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun veureLlibres(rvLlibres: RecyclerView) {

        //agafem el mail com a identificador unic de l'usuari
        val mail = user?.email.toString()

        llibres.clear()

        db.collection("llibres").whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                if (document != null) {

                    val llibresDC = document.toObjects(Llibres::class.java)

                    Log.d("sizellibres", llibresDC.size.toString())

                    for (i in 0 until llibresDC.size) {
                        /*
                        val titol = llibresDC[i].llibre["titol"]
                        val assignatura = llibresDC[i].llibre["assignatura"]
                        val curs = llibresDC[i].llibre["curs"]
                        val editorial = llibresDC[i].llibre["editorial"]
                        val estat = llibresDC[i].llibre["estat"]

                        val llib = Llibre(
                            titol.toString(),
                            assignatura.toString(),
                            curs.toString(),
                            editorial.toString(),
                            estat.toString()
                        )

                        llibres.add(llib)

                         */
                    }
                }
                adapter = Adapter(llibres,
                    CellClickListener { titol, assignatura, editorial, curs, estat ->
                        findNavController().navigate(
                            LlistatLlibresDirections.actionLlistatLlibresToVeureLlibre(
                                titol,
                                assignatura,
                                editorial,
                                curs,
                                estat
                            )
                        )
                    })
                rvLlibres.adapter = adapter
                rvLlibres.layoutManager = LinearLayoutManager(this.context)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }
}
