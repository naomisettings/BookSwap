package cat.copernic.bookswap.utils

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cat.copernic.bookswap.databinding.ItemLlibreBinding
import coil.api.load
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlin.coroutines.coroutineContext

class Adapter(var mLlibres: List<Llibre>, var cellClickListener: CellClickListener) :  RecyclerView.Adapter<Adapter.ViewHolder>(){

    // creem una classe interna amb el nom ViewHolder
    // Pren un argument del view, en què passa la classe generada de item_llibre.xml
    // És a dir, ItemLlibreBinding i al RecyclerView.ViewHolder (binding.root) ho passen així
    inner class ViewHolder(val binding: ItemLlibreBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(llibre: Llibre){


        }
    }


    // dins de onCreateViewHolder infla la vista de ItemLlibreBinding
    // i torna el nou objecte ViewHolder que conté aquest disseny
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {



        //return ViewHolder(binding)
        return ViewHolder(ItemLlibreBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    // lliguem els ítems amb cada ítem de la llista mLlibres quin serà
    // es mostra a la vista de reciclatge
    // per simplificar-ho, no configurem cap dada d'imatge per visualitzar
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(mLlibres[position]){
                binding.titol.text = this.titol
                binding.edAssignatura.text = this.assignatura
                binding.edEditorial.text = this.editorial
                binding.edCurs.text = this.curs
                binding.edEstat.text=this.estat
                binding.edPoblacio.text = this.poblacio

                if (this.poblacio_login != this.poblacio) {
                    binding.edPoblacio.setTextColor(Color.parseColor("#FF9800"))
                }
                val media: String = mLlibres[position].foto

                //Descarregar la imatge amb picasso
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/${mLlibres[position].foto}")

                //Monstrar la imatge
                imageRef.downloadUrl.addOnSuccessListener { url ->
                    binding.imgCardView.load(url)
                }.addOnFailureListener {

                }

                holder.bind(mLlibres[position])
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
open class CellClickListener(val clickListener: (titol: String, assignatura: String,
                                                 editorial: String, curs: String, estat:String, foto:String, id: String, mail:String) -> Unit) {
    fun onCellClickListener(data: Llibre) {
        clickListener(data.titol, data.assignatura, data.editorial, data.curs, data.estat, data.foto, data.id, data.mail)

    }
}



