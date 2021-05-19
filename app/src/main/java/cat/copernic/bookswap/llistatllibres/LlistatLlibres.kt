package cat.copernic.bookswap.llistatllibres

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.SpinnerBindingAdapter
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentLlistatLlibresBinding
import cat.copernic.bookswap.utils.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LlistatLlibres : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentLlistatLlibresBinding

    private val db = FirebaseFirestore.getInstance()
    private var llibres: ArrayList<Llibre> = arrayListOf()

    //recylerView
    private lateinit var adapter: Adapter

    //guardem les dades del usuari identificat
    private val user = Firebase.auth.currentUser

    private lateinit var rvLlibres: RecyclerView

    private lateinit var spinner: Spinner
    private var poblacio = ""

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

        inicalitzarDadesPersonalitzades()

        veureLlibres()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun veureLlibres() {

        //agafem el mail com a identificador unic de l'usuari
        val mail = user?.email.toString()

        //netejar l'array de llibres per si l'usuari entra varies vegades al fragment llitat llibres
        llibres.clear()

        //Consulta de la colecció llibres
        db.collection("llibres").get()
            .addOnSuccessListener { document ->
                if (document != null) {

                    val llibresDC = document.toObjects(Llibres::class.java)

                    //Recorecut de tots els llibres de la colecció per afegir-los al array
                    for (i in 0 until llibresDC.size) {
                        val llib = Llibre(
                            assignatura = llibresDC[i].assignatura,
                            curs = llibresDC[i].curs,
                            editorial = llibresDC[i].editorial,
                            titol = llibresDC[i].titol,
                            estat = llibresDC[i].estat,
                            foto = llibresDC[i].foto
                        )
                        //Afegeix els llibres al array
                        llibres.add(llib)
                    }

                    //Carregem el recyclerView
                    carregarLlibresRyclrView()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

    }

    private fun carregarLlibresRyclrView() {

        //En el cas que de premer un llibre s'obra el fragment veure llibre
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

    private fun inicalitzarDadesPersonalitzades() {
        //Guarda el mail del usuari que ha fet login
        val mail = user?.email.toString()

        //Consulta per extreure el les dades del usuari segons el mail
        db.collection("usuaris").whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                document?.forEach {
                    val usuari = document.toObjects(UsuariDC::class.java)

                    //Inicialitzar spinner
                    spinner = binding.spnPoblacio

                    //Crear l'adapter per el spinner
                    context?.let {
                        ArrayAdapter.createFromResource(
                            it,
                            R.array.poblacions,
                            android.R.layout.simple_spinner_item
                        ).also { adapter ->
                            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

                            //Obtenir la poblacio preestablerta per l'usuari
                            val posicioSpinner = adapter.getPosition(usuari[0].poblacio)

                            //Assignar l'adapter al spinner
                            spinner.adapter = adapter

                            //Assignar la posició de la població preestagblerta al spinner
                            spinner.setSelection(posicioSpinner)
                        }
                    }

                    //Permet seleccionar un camp del spinner
                    spinner.onItemSelectedListener = this


                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}
