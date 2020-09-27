package app.pizzabutton.android.wearos.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import app.pizzabutton.android.common.models.User
import app.pizzabutton.android.wearos.R
import app.pizzabutton.android.wearos.databinding.FragmentHomeBinding

private val TAG = HomeFragment::class.java.simpleName

class HomeFragment : Fragment() {
    private val args: HomeFragmentArgs by navArgs()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = args.user

        // TODO: Show button. On click, submit order.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}