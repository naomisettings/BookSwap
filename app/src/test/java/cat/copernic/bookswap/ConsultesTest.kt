package cat.copernic.bookswap

import cat.copernic.bookswap.utils.Usuari
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class ConsultesTest {

    @Test
    fun usuariLogin() {
        val c = Mockito.mock(Usuari::class.java)

        `when`(c.mail).thenReturn("")
        val prop = c.mail
        assertEquals("", prop)
    }
}