package app.pizzabutton.android.wearos.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import app.pizzabutton.android.common.models.Store
import app.pizzabutton.android.common.models.User
import app.pizzabutton.android.wearos.R
import app.pizzabutton.android.wearos.databinding.FragmentCallBinding

private val TAG = CallFragment::class.java.simpleName

class CallFragment : Fragment() {
    private val args: CallFragmentArgs by navArgs()
    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    private lateinit var user: User
    private lateinit var store: Store

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = args.user
        store = args.store

        // TODO: Start call and show transcript
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}