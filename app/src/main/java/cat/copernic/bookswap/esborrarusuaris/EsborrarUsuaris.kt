package cat.copernic.bookswap.esborrarusuaris

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentEsborrarUsuarisBinding

class EsborrarUsuaris : Fragment() {

    private lateinit var binding: FragmentEsborrarUsuarisBinding

    private lateinit var adapter: AdapterUsuaris

    private lateinit var rvUsuaris: RecyclerView
    private var usuaris: MutableList<Usuari> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_esborrar_usuaris, container, false)

        rvUsuaris = binding.rcyViewUsuaris

        carregarLlibresRyclrView()

        return binding.root
    }

    private fun carregarLlibresRyclrView() {

        val usuari = Usuari(
            nomUsuari = "Rrkrkr",
            mailUsuari = "asdf"
        )
        val usuari2 = Usuari(
            nomUsuari = "RrkrkrFff",
            mailUsuari = "asdfff"
        )
        usuaris.add(usuari)
        usuaris.add(usuari2)
        veureRecyclerView()

        binding.bttnEsborrar.setOnClickListener {

            usuaris.removeAll(adapter.checkedUsuaris)

            rvUsuaris.removeAllViews()
            veureRecyclerView()
        }
    }

    private fun veureRecyclerView() {

        adapter = AdapterUsuaris(usuaris)
        this.rvUsuaris.adapter = adapter
        this.rvUsuaris.layoutManager = LinearLayoutManager(this.context)

    }
}