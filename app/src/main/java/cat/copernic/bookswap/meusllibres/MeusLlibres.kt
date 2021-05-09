package cat.copernic.bookswap.meusllibres

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
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
        return binding.root
    }

}