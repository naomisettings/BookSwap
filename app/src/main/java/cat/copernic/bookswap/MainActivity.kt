package cat.copernic.bookswap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView:BottomNavigationView= findViewById(R.id.nav_view)

        val navController = findNavController(R.id.myNavHostFragment)

        // Passar cada identificador de menú com un conjunt d'identificadors perquè
        // cada menú s’ha de considerar com a destinacions de primer nivell.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.llistatLlibres,
                R.id.meusLlibres,
                R.id.modificarUsuari))

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

}