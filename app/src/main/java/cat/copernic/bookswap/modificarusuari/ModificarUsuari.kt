package cat.copernic.bookswap.modificarusuari

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentAfegirLlibreBinding
import cat.copernic.bookswap.databinding.FragmentModificarUsuariBinding


class ModificarUsuari : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentModificarUsuariBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_modificar_usuari, container, false)
        return binding.root
    }


}