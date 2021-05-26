package cat.copernic.bookswap.afegirllibre

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentAfegirLlibreBinding
import cat.copernic.bookswap.utils.Llibre
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class AfegirLlibre : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentAfegirLlibreBinding
    private lateinit var imgfoto: ImageView
    private lateinit var spinnerEstat: Spinner
    private lateinit var spinnerCurs:Spinner
    private lateinit var spinnerAssignatura: Spinner

    //instancia a firebase
    private val db = FirebaseFirestore.getInstance()

    //inicialitzem les variables dels camps d'afegir llibre
    var titol: String = ""
    var assignatura: String = ""
    var curs: String = ""
    var editorial: String = ""
    var estat: String = ""

    //variable de la imatge a pujar al storage amb data i hora local
    var fileName: String = SimpleDateFormat(
        FILENAME_FORMAT, Locale.US
    ).format(System.currentTimeMillis()) + ".jpg"

    //instancia que referencia al storage
    var refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")

    //variable per guardar el identificador del llibre
    var identificador: String = ""




    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val CAMERA_PERMISSION_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_afegir_llibre, container, false)

        //inicialitzem spinner estat
        spinnerEstat = binding.spnAfegirEstat
        //carreguem els possibles estats a l'spinner
        context?.let {
            ArrayAdapter.createFromResource(it,R.array.estat, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    spinnerEstat.adapter = adapter
                }
        }
        //inicialitzem spinner curs
        spinnerCurs = binding.spnAfegirCurs
        //carreguem els possibles estats a l'spinner
        context?.let {
            ArrayAdapter.createFromResource(it,R.array.cursos, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    spinnerCurs.adapter = adapter
                }
        }
        //inicialitzem spinner assignatura
        spinnerAssignatura = binding.spnAfegirAssignatura
        //carreguem els possibles estats a l'spinner
        context?.let {
            ArrayAdapter.createFromResource(it,R.array.assignatures, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    spinnerAssignatura.adapter = adapter
                }
        }


        //activa la camara per fer foto del llibre
        binding.imageViewFoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //obre l'activity de la camara
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
            imgfoto = binding.imageViewLlibre


        }

        binding.bttnGuardarAfegirLli.setOnClickListener { view: View ->
            //comprovem que els camps no estiguin buits
            //i que el spinner no tingui posicio 0, perque seria el valor selecciona estat
            if (TextUtils.isEmpty(binding.editTextTitolAfegir.text) ||
                 TextUtils.isEmpty(binding.editTextEditorial.text)
            ) {
                Snackbar.make(view, R.string.omplirCamps, Snackbar.LENGTH_LONG).show()
            } else {
                //guardem la foto a la variable bitmap
                val bitmap = (imgfoto.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                //pugem la foto al Storage
                val uploadTask = refStorage.putBytes(data)
                uploadTask.addOnFailureListener {
                    Snackbar.make(view, R.string.errorFoto, Snackbar.LENGTH_LONG).show()

                }.addOnSuccessListener { taskSnapshot ->
                    //Snackbar.make(view, "Foto guardada", Snackbar.LENGTH_LONG).show()

                }
                //Permet seleccionar un camp del spinner
                spinnerEstat.onItemSelectedListener = this
                spinnerCurs.onItemSelectedListener = this
                spinnerAssignatura.onItemSelectedListener = this

                //obtenim la posicio de l'item selecionat de cada spinner
                val positionSpnEstat = spinnerEstat.selectedItemPosition
                val positionSpnCurs = spinnerCurs.selectedItemPosition
                val positionSpnAssignatura = spinnerAssignatura.selectedItemPosition

                //assignem els valors dels spinner
                estat = spinnerEstat.selectedItem.toString()
                curs = spinnerCurs.selectedItem.toString()
                assignatura = spinnerAssignatura.selectedItem.toString()
                //comprovem que s'ha seleccionat un valor als spinners
                if(positionSpnEstat ==0){
                    Snackbar.make(view, R.string.seleccionaEstat, Snackbar.LENGTH_LONG).show()
                }else if(positionSpnAssignatura== 0){
                    Snackbar.make(view, R.string.seleccionaAssignatura, Snackbar.LENGTH_LONG).show()
                }else if(positionSpnCurs== 0){
                    Snackbar.make(view, R.string.seleccionaCurs, Snackbar.LENGTH_LONG).show()
                }else {
                    //cridem a la funcio afegirLlibre per guardar el nou llibre
                    afegirLlibre()
                    view.findNavController().navigate(R.id.action_afegirLlibre_to_meusLlibres)
                }



            }


        }
        return binding.root
    }

    //funcio per guardar el llibre a l'usuari identificat
    fun afegirLlibre() {


        //guardem les dades de l'usari identificat
        val user = Firebase.auth.currentUser
        //agafem el mail com a identificador unic de l'usuari
        val mail = user?.email.toString()

        titol = binding.editTextTitolAfegir.text.toString()
        curs = spinnerCurs.selectedItem.toString()
        assignatura = spinnerAssignatura.selectedItem.toString()
        editorial = binding.editTextEditorial.text.toString()
        estat = spinnerEstat.selectedItem.toString()

        //accedeim a la col.lecció usuaris per recollir la poblacio de l'usuari identificat
         db.collection("usuaris").whereEqualTo("mail",mail).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("dadesp", "${document.id} => ${document["poblacio"]}")
                    Log.d("dades", "${document.id} => ${document.data}")
                }

                val poblacioConsulta = documents.toObjects(Llibre::class.java)
                val poblacio = poblacioConsulta[0].poblacio
                Log.d("poblacio", poblacio)

                //generar idenditificar aleatori del llibre
                identificador = UUID.randomUUID().toString()

                val llibre = hashMapOf(
                    "mail" to mail,
                    "titol" to titol,
                    "curs" to curs,
                    "assignatura" to assignatura,
                    "editorial" to editorial,
                    "id" to identificador,
                    "foto" to fileName,
                    "poblacio" to poblacio,
                    "estat" to estat
                )

                //guardem el llibre a la col.leccio
                db.collection("llibres").add(llibre)
                    .addOnSuccessListener { documentReference ->
                        Log.d(
                            ContentValues.TAG,
                            "DocumentSnapshot added with ID: ${documentReference.id}"
                        )

                    }.addOnFailureListener { e ->
                        Log.w(ContentValues.TAG, "Error adding document", e)
                    }

            }

    }


    //comprova els permisos de la camara
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)

            } else {
                Snackbar.make(
                    requireView(), R.string.permisosCamara +
                            + R.string.infoCamara, Snackbar.LENGTH_LONG
                ).show()

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                val foto: Bitmap = data!!.extras!!.get("data") as Bitmap
                imgfoto.setImageBitmap(foto)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)


    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}
