package cat.copernic.bookswap.meusllibres

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentMeusLlibresBinding

class MeusLlibres : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentMeusLlibresBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_meus_llibres, container, false)


        //acció floatinButton cap a la pantalla afegirLlibre
        binding.floatingActionButton2.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_meusLlibres_to_afegirLlibre)
        }

        return binding.root
    }


}