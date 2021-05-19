package cat.copernic.bookswap.utils

data class UsuariDC(
    var mail: String = "",
    var nom: String = "",
    var telefon: String = "",
    var poblacio: String = "",
    var valoracio: Int = 6
)
data class Llibres(
    var titol: String ="",
    var curs: String ="",
    var assignatura: String ="",
    var editorial: String ="",
    var estat: String ="",
    var foto: String ="",
    var id: String ="",
    var mail: String = ""

)