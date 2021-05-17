package cat.copernic.bookswap.utils

import android.database.AbstractCursor

data class UsuariDC(
    var mail: String = "",
    var nom: String = "",
    var telefon: String = "",
    var poblacio: String = "",
    var valoracio: Int = 6
)
data class Llibres(
    var titol: String = "",
    var assignatura: String = "",
    var curs: String="",
    var editorial: String,
    var foto: String

)