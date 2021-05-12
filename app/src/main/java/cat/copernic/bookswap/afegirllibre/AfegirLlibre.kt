package cat.copernic.bookswap.afegirllibre

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentAfegirLlibreBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase



class AfegirLlibre : Fragment() {

    //instancia a firebase
    val db = FirebaseFirestore.getInstance()
    //inicialitzem les variables dels camps d'afegir llibre
    var titol: String = ""
    var assignatura: String = ""
    var curs: String = ""
    var editorial: String = ""
    //variable de la imatge a pujar al storage
    var fileName: String = ""
    //cridem al Singleton
    init {
        Singleton.nomImg
        Singleton.rutaImg
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAfegirLlibreBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_afegir_llibre, container, false)

        //activa la camara per fer foto del llibre
        binding.imageViewFoto.setOnClickListener {
            val intent = Intent(activity,CameraX::class.java).apply {  }
            startActivity(intent)
        }

        binding.bttnGuardarAfegirLli.setOnClickListener { view: View ->
            //comprovem que els camps no estiguin buits
            if(TextUtils.isEmpty(binding.editTextTitolAfegir.text)||
                TextUtils.isEmpty(binding.editTextAssignatura.text)||
                TextUtils.isEmpty(binding.editTextCurs.text)||TextUtils.isEmpty(binding.editTextEditorial.text)){
                Snackbar.make(view, "Has d'omplir tots els camps", Snackbar.LENGTH_LONG).show()
            }else{
                guardarLlibre()
            }
        }
        return binding.root

    }

    private fun guardarLlibre() {
        //guardem les dades de l'usari identificat
        val user = Firebase.auth.currentUser
        //agafem el mail com a identificador unic de l'usuari
        val mail = user?.email.toString()

        db.collection("llibres").whereEqualTo("mail", mail).get().addOnSuccessListener{doc->

        }
    }
}
