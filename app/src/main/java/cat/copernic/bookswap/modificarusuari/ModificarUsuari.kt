package cat.copernic.bookswap.modificarusuari

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentAfegirLlibreBinding
import cat.copernic.bookswap.databinding.FragmentModificarUsuariBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ModificarUsuari : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentModificarUsuariBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_modificar_usuari, container, false)

        binding.btnGuardarLLibre.setOnClickListener{
            //SingOut
            Firebase.auth.signOut()
            //Obra el fragment principal
            findNavController().navigate(R.id.action_modificarUsuari_to_loginFragment)
        }

        return binding.root
    }


}