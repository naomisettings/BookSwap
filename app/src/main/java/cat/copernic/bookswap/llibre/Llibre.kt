package cat.copernic.bookswap.llibre

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentLlibreBinding
import cat.copernic.bookswap.databinding.FragmentRegistreBinding

class Llibre : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentLlibreBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_llibre, container, false)
        return binding.root
    }


}