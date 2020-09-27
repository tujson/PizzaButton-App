package app.pizzabutton.android.wearos.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import app.pizzabutton.android.common.models.User
import app.pizzabutton.android.wearos.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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

        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        Log.v(TAG, "ID: ${account?.id}")
        Log.v(TAG, "Display name: ${account?.displayName}")
        Log.v(TAG, "Email: ${account?.email}")
        Log.v(TAG, "Family name: ${account?.familyName}")
        Log.v(TAG, "Given name: ${account?.givenName}")
        Log.v(TAG, "Account name: ${account?.account?.name}")
        Log.v(TAG, "Account type: ${account?.account?.type}")

        GoogleSignIn.getLastSignedInAccount(requireContext())?.id?.let {
            retrieveUser(it)
        } ?: run {
            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToSignInFragment())
        }

        // Temporary sign out method
        view.findViewById<ImageView>(R.id.ivAppIcon).setOnClickListener {
            val googleSignInOptions = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestProfile()
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)
            googleSignInClient.signOut()
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
                    } ?: run {
                        Log.e(TAG, "Unregistered id: $id")
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