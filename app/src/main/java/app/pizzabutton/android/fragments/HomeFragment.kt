package app.pizzabutton.android.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import app.pizzabutton.android.R
import app.pizzabutton.android.models.User

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val args: HomeFragmentArgs by navArgs()

    private lateinit var user: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = args.user
    }
}