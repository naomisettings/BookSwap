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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentMeusLlibresBinding
import cat.copernic.bookswap.utils.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class MeusLlibres : Fragment() {
    private lateinit var binding: FragmentMeusLlibresBinding
    //recyclerview
    private lateinit var rvLlibres: RecyclerView

    //adapter
    private lateinit var adapterMeus: Adapter

    //instancia a firebase
    val db = FirebaseFirestore.getInstance()
    var llibres = arrayListOf<Llibre>()
    var llibreEsborrarId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_meus_llibres, container, false)
        binding.lifecycleOwner = this

        //inicialitzem el recyclerview
        rvLlibres = binding.meusLlibresList
        //cridem a la funcio per carregar els libres al recyclerview
        veureLlibres(rvLlibres)

        //acció floatinButton cap a la pantalla afegirLlibre
        binding.floatingActionButton2.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_meusLlibres_to_afegirLlibre)

        }
        //configurem els desplaçament lateral d'un item seleccionat
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                return false

            }

            //funcio que fa que si es desplaça l'item a l'esquerra o dreta es pot esborrar
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                llibreEsborrarId = adapterMeus.mLlibres[viewHolder.bindingAdapterPosition].id
                Log.i("position", adapterMeus.mLlibres[viewHolder.bindingAdapterPosition].id)
                esborrarLlibre(llibreEsborrarId)

                //snackbar que informa que s'ha esborrat l'article i permet desfer l'accio
                Snackbar.make(binding.root, "Esborrat!", Snackbar.LENGTH_LONG).show()
            }

        }).attachToRecyclerView(binding.meusLlibresList)





        return binding.root
    }

    private fun esborrarLlibre(llibreEsborrarId: String) {

        //guardem les dades del usuari identificat
        val user = Firebase.auth.currentUser
        //agafem el mail com a identificador unic de l'usuari
        val mail = user?.email.toString()
        //Consulta per extreure el les dades dels llibres del usuari segons el mail
        db.collection("llibres").whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                val llibreConsulta = document.toObjects(Llibres::class.java)
                document?.forEachIndexed { index, element ->
                    //Extreu la id del document
                    val llibresId = element.id
                    Log.i("llibreEsborrarId", llibreEsborrarId)
                    //for (x in 0 until llibreConsulta.size) {
                    if (llibreConsulta[index].id == llibreEsborrarId) {
                        Log.i("idLlibreConsulta", llibreConsulta[index].id)
                        val sfDocRefLlibre = db.collection("llibres").document(llibresId)
                        Log.i("sfDocRefLlibre", sfDocRefLlibre.toString())
                        //Esborra els llibres publicats per l'usuari
                        db.runTransaction { transaction ->
                            //esborrem el llibre del Firestore
                            transaction.delete(sfDocRefLlibre)
                        }.addOnSuccessListener {
                            Log.d("TAG", "Transaction success!")
                            view?.let {
                                Snackbar.make(
                                    it,
                                    "Llibre esborrat",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                            veureLlibres(rvLlibres)

                        }.addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                        }
                    }
                }

            }

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
                if (document != null) {
                    val llibresDC = document.toObjects(Llibres::class.java)
                    for (i in 0 until llibresDC.size) {

                        val llibresConsulta = Llibre(
                            titol = llibresDC[i].titol,
                            assignatura = llibresDC[i].assignatura,
                            editorial = llibresDC[i].editorial,
                            curs = llibresDC[i].curs,
                            estat = llibresDC[i].estat,
                            foto = llibresDC[i].foto,
                            id = llibresDC[i].id,
                            poblacio = llibresDC[i].poblacio

                        )

                        llibres.add(llibresConsulta)
                    }

                    db.collection("usuaris").whereEqualTo("mail", user?.email).get()
                        .addOnSuccessListener { doc ->

                            val usuariDC = doc.toObjects(UsuariDC::class.java)

                            llibres.forEach {
                                it.poblacio_login = usuariDC[0].poblacio
                            }

                            adapterMeus = Adapter(llibres,
                                CellClickListener { titol, assignatura, editorial, curs, estat, foto, id, mail ->
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
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)


            }


    }
}






