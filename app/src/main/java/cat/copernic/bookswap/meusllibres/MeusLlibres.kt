package cat.copernic.bookswap.meusllibres


import android.app.Activity
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentMeusLlibresBinding
import cat.copernic.bookswap.utils.*
import cat.copernic.bookswap.viewmodel.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import android.content.Context
import android.view.inputmethod.InputMethodManager


class MeusLlibres : Fragment() {
    private lateinit var binding: FragmentMeusLlibresBinding

    //recyclerview
    private lateinit var rvLlibres: RecyclerView

    //adapter
    private lateinit var adapterMeus: Adapter
    private val viewModel: ViewModel by viewModels()

    //instancia a firebase
    val db = FirebaseFirestore.getInstance()
    var llibres = arrayListOf<Llibre>()
    var llibreEsborrarId = ""

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_meus_llibres, container, false)


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
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                llibreEsborrarId = adapterMeus.mLlibres[viewHolder.bindingAdapterPosition].id
                Log.i("position", adapterMeus.mLlibres[viewHolder.bindingAdapterPosition].id)
                esborrarLlibre(llibreEsborrarId)

            }

        }).attachToRecyclerView(binding.meusLlibresList)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
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

                    if (llibreConsulta[index].id == llibreEsborrarId) {
                        Log.i("idLlibreConsulta", llibreConsulta[index].id)
                        val sfDocRefLlibre = db.collection("llibres").document(llibresId)

                        //Esborra els llibres publicats per l'usuari
                        db.runTransaction { transaction ->
                            //esborrem el llibre del Firestore
                            transaction.delete(sfDocRefLlibre)
                        }.addOnSuccessListener {
                            Log.d("TAG", "Transaction success!")
                            view?.let {
                                Snackbar.make(
                                    it,
                                    R.string.llibreEsborrat,
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun veureLlibres(rvLlibres: RecyclerView) {
        viewModel.meusLlibresVM().observe(requireActivity(), { llibresRV ->

            viewModel.usuariLoginat().observe(requireActivity(), { usuari ->

                    llibresRV.forEach {
                        it.poblacio_login = usuari.poblacio
                    }

                    adapterMeus = Adapter(llibresRV,
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
                                    mail,
                                )
                            )
                        })
                    rvLlibres.adapter = adapterMeus
                    rvLlibres.layoutManager = LinearLayoutManager(this.context)


                })

        })

    }
    //Amagar teclat
    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    //Amagar teclat
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

}












