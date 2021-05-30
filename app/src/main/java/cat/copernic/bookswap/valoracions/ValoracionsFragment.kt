package cat.copernic.bookswap.valoracions


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentValoracionsBinding
import cat.copernic.bookswap.utils.UsuariDC
import cat.copernic.bookswap.viewmodel.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

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


        //guardem les valoracions enviant les dades del llibre al fragment VeureLLibre
        binding.btnOk.setOnClickListener {
            valoracions(tempsValor, estatValor, satisfaccioValor)
            view?.findNavController()?.navigate(
                ValoracionsFragmentDirections.actionValoracionsFragmentToVeureLlibre(
                    args.titol,
                    args.assignatura,
                    args.editorial,
                    args.curs,
                    args.estat,
                    args.foto,
                    args.id,
                    args.mail
                )
            )

        }
        //cancelem les valoracions enviant les dades del llibre al fragment VeureLLibre
        binding.btnCancelar.setOnClickListener {
            view?.findNavController()?.navigate(
                ValoracionsFragmentDirections.actionValoracionsFragmentToVeureLlibre(
                    args.titol,
                    args.assignatura,
                    args.editorial,
                    args.curs,
                    args.estat,
                    args.foto,
                    args.id,
                    args.mail
                )
            )
        }

        return binding.root
    }

    private fun valoracions(temps: Double, estat: Double, satisfaccio: Double) {
        args = ValoracionsFragmentArgs.fromBundle(requireArguments())
        //guardem la mitja de les 3 valoracions entrades
        val valoracioTotal = (temps + estat + satisfaccio) / 3
        val mail = args.mail
        Log.i("temps", temps.toString())
        Log.i("estat", estat.toString())
        Log.i("satisfaccio", satisfaccio.toString())
        Log.i("valoracioTotal", valoracioTotal.toString())
        db.collection("usuaris").whereEqualTo("mail", mail).get().addOnSuccessListener { docs ->

            val usuariConsulta = docs.toObjects(UsuariDC::class.java)
            Log.i("inicifor", "inicifor")
            docs?.forEach {
                //comprobem si l'usuari te valoracions, si no en te afegeix l'actual
                val usuariId = it.id
                if (usuariConsulta[0].mail == mail) {
                    val sfDocRef = db.collection("usuaris").document(usuariId)
                    if (usuariConsulta[0].valoracio == 0.0) {

                        db.runTransaction { transaction ->

                            transaction.update(sfDocRef, "valoracio", valoracioTotal)


                        }.addOnSuccessListener {
                            Log.d("TAG", "Transaction success!")
                            view?.let {
                                Snackbar.make(
                                    it,
                                    R.string.valoracioGuardada,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }.addOnFailureListener { e ->
                            Log.w("TAG2", "Transaction failure.", e)
                            view?.let {

                                Snackbar.make(
                                    it,
                                    R.string.errorValoracio,
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                            }

                        }

                    } else {
                        //si l'usuari ja tenia valoracions fa la mitjana
                        //entre la nova i les existents i l'actulitzem
                        val valoracioMitjaConsulta = usuariConsulta[0].valoracio
                        val mitjaConsulta: Double = valoracioMitjaConsulta
                        val mitjana: Double = (mitjaConsulta + valoracioTotal) / 2

                        Log.i("valoracioMitja", valoracioMitjaConsulta.toString())
                        Log.i("mitjana", mitjana.toString().format("%.2f"))
                        db.runTransaction { transaction ->
                            transaction.update(sfDocRef, "valoracio", mitjana)

                        }.addOnSuccessListener {
                            Log.d("TAG", "Transaction success!")
                            view?.let {
                                Snackbar.make(
                                    it,
                                    R.string.valoracioGuardada,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }.addOnFailureListener { e ->
                            Log.w("TAG2", "Transaction failure.", e)
                            view?.let {

                                Snackbar.make(
                                    it,
                                    R.string.errorValoracio,
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                            }

                        }


                    }

                }
            }

        }

    }
}


