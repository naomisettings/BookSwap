package cat.copernic.bookswap.utils

data class UsuariDC(
    var mail: String = "",
    var nom: String = "",
    var telefon: String = "",
    var poblacio: String = "",
    var valoracio: Int = 6
)
data class Llibres(
    var llibresUsuari: ArrayList<HashMap<String, String>> = arrayListOf(),
    var mail: String = ""

)