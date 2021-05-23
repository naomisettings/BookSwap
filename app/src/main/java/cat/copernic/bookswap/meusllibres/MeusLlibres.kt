package cat.copernic.bookswap.meusllibres


import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentMeusLlibresBinding
import cat.copernic.bookswap.utils.Adapter
import cat.copernic.bookswap.utils.CellClickListener
import cat.copernic.bookswap.utils.Llibre
import cat.copernic.bookswap.utils.Llibres
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class MeusLlibres : Fragment() {
    private lateinit var binding: FragmentMeusLlibresBinding
    //instancia a firebase
    val db = FirebaseFirestore.getInstance()
    var llibres = arrayListOf<Llibre>()
    //adapter
    private lateinit var adapterMeus: Adapter

    //recylerView
//    var rvLlibres = binding.meusLlibresList

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_meus_llibres, container, false)
        binding.lifecycleOwner = this

        //inicialitzem el recyclerview
        val rvLlibres = binding.meusLlibresList
        //cridem a la funcio per carregar els libres al recyclerview
        veureLlibres(rvLlibres)

        //acció floatinButton cap a la pantalla afegirLlibre
        binding.floatingActionButton2.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_meusLlibres_to_afegirLlibre)

        }




        return binding.root
    }

    private fun veureLlibres(rvLlibres: RecyclerView) {
        //guardem les dades del usuari identificat
        val user = Firebase.auth.currentUser
        //agafem el mail com a identificador unic de l'usuari
        val mail = user?.email.toString()
        //buidem l/array de llibres perque no es vagin duplicant
        llibres.clear()
        //busquem a la col.leccio llibres els que tenen el mail de l'usuari identificat
        db.collection("llibres").whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                if(document != null){
                    val llibresDC = document.toObjects(Llibres::class.java)
                    for (i in 0 until llibresDC.size) {

                        val llibresConsulta = Llibre(
                            titol = llibresDC[i].titol,
                            assignatura = llibresDC[i].assignatura,
                            editorial = llibresDC[i].editorial,
                            curs = llibresDC[i].curs,
                            estat = llibresDC[i].estat,
                            foto = llibresDC[i].foto

                        )
                        llibres.add(llibresConsulta)


                    }

                }
                adapterMeus = Adapter(llibres,
                    CellClickListener {titol, assignatura, editorial, curs, estat, foto, id ->
                        findNavController().navigate(
                            MeusLlibresDirections.actionMeusLlibresToModificarLlibre(
                                titol,
                                assignatura,
                                editorial,
                                curs,
                                estat,
                                foto,
                                id,
                            )
                        )
                    })
                rvLlibres.adapter = adapterMeus
                rvLlibres.layoutManager = LinearLayoutManager(this.context)


        }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }



    }
}





