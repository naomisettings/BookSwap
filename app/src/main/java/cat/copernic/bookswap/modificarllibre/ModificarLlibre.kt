package cat.copernic.bookswap.modificarllibre

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentModificarLlibreBinding
import cat.copernic.bookswap.utils.Llibres
import coil.api.load
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ModificarLlibre : Fragment() {
    //instancia a firebase
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: FragmentModificarLlibreBinding
    private lateinit var args: ModificarLlibreArgs


    private lateinit var spinnerModificarEstat: Spinner
    private lateinit var spinnerModificarCurs: Spinner
    private lateinit var spinnerModificarAssignatura: Spinner
    private var estat = ""
    private var curs = ""
    private var assignatura = ""

    private lateinit var imgfoto: ImageView

    //variable de la imatge a pujar al storage amb data i hora local
    private var fileName: String = SimpleDateFormat(
        FILENAME_FORMAT, Locale.US
    ).format(System.currentTimeMillis()) + ".jpg"

    //instancia que referencia al storage
    var refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")

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
            DataBindingUtil.inflate(inflater, R.layout.fragment_modificar_llibre, container, false)
        //rebre dades del llibre seleccionat
        args = ModificarLlibreArgs.fromBundle(requireArguments())

        //funcio per carregar les dades del llibre seleccionat
        mostrarLlibre()


        binding.btnActualitzarLlibre.setOnClickListener {
            //guardem la foto a la variable bitmap
            val bitmap = (imgfoto.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            //pugem la foto al Storage
            val uploadTask = refStorage.putBytes(data)
            uploadTask.addOnFailureListener {
                Snackbar.make(requireView(), "Error al guardar la foto", Snackbar.LENGTH_LONG)
                    .show()

            }.addOnSuccessListener { taskSnapshot ->
                //Snackbar.make(view, "Foto guardada", Snackbar.LENGTH_LONG).show()

            }
            //cridem a la funcio per actualitzar el llibre passant com argument el id del llibre
            actualitzarLlibre(args.id)
            view?.findNavController()?.navigate(R.id.action_modificarLlibre_to_meusLlibres)
        }

        //activa la camara per fer foto del llibre
        binding.imageViewFotoModificar.setOnClickListener {
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
            imgfoto = binding.imageViewModificarLlibre


        }

        return binding.root
    }

    private fun actualitzarLlibre(idLlibre: String) {

        //agafem el llibre de la coleccio amb el seu ID
        Log.i("idLlibre", idLlibre)
        db.collection("llibres").addSnapshotListener { snapshot, error ->
            //guardem els documents
            val doc = snapshot?.documents
            //iterem pels documents dels llibres
            doc?.forEach {
                val llibreConsulta = it.toObject(Llibres::class.java)
                if (llibreConsulta?.id == idLlibre) {
                    //guardem el id del document del llibre seleccionat
                    val llibreId = it.id
                    //agafem el id del llibre
                    val sfDocRef = db.collection("llibres").document(llibreId)
                    Log.i("idLlibre", llibreId)

                    //agafem els valors dels spinners
                    assignatura = spinnerModificarAssignatura.selectedItem.toString()
                    curs = spinnerModificarCurs.selectedItem.toString()
                    estat = spinnerModificarEstat.selectedItem.toString()

                    //actualitzem les dades
                    db.runTransaction { transaction ->

                        transaction.update(
                            sfDocRef,
                            "titol",
                            binding.editTextTitolModificar.text.toString()
                        )
                        transaction.update(sfDocRef, "assignatura", assignatura)
                        transaction.update(
                            sfDocRef,
                            "editorial",
                            binding.editTextEditorialModificar.text.toString()
                        )
                        transaction.update(sfDocRef, "curs", curs)
                        transaction.update(sfDocRef, "estat", estat)
                        transaction.update(sfDocRef, "foto", fileName)

                    }.addOnSuccessListener {
                        Log.d("TAG", "Transaction success!")
                        view?.let {
                            Snackbar.make(
                                it,
                                "Llibre actualitzat",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }.addOnFailureListener { e ->
                        Log.w("TAG2", "Transaction failure.", e)
                        view?.let {

                            Snackbar.make(
                                it,
                                "Error a l'actualitzar el llibre",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                        }

                    }
                }
            }
        }


    }

    //funcio que mostra les dades del llibre seleccionat
    private fun mostrarLlibre() {
        //rebre dades del llibre seleccionat
        //val args = ModificarLlibreArgs.fromBundle(requireArguments())
        //guardem els valors als editText
        binding.editTextTitolModificar.setText(args.titol)
        binding.editTextEditorialModificar.setText(args.editorial)
        Log.i("idmostrar", args.id)


        //inicialitzem spinner estat
        spinnerModificarEstat = binding.spnModificarEstat
        //carreguem els possibles estats a l'spinner
        context?.let {
            ArrayAdapter.createFromResource(it, R.array.estat, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    //guardem la posicio del valor
                    val positionSpnEstat = adapter.getPosition(args.estat)
                    spinnerModificarEstat.adapter = adapter
                    //assignem la posicio perque ens mostri el valor
                    spinnerModificarEstat.setSelection(positionSpnEstat)
                }
        }
        //inicialitzem spinner curs
        spinnerModificarCurs = binding.spnModificarCurs
        //carreguem els possibles estats a l'spinner
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.cursos,
                android.R.layout.simple_spinner_item
            )
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    //guardem la posicio del valor
                    val positionSpnCurs = adapter.getPosition(args.curs)
                    spinnerModificarCurs.adapter = adapter
                    //assignem la posicio perque ens mostri el valor
                    spinnerModificarCurs.setSelection(positionSpnCurs)
                }
        }
        //inicialitzem spinner assignatura
        spinnerModificarAssignatura = binding.spnModificarAssignatura
        //carreguem els possibles estats a l'spinner
        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.assignatures,
                android.R.layout.simple_spinner_item
            )
                .also { adapter ->
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    //guardem la posicio del valor
                    val positionSpnAssignatura = adapter.getPosition(args.assignatura)
                    spinnerModificarAssignatura.adapter = adapter
                    //assignem la posicio perque ens mostri el valor
                    spinnerModificarAssignatura.setSelection(positionSpnAssignatura)
                }
        }


        //per carregar la imatge del llibre selecionat
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${args.foto}")

        imageRef.downloadUrl.addOnSuccessListener { url ->
            binding.imageViewModificarLlibre.load(url)
        }.addOnFailureListener { }

        Log.i("idmostrar", args.id)

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