package cat.copernic.bookswap.llistatllibres

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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

    //Spinners
    private lateinit var spinner: Spinner
    private lateinit var spinnercurs: Spinner
    private lateinit var spinnerassignatura: Spinner

    //id del llibre que es vol esborrar (s'extreu del adapter)
    private var llibreEsborrarId = ""

    //Boolean per saber si s'ha relitzat un filtre
    private var filtrat = false

    //Llistats per relitzar els filtres
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

        //Inicialitzar recicler view
        rvLlibres = binding.rcyViewLlibres

        //Inicialitza el spinner població
        inicalitzarPoblacio()

        //Inicialitza el spinner assignatura
        inicailitzarAssignatura()

        //Inicialitza el spinner curs
        inicailitzarCurs()

        //Carrega el recycler view del llistat llibres en view model
        carregarLlibresRyclrViewModel()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun carregarLlibresRyclrViewModel() {

        //Truca a la consulta del view model per vereure tots els llibres
        viewModel.totsLlibresVM().observe(requireActivity(), { llibresRV ->

            //Truca a la consulta del view model per extreure les dades del usuari loginat
            viewModel.usuariLoginat().observe(requireActivity(), { usuari ->

                //Esborra els llibres amb el mateix mail que el del usuari loginat
                val llibresIter = llibresRV.iterator()
                while (llibresIter.hasNext()) {
                    if (llibresIter.next().mail == usuari.mail) {
                        llibresIter.remove()
                    }
                }

                //Adapter del llistat dels llibres
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

                //Transorma el llistat dels llibres a minúsculas i sense accents per fer
                //el filtratge per titol
                arrayLlibresNoCaracters(llibresRV)

                //En cas que el usuari sigui admin podrà esborrar qualsevol llibre
                if (usuari.admin) {
                    swipePerEsborrar()
                }
                //Botó per filtrar els llibres
                binding.bttnBuscar.setOnClickListener {
                    filtrarPerCamp(usuari.admin, llibresRV)

                    //Override que amaga el teclat
                    hideKeyboard()
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
                //Extreu la id del llibre carregat al recycler view per esborrar-lo de la base de dades
                llibreEsborrarId =
                    adapter.mLlibres[viewHolder.bindingAdapterPosition].id

                //Funció que esborra el llibre
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

    // Només s'executa la primera vegada que s'obre el fragment o el view model
    private fun arrayLlibresNoCaracters(llibresRV: MutableList<Llibre>) {

        //Després de realitzar la consulta per extreure els llibres,
        // reasignem el llistat de llibres per extreure els caràcters
        //no ASCII dels camps que volem filtrar i canviar-los
        val llibresIterator = llibresRV.iterator()
        while (llibresIterator.hasNext()) {
            val llib = llibresIterator.next()

            //S'executa la funció que modifica els caràcters especias a ASCII i retorna un quatre
            //variables en forma de string
            val poblacioNoAscii = noCaractersEspecials(llib.poblacio)
            val assignaturaNoAscii = noCaractersEspecials(llib.assignatura)
            val cursNoAscii = noCaractersEspecials(llib.curs)
            val titolNoAscii = noCaractersEspecials(llib.titol)

            //Instanciem Llibre per modifcar les dades sense caràctres ASCII
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
    fun noCaractersEspecials(
        valor: String
    ): String {

        val accents = "àáèéíòóúïüç"
        val correct = "aaeeioouiuc"

        //Passa el sting a minuscula
        var valorNoAscii = valor.toLowerCase(Locale.ROOT)

        //Intercanvia els accens, les díeresi i les ç a caràcters ASCII
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
        //Serveix per saber si hem utilitzat el filtre
        filtrat = true

        //Neteja la llista dels llibres filtrats
        llibresNoCaractersFiltrar.clear()
        llibresFiltratsBenEscrits.clear()

        //Afegeix el llistat tipus "Llibre" a un nou array per filtrar i no haver de repetir
        //la consulta cada vegada que l'usuari filtra
        llibresNoCaractersFiltrar.addAll(llibresNoCaracters)

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
        //Els dos filtres posteriors funcionen igual
        llibresNoCaractersFiltrar.removeAll(noTotesPoblacions)

        val noTotsCursos = llibresNoCaractersFiltrar.filter {
            it.curs != curs &&
                    spinnercurs.selectedItemPosition != 0
        }
        llibresNoCaractersFiltrar.removeAll(noTotsCursos)

        val noTotesAssignaures = llibresNoCaractersFiltrar.filter {
            it.assignatura != assignatura &&
                    spinnerassignatura.selectedItemPosition != 0
        }
        llibresNoCaractersFiltrar.removeAll(noTotesAssignaures)

        //Filtra segons el titol (Contingut, no pararula completa)
        //i no és sensible als caràcters no ASCII
        val titolBuscat = buscarTitol()


        //Compara el llistat de llibres filtrats amb els del llistat original
        //per mostar aquests al recycler view. Creant dos llistats finals amb llibres ben escrits,
        //el llistat original i el llistat amb llibres filtrats
        llibresNoCaractersFiltrar.forEach { filtrar ->
            llibresRV.forEach { llibreEscrit ->
                if (filtrar.id == llibreEscrit.id) {
                    llibresFiltratsBenEscrits.add(llibreEscrit)
                }
            }
        }

        //En el cas que l'usuari sigui admin podrà esborrar llibres del llistat públic
        if (admin) {
            swipePerEsborrar()
        }

        //Carregem el recyclerView amb els llibres amb la ortografia correcte
        //En el cas que s'hagi esborrat tots els filtres i premi el botó filtrar
        //Per veure'ls tots es carrega autoàticament el recicyler view fent la consulta
        //del view model
        if (titolBuscat && poblacioSeleccionada == "Totes les Poblacions"
            && cursSeleccionada == "Tote els Cursos"
            && assignaturaSeleccionada == "Totes les Assignatures"
        ) {
            carregarLlibresRyclrViewModel()
        } else {
            //En cas contrari carrega els llibres filtrats
            carregarLlibresRyclrView(llibresFiltratsBenEscrits)
        }

        //En el cas que no hi hagin llibres i s'hagi filtrat es mostra un snackbar
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
            //Extreu caracters especials del edit text amb el titol introduit per l'usuari
            val titolEdTxtNoCaracters = noCaractersEspecials(titolEditText)
            //Inicialitza el iterator amb el array dels llibres filtrats
            val llibresIterator = llibresNoCaractersFiltrar.iterator()
            while (llibresIterator.hasNext()) {
                //En el cas que un llibre NO contingui el valor del editText del titol, s'esborra
                //del llistat de llibres per mostar els que sí coincideixen al recycler view
                val llib = llibresIterator.next().titol
                if (!llib.contains(titolEdTxtNoCaracters)) {
                    llibresIterator.remove()
                }
            }
            //Retorna true o false perque la funcó buscar() sàpiga si s'ha relitzat un filtre
            //o no
            return true
        } else {
            return false
        }
    }

    //Recycler view per carregar els llibres filtrats
    private fun carregarLlibresRyclrView(llib: ArrayList<Llibre>) {

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

    //Funció que es truca des del swipe per esborrar els llibres deslliçant-los cap un costat
    @RequiresApi(Build.VERSION_CODES.N)
    private fun esborrarLlibre() {
        //Truca a la consutlta del view modle i li passem el paràmetre extret del adapter a la
        //funció onSwipe perque esborri el llibre que toca i retorna true o false si s'ha esborrat
        //el llibre
        viewModel.esborrarLlibre(llibreEsborrarId).observe(requireActivity(), {

            if (it) {
                //Esborra el llibre també del llistat de llibres filtrats per si
                //l'usuari estava utilitzatn un filtre
                val llibresFiltrarIterator = llibresFiltratsBenEscrits.iterator()
                while (llibresFiltrarIterator.hasNext()) {
                    if (llibresFiltrarIterator.next().id == llibreEsborrarId) {
                        llibresFiltrarIterator.remove()
                    }
                }
            }
            //En el cas que l'usuari hagi utilitzat un filtre s'executarà el recycler view
            //de llibres filtrats o el del view model amb tots els llibres
            if (filtrat) {
                carregarLlibresRyclrView(llibresFiltratsBenEscrits)
                filtrat = false
            } else {
                carregarLlibresRyclrViewModel()
            }
        })
    }

    //Inicialitza la població al spinner
    private fun inicalitzarPoblacio() {

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

                //Assignar l'adapter al spinner
                spinner.adapter = adapter
            }
        }
        //Permet seleccionar un camp del spinner
        spinner.onItemSelectedListener = this
    }

    //Inicialitza el curs al spinner
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

    //Inicialitza l'assignatura al spinner
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

    //Serveix perque el cardview del recycler view es pugui aptretar
    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    //Amagar teclat
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    //Amagar teclat
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }
}
