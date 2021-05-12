package cat.copernic.bookswap.afegirllibre

import android.net.Uri

object Singleton{
    init {
        println("Singleton class invoked")
    }
    var nomImg = ""
    get() = field
    set(value){
        field = value
    }

    var rutaImg: Uri? = null
    get() = field
    set(value) {
        field = value
    }
}