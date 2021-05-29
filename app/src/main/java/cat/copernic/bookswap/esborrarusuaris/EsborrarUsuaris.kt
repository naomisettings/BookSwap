package cat.copernic.bookswap.esborrarusuaris

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentEsborrarUsuarisBinding
import cat.copernic.bookswap.utils.Usuari
import cat.copernic.bookswap.viewmodel.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class EsborrarUsuaris : Fragment() {

    private lateinit var binding: FragmentEsborrarUsuarisBinding

    private lateinit var adapter: AdapterUsuaris
    private lateinit var rvUsuaris: RecyclerView
    private val viewModel: ViewModel by viewModels()

    private var usuarisList: MutableList<Usuari> = mutableListOf()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_esborrar_usuaris, container, false)

        rvUsuaris = binding.rcyViewUsuaris

        carregarLlibresLlistat()




        return binding.root
    }

    private fun carregarLlibresLlistat() {

        //Truca a la consulta del view model per extreure les dades del usuari loginat
        viewModel.totsUsuaris().observe(requireActivity(), { usuaris ->

            usuaris?.forEach { usuari ->
                if (!usuari.expulsat) {
                    usuarisList.add(usuari)
                }
            }
            veureRecyclerView()

            binding.bttnEsborrar.setOnClickListener {

                baixaUsuari(adapter.checkedUsuaris)

                usuarisList.removeAll(adapter.checkedUsuaris)

                rvUsuaris.removeAllViews()
            }

        })
    }

    private fun veureRecyclerView() {
        adapter = AdapterUsuaris(usuarisList)
        this.rvUsuaris.adapter = adapter
        this.rvUsuaris.layoutManager = LinearLayoutManager(this.context)

    }

    private fun baixaUsuari(checkedUsuaris: MutableList<Usuari>) {
        for (i in 0 until checkedUsuaris.size) {
            db.collection("usuaris").whereEqualTo("mail", checkedUsuaris[i].mail).get()
                .addOnSuccessListener { document ->
                    document?.forEach { doc ->
                        val sfDocRef = db.collection("usuaris").document(doc.id)

                        db.runTransaction { transaction ->
                            transaction.update(
                                sfDocRef,
                                "expulsat",
                                true
                            )
                            null
                        }.addOnSuccessListener {
                            Log.d("TAG", "Transaction success!")

                        }.addOnFailureListener { e ->
                            Log.w("TAG2", "Transaction failure.", e)
                        }

                        db.collection("llibres").whereEqualTo("mail", checkedUsuaris[i].mail)
                            .get()
                            .addOnSuccessListener { document ->
                                document?.forEach {

                                    //Extreu la id del document
                                    val llibresId = it.id

                                    val sfDocRefLlibres =
                                        db.collection("llibres").document(llibresId)

                                    //Esborra els llibres publicats per l'usuari
                                    db.runTransaction { transaction ->
                                        //agafem el ID
                                        val snapshot = transaction.get(sfDocRefLlibres)
                                        //actualitzem el id amb el mail del usuari identificat i guardem els camps
                                        snapshot.getString("mail")!!

                                        //esborrem usuari del Firestore
                                        transaction.delete(sfDocRefLlibres)

                                    }.addOnFailureListener { exception ->
                                        Log.w(
                                            ContentValues.TAG,
                                            "Error getting documents: ",
                                            exception
                                        )
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                            }
                    }
                }.addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                }
        }
    }
}