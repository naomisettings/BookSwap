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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentLlistatLlibresBinding
import cat.copernic.bookswap.utils.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class LlistatLlibres : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentLlistatLlibresBinding

    private val db = FirebaseFirestore.getInstance()
    private var llibres: ArrayList<Llibre> = arrayListOf()
    private var llibresNoCaracters: ArrayList<Llibre> = arrayListOf()
    private var llibresNoCaractersFiltrar: ArrayList<Llibre> = arrayListOf()

    //recylerView
    private lateinit var adapter: Adapter

    //guardem les dades del usuari identificat
    private val user = Firebase.auth.currentUser

    private lateinit var rvLlibres: RecyclerView

    private lateinit var spinner: Spinner
    private lateinit var spinnercurs: Spinner
    private lateinit var spinnerassignatura: Spinner

    private var entraVeureLlibres = true

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

        //Mostra els llibres
        if (entraVeureLlibres) {
            veureLlibres()
        }

        //Botó per filtrar
        binding.bttnBuscar.setOnClickListener {
            filtrarPerCamp()
        }

        return binding.root
    }

    //ATENCIÓ
    //Només entra la primera vegada que s'executa el fragment
    @RequiresApi(Build.VERSION_CODES.N)
    private fun veureLlibres() {

        //netejar l'array de llibres per si l'usuari entra varies vegades al fragment llitat llibres
        llibres.clear()

        //Consulta de la colecció llibres
        db.collection("llibres").get()
            .addOnSuccessListener { document ->
                if (document != null) {

                    val llibresDC = document.toObjects(Llibres::class.java)

                    //Recorecut de tots els llibres de la colecció per afegir-los al array
                    for (x in 0 until llibresDC.size) {

                        //Gurada els valors en una constant tipo llibre
                        val llib = Llibre(
                            assignatura = llibresDC[x].assignatura,
                            curs = llibresDC[x].curs,
                            editorial = llibresDC[x].editorial,
                            titol = llibresDC[x].titol,
                            estat = llibresDC[x].estat,
                            foto = llibresDC[x].foto,
                            poblacio = llibresDC[x].poblacio,
                            id = llibresDC[x].id
                        )
                        //Afegeix el llibre al llistat llibres por mostrar-los al recycler view
                        llibres.add(llib)

                    }

                    entraVeureLlibres = false

                    arrayLlibresNoCaracters()

                    //Carregem el recyclerView
                    carregarLlibresRyclrView(llibres)

                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    // Igual que la consulta, només s'executa la primera vegada que s'obre el fragment
    private fun arrayLlibresNoCaracters() {

        //Després de realitzar la consulta reasignem el llistat de llibres per treure els caràcters
        //no ASCII dels camps que volem filtrar
        val llibresIterator = llibres.iterator()
        while (llibresIterator.hasNext()) {
            val llib = llibresIterator.next()

            //S'executa la funció que modifica els caràcters especias a ASCII i retorna un tres
            //variables amb els tres caràcters que permetran filtrar.
                val poblacioNoAscii = noCaractersEspecials(llib.poblacio)
                val assignaturaNoAscii = noCaractersEspecials(llib.assignatura)
                val cursNoAscii = noCaractersEspecials(llib.curs)

            val llibrLliure = Llibre(
                poblacio = poblacioNoAscii,
                assignatura = assignaturaNoAscii,
                curs = cursNoAscii,
                editorial = llib.editorial,
                titol = llib.titol,
                estat = llib.estat,
                foto = llib.foto,
                id = llib.id

            )
            //Afegeix el llibre al llistat sense caràcters ASCII
            llibresNoCaracters.add(llibrLliure)
        }
    }

    //Funció per canviar els caracters especials per ASCII
    private fun noCaractersEspecials(
        valor: String
    ): String {

        val accents = "àèéíòóúïüç"
        val correct = "aeeioouiuc"

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


    private fun filtrarPerCamp() {

        //Neteja la llista dels llibres filtrats
        llibresNoCaractersFiltrar.clear()

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
        buscarTitol()

        val llibresFiltratsBenEscrits: ArrayList<Llibre> = arrayListOf()

        //Compara el llistat de llibres filtrats amb els del llistat amb la ortografia correcta
        //per mostar aquests al recycler view
        llibresNoCaractersFiltrar.forEach { filtrar ->
            llibres.forEach { llibreEscrit ->
                if (filtrar.id == llibreEscrit.id) {
                    llibresFiltratsBenEscrits.add(llibreEscrit)
                }
            }
        }

        //Carregem el recyclerView amb els llibres amb la ortografia correcte
        carregarLlibresRyclrView(llibresFiltratsBenEscrits)
    }

    private fun buscarTitol() {

        //Extreu el editText titol
        val titolEditText = binding.edTxtTitol.text.toString()

        //Comprova que el titol estigui emplenat
        if (titolEditText != "") {
            //Inicialitza el iterator
            val llibresIterator = llibresNoCaractersFiltrar.iterator()
            while (llibresIterator.hasNext()) {
                //En el cas que un llibre NO sigui contingui el valor del editText titol s'esborra
                //del llistat de llibres per mostar aquest llistat al recycler view
                if (!llibresIterator.next().titol.contains(titolEditText))
                    llibresIterator.remove()
            }
        }
    }


    private fun carregarLlibresRyclrView(llib: ArrayList<Llibre>) {

        //En el cas que de premer un llibre s'obra el fragment veure llibre
        adapter = Adapter(
            llib,
            CellClickListener { titol, assignatura, editorial, curs, estat, foto, id ->
                findNavController().navigate(
                    LlistatLlibresDirections.actionLlistatLlibresToVeureLlibre(
                        titol,
                        assignatura,
                        curs,
                        editorial,
                        estat,
                        foto,
                        id,
                    )
                )
            })
        rvLlibres.adapter = adapter
        rvLlibres.layoutManager = LinearLayoutManager(this.context)

    }

    private fun inicalitzarPoblacio() {
        //Guarda el mail del usuari que ha fet login
        val mail = user?.email.toString()

        //Consulta per extreure el les dades del usuari segons el mail
        db.collection("usuaris").whereEqualTo("mail", mail).get()
            .addOnSuccessListener { document ->
                document?.forEach { _ ->
                    val usuari = document.toObjects(UsuariDC::class.java)

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


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}
