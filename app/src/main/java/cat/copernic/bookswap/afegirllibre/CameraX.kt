package cat.copernic.bookswap.afegirllibre

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.MenuItem
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.ActivityCameraXBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma:Double) -> Unit
class CameraX : AppCompatActivity() {
    private lateinit var binding: ActivityCameraXBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    init {
        Singleton.nomImg
        
    }
    //nom arxiu de la imatge a pujar al storage
    var fileName: String = ""
    val storage = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_camera_x)
        
        //permissos per accedir a la camara
        if(allPermissionsGranted()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        //listener per fer la foto
        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
            Thread.sleep(300)
            finish()
        }
         outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    //Mostrar back button toolbar
    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun takePhoto() {
        // Obtenim una referència estable del cas d'ús modificable de captura d'imatges
        val imageCapture = imageCapture ?: return
        //guardem el format i nom de la imatge
        val nameFile = SimpleDateFormat(FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".jpg"

        Singleton.nomImg = nameFile

        //creem un fitxer per contenir la imatge. Afegim una marca de temps
        // perquè el nom del fitxer sigui únic.
        val photoFile = File(
            outputDirectory,
            nameFile
        )

        // Creem un objecte d'opcions de sortida que contingui fitxer + metadades
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        val savedUri = Uri.fromFile(photoFile)
        Singleton.rutaImg = savedUri
        // Cridem a takePicture () a l'objecte imageCapture.
        // ha estat presa
        imageCapture.takePicture(
            //Passeu outputOptions, l'executor i una devolució de trucada
            // per quan es desi la imatge.
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    //error si falla la captura de la imatge
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)

                }

                //funcio que guarda la imatge si tot es correcte
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                    Toast.makeText(baseContext, "Imatge guardada", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun startCamera() {
        //instancia per vincular el cicle de vida de les càmeres al propietari del cicle de vida
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // S'utilitza per vincular el cicle de vida de la càmera
            // al LifecycleOwner dins del procés de l'aplicació.
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // inicialitzem la vista prèvia
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(480, 640))
                .build()


            // Seleccionem la càmera posterior com a predeterminada
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Deslliguem els casos d’ús abans de tornar a enllaçar
                cameraProvider.unbindAll()

                // Enllacem els casos d’ús a la càmera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
    //funcio per comprovar els permisos
    fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }
    //funcio que crea el directori per guardar la imatge
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
    //permisos per la camara
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        //Comprovem si el codi de sol·licitud és correcte; ignora-ho si no.
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            //Si es concedeixen els permisos, truqueu a startCamera ()
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                //Si no es concedeixen permisos, presenteu un toast per notificar
                // a l'usuari que no s'han concedit els permisos.
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //Generem uri per a la imatge
 /*   override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode == GALLERY_REQUEST_CODE
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null
        ) {

            // Get the Uri of data
            val file_uri = data.data!!

            if (file_uri != null) {
                uploadImageToFirebase(file_uri)
            }
        }
    }

    //funció per pujar la imatge al storage
    private fun uploadImageToFirebase(fileUri: Uri) {
        //Generar un nom per a la imatge
        val nameFoto = Singleton.nomImg

        Log.d("imguri", fileUri.toString())
        if (nameFoto == "") {
            fileName =  "masia.png"
        } else {
            fileName = nameFoto
        }

        //Crear una referencia per pujar la imatge
        val refStorage = FirebaseStorage.getInstance().reference.child("images/masia.png")

        refStorage.putFile(fileUri)
            .addOnSuccessListener(
                OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                    }
                })

            ?.addOnFailureListener(OnFailureListener { e ->
                print(e.message)
            })
    }*/

}