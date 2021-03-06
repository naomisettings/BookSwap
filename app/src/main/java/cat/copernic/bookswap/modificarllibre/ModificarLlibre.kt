package cat.copernic.bookswap.modificarllibre

import android.app.Activity
import android.content.Context
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
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
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

class ModificarLlibre : Fragment(),  AdapterView.OnItemSelectedListener {
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

    var imgfoto: ImageView? = null

    //variable de la imatge a pujar al storage amb data i hora local
    private var fileName: String = ""
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

        binding.btnActualitzarLlibre.setOnClickListener {view: View ->
            if(imgfoto != null){
                fileName = SimpleDateFormat(
                    FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".jpg"
                //guardem la foto a la variable bitmap
                val bitmap = (imgfoto?.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                //instancia que referencia al storage
                refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")
                //pugem la foto al Storage
                Log.i("foto", fileName)
                val uploadTask = refStorage.putBytes(data)
                uploadTask.addOnFailureListener {
                    Snackbar.make(requireView(), "Error al guardar la foto", Snackbar.LENGTH_LONG)
                        .show()

                }.addOnSuccessListener { taskSnapshot ->

                }

            }
            //cridem a la funcio per actualitzar el llibre passant com argument el id del llibre
            //actualitzarLlibre(args.id)
            alertaModificarLlibre(args.id)

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
        db.collection("llibres").whereEqualTo("id",idLlibre) .get().addOnSuccessListener { doc ->

            //iterem pels documents dels llibres
            doc?.forEach {

                    //guardem el id del document del llibre seleccionat
                    val llibreId = it.id
                    //agafem el id del llibre
                    val sfDocRef = db.collection("llibres").document(llibreId)
                    Log.i("idLlibre", llibreId)

                    //agafem els valors dels spinners
                    assignatura = spinnerModificarAssignatura.selectedItem.toString()
                    Log.i("assignatura", assignatura)
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
                        if(fileName != ""){
                            Log.i("abans update filename", fileName)
                            transaction.update(sfDocRef, "foto", fileName)
                            Log.i("despres update filename", fileName)
                        }


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

                    }
                //}
            }
        }


    }

    //funcio que mostra les dades del llibre seleccionat
    private fun mostrarLlibre() {

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
                            + "Els pots modificar a la configuraci?? del m??bil", Snackbar.LENGTH_LONG
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
    private fun alertaModificarLlibre(llibreId: String) {

        val dialog = context?.let {
            AlertDialog.Builder(it)
                .setIcon(R.drawable.bookswaplogo)
                .setTitle(args.titol)
                .setMessage(getString(R.string.snacbar_modificar_llibre))
                .setNegativeButton(R.string.cancelar) { view, _ ->
                    view.cancel()

                }
                .setPositiveButton(R.string.acceptar) { view, _ ->
                    //Truca a la funci?? baixaUsuari
                    actualitzarLlibre(llibreId)
                    view.dismiss()
                }
                .setCancelable(false)
                .create()
        }
        dialog!!.show()
    }



}