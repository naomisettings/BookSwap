package cat.copernic.bookswap.afegirllibre

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
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

    private lateinit var spinnerCurs:Spinner
    private lateinit var spinnerAssignatura: Spinner

    //instancia a firebase
    private val db = FirebaseFirestore.getInstance()

    //inicialitzem les variables dels camps d'afegir llibre
    private var titol: String = ""
    private var assignatura: String = ""
    private var curs: String = ""
    private var editorial: String = ""
    private var estat: String = ""
    private var imgfoto: ImageView? = null



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
//        spinnerCurs.setOnTouchListener(this)

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

            if (TextUtils.isEmpty(binding.editTextTitolAfegir.text) ||
                 TextUtils.isEmpty(binding.editTextEditorial.text)
            ) {
                Snackbar.make(view, R.string.omplirCamps, Snackbar.LENGTH_LONG).show()
            }else{
                if(imgfoto?.drawable != null){
                    //guardem la foto a la variable bitmap
                    val bitmap = (imgfoto?.drawable as BitmapDrawable).bitmap
                    Log.i("bitmap", bitmap.toString())
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
                }

                //Permet seleccionar un camp del spinner
                spinnerCurs.onItemSelectedListener = this
                spinnerAssignatura.onItemSelectedListener = this
                spinnerCurs.onItemClickListener


                //obtenim la posicio de l'item selecionat de cada spinner
                //val positionSpnEstat = spinnerEstat.selectedItemPosition
                val positionSpnCurs = spinnerCurs.selectedItemPosition
                val positionSpnAssignatura = spinnerAssignatura.selectedItemPosition

                //assignem els valors dels spinner
                curs = spinnerCurs.selectedItem.toString()
                assignatura = spinnerAssignatura.selectedItem.toString()
                //comprovem que s'ha seleccionat un valor als spinners
               if(positionSpnAssignatura== 0){
                    Snackbar.make(view, R.string.seleccionaAssignatura, Snackbar.LENGTH_LONG).show()
                }else if(positionSpnCurs== 0){
                    Snackbar.make(view, R.string.seleccionaCurs, Snackbar.LENGTH_LONG).show()
                }else {
                    //cridem a la funcio afegirLlibre per guardar el nou llibre
                    afegirLlibre()
                    //view.findNavController().navigate(R.id.action_afegirLlibre_to_meusLlibres)
                }



            }


        }
        return binding.root
    }
    fun guardarFoto(){
        //guardem la foto a la variable bitmap
        val bitmap = (imgfoto?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        //pugem la foto al Storage
        val uploadTask = refStorage.putBytes(data)
        uploadTask.addOnFailureListener {
            Snackbar.make(requireView(), R.string.errorFoto, Snackbar.LENGTH_LONG).show()

        }.addOnSuccessListener { taskSnapshot ->
            //Snackbar.make(view, "Foto guardada", Snackbar.LENGTH_LONG).show()

        }

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
        val teclat: View? =
            requireActivity().currentFocus //amagem el teclat utilitzant la classe InputMethodManager cridant al metode hideSorftInputFromWindow
        teclat?.clearFocus()
        if (teclat != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.getWindowToken(), 0)

        }

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
                if(imgfoto?.drawable == null){
                    fileName = ""

                }

                val llibre = hashMapOf(
                    "mail" to mail,
                    "titol" to titol,
                    "curs" to curs,
                    "assignatura" to assignatura,
                    "editorial" to editorial,
                    "id" to identificador,
                    "foto" to fileName,
                    "poblacio" to poblacio,
                    "estat" to "Disponible"
                )

                //guardem el llibre a la col.leccio
                db.collection("llibres").add(llibre)
                    .addOnSuccessListener { documentReference ->
                        Log.d(
                            ContentValues.TAG,
                            "DocumentSnapshot added with ID: ${documentReference.id}"
                        )
                        //snackbar que informa que s'ha esborrat l'article i permet desfer l'accio
                        Snackbar.make(
                             binding.root,
                             getString(R.string.llibre_afegit),
                             Snackbar.LENGTH_LONG
                         ).show()

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
                imgfoto?.setImageBitmap(foto)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)


    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {


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




//}
