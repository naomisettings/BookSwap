package cat.copernic.bookswap.valoracions


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentValoracionsBinding
import cat.copernic.bookswap.utils.UsuariDC
import cat.copernic.bookswap.veurellibre.VeureLlibreArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField

class ValoracionsFragment : Fragment() {

    //instancia a firebase
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: FragmentValoracionsBinding
    private lateinit var args: ValoracionsFragmentArgs

    var titol: String = ""
    var assignatura: String = ""
    var curs: String = ""
    var editorial: String = ""
    var estat: String = ""

    var tempsValor = 0.0
    var estatValor = 0.0
    var satisfaccioValor = 0.0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_valoracions, container, false)

        //rebre dades del llibre seleccionat
        args = ValoracionsFragmentArgs.fromBundle(requireArguments())
        //inicialitzem els ratingBar amb els valors assignats per l'usuari
        binding.ratingBarTemps.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            tempsValor = rating.toDouble()

        }
        binding.ratingBarEstat.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            estatValor = rating.toDouble()
        }
        binding.ratingBarSatisfaccio.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            satisfaccioValor = rating.toDouble()
        }



        binding.btnOk.setOnClickListener {
            valoracions(tempsValor,estatValor,satisfaccioValor)
            view?.findNavController()?.navigate(ValoracionsFragmentDirections.actionValoracionsFragmentToVeureLlibre(
                args.titol,
                args.assignatura,
                args.editorial,
                args.curs,
                args.estat,
                args.foto,
                args.id,
                args.mail))
        }

        binding.btnCancelar.setOnClickListener {
            view?.findNavController()?.navigate(ValoracionsFragmentDirections.actionValoracionsFragmentToVeureLlibre(
                args.titol,
                args.assignatura,
                args.editorial,
                args.curs,
                args.estat,
                args.foto,
                args.id,
                args.mail))
        }

        return binding.root
    }

    private fun valoracions(temps: Double, estat: Double, satisfaccio: Double) {
        args = ValoracionsFragmentArgs.fromBundle(requireArguments())
        val valoracioTotal = (temps + estat + satisfaccio) / 3
        val mail = args.mail
        Log.i("temps", temps.toString())
        Log.i("estat", estat.toString())
        Log.i("satisfaccio", satisfaccio.toString())
        Log.i("valoracioTotal", valoracioTotal.toString())
        db.collection("usuaris").addSnapshotListener{ snapshot, error ->
            //guardem els documents
            val doc = snapshot?.documents
            Log.i("inicifor", "inicifor")
            doc?.forEach {
                val usuariConsulta = it.toObject(UsuariDC::class.java)
                val usuariId = it.id
                if(usuariConsulta?.mail == mail){
                    val sfDocRef = db.collection("usuaris").document(usuariId)
                    if(usuariConsulta.valoracio == 0){

                        db.runTransaction { transaction ->

                            transaction.update(sfDocRef, "valoracio", valoracioTotal)

                        null
                        }.addOnSuccessListener {
                            Log.d("TAG", "Transaction success!")
                            view?.let {
                                Snackbar.make(
                                    it,
                                    "Valoració guardada",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }.addOnFailureListener { e ->
                            Log.w("TAG2", "Transaction failure.", e)
                            view?.let {

                                Snackbar.make(it, "Error al guardar la valoració", Snackbar.LENGTH_LONG)
                                    .show()
                            }

                        }
                        Log.i("fi del if", "fi del if")
                    }else{
                    //if(usuariConsulta?.valoracio != 0){

                        val valoracioMitjaConsulta = it.get("valoracio")
                        val mitjaConsulta: Double = valoracioMitjaConsulta as Double
                        val mitjana: Double = (mitjaConsulta + valoracioTotal) /2
                        Log.i("valoracioMitja", valoracioMitjaConsulta.toString())
                        Log.i("mitjana", mitjana.toString())
                        db.runTransaction { transaction ->
                            transaction.update(sfDocRef, "valoracio", mitjana)
                            null
                        }.addOnSuccessListener {
                        Log.d("TAG", "Transaction success!")
                        view?.let {
                            Snackbar.make(
                                it,
                                "Valoració guardada",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }.addOnFailureListener { e ->
                        Log.w("TAG2", "Transaction failure.", e)
                        view?.let {

                            Snackbar.make(it, "Error al guardar la valoració", Snackbar.LENGTH_LONG)
                                .show()
                        }

                    }


                    }

                }
            }

        }

    }
}


