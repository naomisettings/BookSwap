package cat.copernic.bookswap.registre

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import cat.copernic.bookswap.R
import cat.copernic.bookswap.databinding.FragmentRegistreBinding

class RegistreFragment : Fragment() {

    private lateinit var viewModel: RegistreViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentRegistreBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_registre, container, false)

        viewModel = ViewModelProvider(this).get(RegistreViewModel::class.java)

        return inflater.inflate(R.layout.fragment_registre, container, false)
    }
}