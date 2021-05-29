package cat.copernic.bookswap.llistatllibres

import android.annotation.SuppressLint
import android.app.ActionBar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentLlistatLlibresBinding
import cat.copernic.bookswap.utils.*
import cat.copernic.bookswap.viewmodel.ViewModel
import com.google.android.material.snackbar.Snackbar
import java.util.*

class LlistatLlibres : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentLlistatLlibresBinding
    private lateinit var adapter: Adapter
    private lateinit var rvLlibres: RecyclerView
    private val viewModel: ViewModel by viewModels()

    private lateinit var spinner: Spinner
    private lateinit var spinnercurs: Spinner
    private lateinit var spinnerassignatura: Spinner

    private var llibreEsborrarId = ""
    private var filtrat = false

    private var llibres: ArrayList<Llibre> = arrayListOf()
    private var llibresNoCaracters: ArrayList<Llibre> = arrayListOf()
    private var llibresNoCaractersFiltrar: ArrayList<Llibre> = arrayListOf()
    private val llibresFiltratsBenEscrits: ArrayList<Llibre> = arrayListOf()

    @SuppressLint("ShowToast")
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
        spinnercurs = binding.spnPoblacio

        //Inicialitza el spinner
        inicalitzarPoblacio()

        //Inicialitza el spinner
        inicailitzarAssignatura()

        //Inicialitza el spinner
        inicailitzarCurs()

        carregarLlibresRyclrViewModel()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun carregarLlibresRyclrViewModel() {

        viewModel.totsLlibresVM().observe(requireActivity(), { llibresRV ->

            viewModel.usuari().observe(requireActivity(), { usuari ->


                val llibresUsuariIterator = llibresRV.iterator()
                while (llibresUsuariIterator.hasNext()) {

                    if (llibresUsuariIterator.next().mail.contains(usuari.mail)) {
                        llibresUsuariIterator.remove()
                    }
                }

                //En el cas que de premer un llibre s'obra el fragment veure llibre
                adapter = Adapter(
                    llibresRV,
                    CellClickListener { titol, assignatura, editorial, curs, estat, foto, id, mail ->
                        findNavController().navigate(
                            LlistatLlibresDirections.actionLlistatLlibresToVeureLlibre(
                                titol,
                                assignatura,
                                curs,
                                editorial,
                                estat,
                                foto,
                                id,
                                mail,
                            )
                        )
                    })
                rvLlibres.adapter = adapter
                rvLlibres.layoutManager = LinearLayoutManager(this.context)

                llibresRV.forEach {
                    it.poblacio_login = usuari.poblacio
                }

                arrayLlibresNoCaracters(llibresRV)

                if (usuari.admin) {
                    swipePerEsborrar()
                }

                //Botó per filtrar
                binding.bttnBuscar.setOnClickListener {
                    filtrarPerCamp(usuari.admin, llibresRV)
                }

            })

        })

    }

    private fun swipePerEsborrar() {
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
            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {

                llibreEsborrarId =
                    adapter.mLlibres[viewHolder.bindingAdapterPosition].id

                esborrarLlibre()

                //snackbar que informa que s'ha esborrat l'article i permet desfer l'accio
                Snackbar.make(
                    binding.root,
                    R.string.esborrat,
                    Snackbar.LENGTH_LONG
                ).show()
            }

        }).attachToRecyclerView(binding.rcyViewLlibres)

    }

    // Igual que la consulta, només s'executa la primera vegada que s'obre el fragment
    private fun arrayLlibresNoCaracters(llibresRV: MutableList<Llibre>) {

        //Després de realitzar la consulta reasignem el llistat de llibres per treure els caràcters
        //no ASCII dels camps que volem filtrar
        val llibresIterator = llibresRV.iterator()
        while (llibresIterator.hasNext()) {
            val llib = llibresIterator.next()

            //S'executa la funció que modifica els caràcters especias a ASCII i retorna un tres
            //variables amb els tres caràcters que permetran filtrar.
            val poblacioNoAscii = noCaractersEspecials(llib.poblacio)
            val assignaturaNoAscii = noCaractersEspecials(llib.assignatura)
            val cursNoAscii = noCaractersEspecials(llib.curs)

            val titolNoAscii = noCaractersEspecials(llib.titol)

            val llibrLliure = Llibre(
                poblacio = poblacioNoAscii,
                assignatura = assignaturaNoAscii,
                curs = cursNoAscii,
                editorial = llib.editorial,
                titol = titolNoAscii,
                estat = llib.estat,
                foto = llib.foto,
                id = llib.id,
                mail = llib.mail,
                poblacio_login = llib.poblacio_login

            )
            //Afegeix el llibre al llistat sense caràcters ASCII
            llibresNoCaracters.add(llibrLliure)
        }
    }

    //Funció per canviar els caracters especials per ASCII
    private fun noCaractersEspecials(
        valor: String
    ): String {

        val accents = "àáèéíòóúïüç"
        val correct = "aaeeioouiuc"

        //Passa el sting a minuscula
        var valorNoAscii = valor.toLowerCase(Locale.ROOT)

        //Intercanvia els accens i les ç a caràcters ASCII
        for (i in accents.indices) {
            val posLletra = valorNoAscii.indexOf(accents[i])
            if (posLletra != -1) {
                valorNoAscii =
                    valorNoAscii.replace(
                        valorNoAscii[posLletra],
                        correct[i]
                    )
            }
        }

        //Remplaça el caràcter - per un esapi en blanc
        if (valorNoAscii.contains("-")) {
            valorNoAscii = valorNoAscii.replace("-", "")
        }

        //Retorna el string en ASCII
        return valorNoAscii
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun filtrarPerCamp(admin: Boolean, llibresRV: MutableList<Llibre>) {
        filtrat = true

        //Neteja la llista dels llibres filtrats
        llibresNoCaractersFiltrar.clear()
        llibresFiltratsBenEscrits.clear()

        //Afegeix el llistat tipus "Llibre" a un nou array per filtrar i no haver de repetir
        //la consulta cada vegada que l'usuari filtra
        llibresNoCaractersFiltrar.addAll(llibresNoCaracters)
        Log.d("prova", llibresNoCaractersFiltrar[0].poblacio_login)

        //Extreu les dades dels spinners
        val poblacioSeleccionada = spinner.selectedItem.toString()
        val cursSeleccionada = spinnercurs.selectedItem.toString()
        val assignaturaSeleccionada =
            spinnerassignatura.selectedItem.toString()

        //Els valors extrets dels spinners es canvien a caràcters no ASCII
        val poblaico = noCaractersEspecials(poblacioSeleccionada)
        val assignatura = noCaractersEspecials(assignaturaSeleccionada)
        val curs = noCaractersEspecials(cursSeleccionada)

        //Filtra les poblacións en el cas de seleccionar-ne una (el primer camp del spinner
        // és totes les poblacions)
        val noTotesPoblacions = llibresNoCaractersFiltrar.filter {
            it.poblacio != poblaico &&
                    spinner.selectedItemPosition != 0
        }
        //Del llitat de tots els llibres sense caràcters ASCII, s'esborren els camps extrets del filtre
        //per mostar aquest llistat al recycler view
        llibresNoCaractersFiltrar.removeAll(noTotesPoblacions)

        //Mateix filtre pels cursos
        val noTotsCursos = llibresNoCaractersFiltrar.filter {
            it.curs != curs &&
                    spinnercurs.selectedItemPosition != 0
        }
        //Mateix mètode d'esborrar pels cursos
        llibresNoCaractersFiltrar.removeAll(noTotsCursos)

        //Mateix filtre per les assignatures
        val noTotesAssignaures = llibresNoCaractersFiltrar.filter {
            it.assignatura != assignatura &&
                    spinnerassignatura.selectedItemPosition != 0
        }
        //Mateix mètode d'esborrar per les assignatures
        llibresNoCaractersFiltrar.removeAll(noTotesAssignaures)

        //Filtra segons el titol (Contingut, no pararula completa)
        val titolBuscat = buscarTitol()


        //Compara el llistat de llibres filtrats amb els del llistat amb la ortografia correcta
        //per mostar aquests al recycler view
        llibresNoCaractersFiltrar.forEach { filtrar ->
            llibresRV.forEach { llibreEscrit ->
                if (filtrar.id == llibreEscrit.id) {
                    llibresFiltratsBenEscrits.add(llibreEscrit)
                }
            }
        }

        if (admin) {
            swipePerEsborrar()
        }

        //Carregem el recyclerView amb els llibres amb la ortografia correcte
        if (titolBuscat && poblacioSeleccionada == "Totes les Poblacions"
            && cursSeleccionada == "Tote els Cursos"
            && assignaturaSeleccionada == "Totes les Assignatures"
        ) {
            carregarLlibresRyclrViewModel()
        } else {
            carregarLlibresRyclrView(llibresFiltratsBenEscrits)
        }

        if (llibresFiltratsBenEscrits.isEmpty()) {
            view?.let {
                Snackbar.make(it, R.string.filtrarLlibresBuit, Snackbar.LENGTH_LONG).apply {
                    val layoutParams = ActionBar.LayoutParams(this.view.layoutParams)
                    layoutParams.gravity = Gravity.BOTTOM
                }.show()
            }
        }
    }

    private fun buscarTitol(): Boolean {

        //Extreu el editText titol
        val titolEditText = binding.edTxtTitol.text.toString()

        //Comprova que el titol estigui emplenat
        if (titolEditText != "") {
            //Treure caracters especials del edit text
            val titolEdTxtNoCaracters = noCaractersEspecials(titolEditText)
            //Inicialitza el iterator
            val llibresIterator = llibresNoCaractersFiltrar.iterator()
            while (llibresIterator.hasNext()) {
                //En el cas que un llibre NO sigui contingui el valor del editText titol s'esborra
                //del llistat de llibres per mostar aquest llistat al recycler view
                val llib = llibresIterator.next().titol
                if (!llib.contains(titolEdTxtNoCaracters)) {
                    llibresIterator.remove()
                }
            }
            return true
        } else {
            return false
        }
    }


    private fun carregarLlibresRyclrView(llib: ArrayList<Llibre>) {
        Log.d("prova", llib[0].poblacio_login)

        //En el cas que de premer un llibre s'obra el fragment veure llibre
        adapter = Adapter(
            llib,
            CellClickListener
            { titol, assignatura, editorial, curs, estat, foto, id, mail ->
                findNavController().navigate(
                    LlistatLlibresDirections.actionLlistatLlibresToVeureLlibre(
                        titol,
                        assignatura,
                        curs,
                        editorial,
                        estat,
                        foto,
                        id,
                        mail,
                    )
                )
            })
        rvLlibres.adapter = adapter
        rvLlibres.layoutManager = LinearLayoutManager(this.context)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun esborrarLlibre() {
        viewModel.esborrarLlibre(llibreEsborrarId).observe(requireActivity(), {

            if (it) {
                val llibresIterator = llibres.iterator()
                while (llibresIterator.hasNext()) {
                    if (llibresIterator.next().id == llibreEsborrarId) {
                        llibresIterator.remove()
                        Log.d("prova", llibreEsborrarId)
                    }
                }
                val llibresFiltrarIterator = llibresFiltratsBenEscrits.iterator()
                while (llibresFiltrarIterator.hasNext()) {
                    if (llibresFiltrarIterator.next().id == llibreEsborrarId) {
                        llibresFiltrarIterator.remove()
                    }
                }
                Log.d("prova", filtrat.toString())

            }
            if (filtrat) {
                carregarLlibresRyclrView(llibresFiltratsBenEscrits)
                filtrat = false
            } else {
                carregarLlibresRyclrViewModel()

            }
        })
    }

    private fun inicalitzarPoblacio() {

        //Consulta per extreure el les dades del usuari segons el mail
        viewModel.usuari().observe(requireActivity(), { usuari ->

            //Inicialitzar spinner
            spinner = binding.spnPoblacio

            //Crear l'adapter per el spinner
            context?.let {
                ArrayAdapter.createFromResource(
                    it,
                    R.array.poblacionsFiltre,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

                    //Obtenir la poblacio preestablerta per l'usuari
                    val posicioSpinner = adapter.getPosition(usuari.poblacio)


                    //Assignar l'adapter al spinner
                    spinner.adapter = adapter

                    //Assignar la posició de la població preestagblerta al spinner
                    spinner.setSelection(posicioSpinner)
                }
            }

            //Permet seleccionar un camp del spinner
            spinner.onItemSelectedListener = this


        })
    }

    private fun inicailitzarCurs() {

        //Inicialitzar spinner
        spinnercurs = binding.spnCurs

        //Crear l'adapter per el spinner
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.cursos,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)


                //Assignar l'adapter al spinner
                spinnercurs.adapter = adapter
            }
        }

        //Permet seleccionar un camp del spinner
        spinnercurs.onItemSelectedListener = this


    }

    private fun inicailitzarAssignatura() {

        //Inicialitzar spinner
        spinnerassignatura = binding.spnAssignatura

        //Crear l'adapter per el spinner
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.assignatures,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)


                //Assignar l'adapter al spinner
                spinnerassignatura.adapter = adapter
            }
        }

        //Permet seleccionar un camp del spinner
        spinnerassignatura.onItemSelectedListener = this


    }


    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}
