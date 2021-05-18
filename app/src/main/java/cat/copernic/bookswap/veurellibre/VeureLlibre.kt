package cat.copernic.bookswap.veurellibre

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentVeureLlibreBinding

class VeureLlibre : Fragment() {

    private lateinit var binding: FragmentVeureLlibreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_veure_llibre, container, false)


        return binding.root
    }


}