package cat.copernic.bookswap.utils

class Llibre(
    var titol: String = "",
    var curs: String = "",
    var assignatura: String = "",
    var editorial: String = "",
    var estat: String = "",
    var foto: String = "",
    var id: String = "",
    var poblacio: String = "",
    var mail: String = "",
    var poblacio_login: String = ""

    )

class Usuari(
    var mail: String = "",
    var nom: String = "",
    var telefon: String = "",
    var poblacio: String = "",
    var valoracio: Double = 6.0,
    var comptador_valoracions: Int = 0,
    var admin: Boolean = false,
    var expulsat: Boolean = false
)
