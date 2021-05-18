package cat.copernic.bookswap.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.databinding.ItemLlibreBinding
class Adapter(var mLlibres: List<Llibre>, var cellClickListener: CellClickListener) :  RecyclerView.Adapter<Adapter.ViewHolder>(){

    // creem una classe interna amb el nom ViewHolder
    // Pren un argument del view, en què passa la classe generada de item_llibre.xml
    // És a dir, ItemLlibreBinding i al RecyclerView.ViewHolder (binding.root) ho passen així
    inner class ViewHolder(val binding: ItemLlibreBinding) : RecyclerView.ViewHolder(binding.root)

    // dins de onCreateViewHolder infla la vista de ItemLlibreBinding
    // i torna el nou objecte ViewHolder que conté aquest disseny
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       // val binding = ItemLlibreBinding
        //    .inflate(LayoutInflater.from(parent.context), parent, false)

        //return ViewHolder(binding)
        return ViewHolder(ItemLlibreBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    // lliguem els ítems amb cada ítem de la llista mLlibres quin serà
    // es mostra a la vista de reciclatge
    // per simplificar-ho, no configurem cap dada d'imatge per visualitzar
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(mLlibres[position]){
                binding.edAssignatura.text = this.assignatura
                binding.titol.text = this.titol
                binding.edCurs.text = this.curs
                binding.edEditorial.text = this.editorial
                binding.edEstat.text=this.estat

                holder.itemView.setOnClickListener {
                    cellClickListener.onCellClickListener(mLlibres[position])
                }

            }
        }
    }

    // torna la mida de la llista mLlibres
    override fun getItemCount(): Int {
        return mLlibres.size
    }
}
//Listener que gestiona els clics al elements del RecyclerView
open class CellClickListener(val clickListener: (titol: String, assignatura: String, editorial: String, curs: String, estat:String) -> Unit) {
    fun onCellClickListener(data: Llibre) {
        clickListener(data.titol, data.assignatura, data.curs, data.editorial,data.estat)

    }
}


