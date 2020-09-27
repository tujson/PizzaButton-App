package app.pizzabutton.android.phone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.pizzabutton.android.phone.databinding.FragmentRegisterBinding
import app.pizzabutton.android.phone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseUser =
            FirebaseAuth.getInstance().currentUser!! ?: error("Firebase user signed out.")

        binding.etName.setText(firebaseUser.displayName)

        binding.btnRegister.setOnClickListener {
            val user = User(
                firebaseUser.uid,
                binding.etName.text.toString(),
                binding.etAddress.text.toString(),
                binding.etPhoneNumber.text.toString(),
                binding.etDefaultPizza.text.toString(),
            )

            registerUser(user)
        }
    }

    private fun registerUser(user: User) {
        Firebase.database.reference.child(user.id).setValue(user)

        findNavController().navigate(
            RegisterFragmentDirections.actionRegisterFragmentToHomeFragment(
                user
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}