package cat.copernic.bookswap

import cat.copernic.bookswap.llistatllibres.LlistatLlibres
import cat.copernic.bookswap.viewmodel.ViewModel
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.junit.Assert
import org.junit.Test


class LlistatLlibresTest {
    val user = Firebase.auth.currentUser
    val inf = LlistatLlibres()
    val inf2 = ViewModel()

    @Test
    fun noCaractersEspecials() {

        //Comprovar que passa els strings a minuscules i en caràcters ASCII (Guions tampoc)

        val mates = inf.noCaractersEspecials("Matemàtiques")
        Assert.assertEquals(mates, "matematiques")

        val palau = inf.noCaractersEspecials("Palau-solità i Plegamans")
        Assert.assertEquals(palau, "palausolita i plegamans")

        val barbera = inf.noCaractersEspecials("Barberà del Vallès")
        Assert.assertEquals(barbera, "barbera del valles")

        val informatica = inf.noCaractersEspecials("Informàtica")
        Assert.assertEquals(informatica, "informatica")
    }

    @Test
    fun consulta() {

        //Comprovar que passa els strings a minuscules i en caràcters ASCII (Guions tampoc)

        val usuari = inf2.usuariLoginat()
        Assert.assertEquals(usuari.value?.mail, user?.email)

    }
}