package cat.copernic.bookswap.utils

data class UsuariDC(
    var mail: String = "",
    var nom: String = "",
    var telefon: String = "",
    var poblacio: String = "",
    var valoracio: Double = 6.0,
    var comptador_valoracions: Int = 0,
    var admin: Boolean = false
)
data class Llibres(
    var titol: String ="",
    var curs: String ="",
    var assignatura: String ="",
    var editorial: String ="",
    var estat: String ="",
    var foto: String ="",
    var id: String ="",
    var mail: String = "",
    var poblacio: String = ""
)

data class poblacio(
    val poblacionsList: ArrayList<String> = arrayListOf(
        "Badia del Vallès",
        "Badia del Vallès",
        "Barberà del Vallès",
        "Castellar del Vallès",
        "Castellbisbal",
        "Cerdanyola del Vallès",
        "Gallifa",
        "Matadepera",
        "Montcada i Reixac",
        "Palau-solità i Plegamans",
        "Polinyà",
        "Rellinars",
        "Ripollet",
        "Rubí",
        "Sabadell",
        "Sant Cugat de Vallès",
        "Sant Llorenç Savall",
        "Sant Quirze del Vallès",
        "Santa Perpètua de Mogoda",
        "Sentmenat",
        "Terrassa",
        "Ullastrell",
        "Vacarisses",
        "Viladecavalls"
    )
)