package cat.copernic.bookswap.esborrarusuaris

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.databinding.ItemEsborrarUsuarisBinding
import cat.copernic.bookswap.databinding.FragmentEsborrarUsuarisBinding
import cat.copernic.bookswap.utils.Usuari
import java.util.ArrayList


class AdapterUsuaris(var mUsuaris: MutableList<Usuari>) :
    RecyclerView.Adapter<AdapterUsuaris.ViewHolder>() {

    var checkedUsuaris: MutableList<Usuari> = mutableListOf()

    // creem una classe interna amb el nom ViewHolder
    // Pren un argument del view, en què passa la classe generada de item_esborrar_usuari.xml
    // És a dir, ItemEsborrarUsuariBinding i al RecyclerView.ViewHolder (binding.root) ho passen així
    inner class ViewHolder(val binding: ItemEsborrarUsuarisBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(usuari: Usuari) {


        }
    }


    // dins de onCreateViewHolder infla la vista de ItemUsuariBinding
    // i torna el nou objecte ViewHolder que conté aquest disseny
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        //return ViewHolder(binding)
        return ViewHolder(
            ItemEsborrarUsuarisBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(mUsuaris[position]) {
                binding.nomUsuari.text = this.nom
                binding.mailUsuari.text = this.mail
            }

            this.binding.ckBoxEsborrar.setOnClickListener {
                checkedUsuaris.add(mUsuaris[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return mUsuaris.size
    }
}


