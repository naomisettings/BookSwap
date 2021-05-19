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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentAfegirLlibreBinding
import cat.copernic.bookswap.utils.Llibres
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList


class AfegirLlibre : Fragment() {

    private lateinit var binding: FragmentAfegirLlibreBinding
    private lateinit var imgfoto: ImageView
    var llibresMap: HashMap<String, String> = hashMapOf()

    //instancia a firebase
    val db = FirebaseFirestore.getInstance()

    //inicialitzem les variables dels camps d'afegir llibre
    var titol: String = ""
    var assignatura: String = ""
    var curs: String = ""
    var editorial: String = ""

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
                TextUtils.isEmpty(binding.editTextAssignatura.text) ||
                TextUtils.isEmpty(binding.editTextCurs.text) || TextUtils.isEmpty(binding.editTextEditorial.text)
            ) {
                Snackbar.make(view, "Has d'omplir tots els camps", Snackbar.LENGTH_LONG).show()
            } else {
                //guardem la foto a la variable bitmap
                val bitmap = (imgfoto.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                //pugem la foto al Storage
                val uploadTask = refStorage.putBytes(data)
                uploadTask.addOnFailureListener {
                    Snackbar.make(view, "Error al guardar la foto", Snackbar.LENGTH_LONG).show()

                }.addOnSuccessListener { taskSnapshot ->
                    Snackbar.make(view, "Foto guardada", Snackbar.LENGTH_LONG).show()

                }

                afegirLlibre()
                view.findNavController().navigate(R.id.action_afegirLlibre_to_meusLlibres)

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
        curs = binding.editTextCurs.text.toString()
        assignatura = binding.editTextAssignatura.text.toString()
        editorial = binding.editTextEditorial.text.toString()

        //identificador unic amb data i hora per cada llibre introduït
        /*
        identificador = SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis())
         */

        identificador = UUID.randomUUID().toString()

        val llibre = hashMapOf(
            "mail" to mail,
            "titol" to titol,
            "curs" to curs,
            "assignatura" to assignatura,
            "editorial" to editorial,
            "id" to identificador,
            "foto" to fileName
        )


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
                    requireView(), "Has denegat els permisos de la camara. \n"
                            + "Els pots modificar a la configuració del mòbil", Snackbar.LENGTH_LONG
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
}
