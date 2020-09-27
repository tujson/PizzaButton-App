package app.pizzabutton.android.wearos.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import app.pizzabutton.android.common.models.User
import app.pizzabutton.android.wearos.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

private val TAG = SplashFragment::class.java.simpleName

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GoogleSignIn.getLastSignedInAccount(requireContext())?.id?.let {
            retrieveUser(it)
        } ?: run {
            view.findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
        }
    }

    private fun retrieveUser(id: String) {
        Firebase.database.reference
            .child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<User>()?.let {
                        view?.findNavController()?.navigate(
                            SplashFragmentDirections.actionSplashFragmentToHomeFragment(
                                it
                            )
                        )
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

}