package cat.copernic.bookswap.llistatllibres

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentAfegirLlibreBinding
import cat.copernic.bookswap.databinding.FragmentLlistatLlibresBinding

class LlistatLlibres : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentLlistatLlibresBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_llistat_llibres, container, false)
        return binding.root
    }


}