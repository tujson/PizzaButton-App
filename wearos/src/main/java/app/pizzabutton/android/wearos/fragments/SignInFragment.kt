package app.pizzabutton.android.wearos.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import app.pizzabutton.android.common.models.User
import app.pizzabutton.android.wearos.R
import app.pizzabutton.android.wearos.databinding.FragmentSignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

private val TAG = SignInFragment::class.java.simpleName

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleSignInActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignIn.children.forEach {
            if (it is TextView) {
                it.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }

        binding.btnSignIn.setOnClickListener {
            signIn()
        }

        googleSignInActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
                task.getResult(ApiException::class.java)?.id?.let {
                    checkUserRegistration(it)
                } ?: run {
                    Log.e(TAG, "Failed to retrieve data from GoogleSignIn")
                }
            } else {
                Log.e(TAG, "Result code not OK")
            }
        }
    }

    private fun signIn() {
        val googleSignInOptions = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestProfile()
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

        googleSignInActivityResultLauncher.launch(
            googleSignInClient.signInIntent
        )
    }

    private fun checkUserRegistration(firebaseUserUid: String) {
        Firebase.database.reference
            .child(firebaseUserUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<User>()?.let {
                        Log.v(TAG, "User id: ${it.id}")
                        findNavController().navigate(
                            SignInFragmentDirections.actionSignInFragmentToHomeFragment(it)
                        )
                    } ?: run {
                        Log.v(TAG, "Unregistered id: $firebaseUserUid")
                        TODO("Tell user to register on phone")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        TAG,
                        "Failed to retrieve user from Firebase Realtime Database",
                        error.toException()
                    )
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}