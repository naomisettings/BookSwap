package cat.copernic.bookswap.registre

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentRegistreBinding

class RegistreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentRegistreBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_registre, container, false)

        return inflater.inflate(R.layout.fragment_registre, container, false)
    }
}