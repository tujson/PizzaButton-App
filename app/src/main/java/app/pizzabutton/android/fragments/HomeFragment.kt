package app.pizzabutton.android.fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import app.pizzabutton.android.R
import app.pizzabutton.android.databinding.FragmentHomeBinding
import app.pizzabutton.android.models.User

class HomeFragment : Fragment() {
    private val args: HomeFragmentArgs by navArgs()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = args.user

        val defaultPizza = SpannableString(user.defaultPizza).apply {
            setSpan(UnderlineSpan(), 0, this.length, 0)
        }
        binding.tvDefaultPizza.text = defaultPizza

        val address = SpannableString(user.address).apply {
            setSpan(UnderlineSpan(), 0, this.length, 0)
        }
        binding.tvAddress.text = address
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}