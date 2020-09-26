package app.pizzabutton.android.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import app.pizzabutton.android.R
import app.pizzabutton.android.models.User
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
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

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            showSignIn()
        } else {
            checkUserRegistration(firebaseUser.uid)
        }
    }

    private fun checkUserRegistration(firebaseUserUid: String) {
        Firebase.database.reference
            .child(firebaseUserUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<User>()?.let {
                        // TODO: Navigate to HomeFragment
                    } ?: run {
                        // TODO: Navigate to RegisterFragment
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

    private fun showSignIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        val signInActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()

        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                FirebaseAuth.getInstance().currentUser?.let {
                    checkUserRegistration(it.uid)
                } ?: run {
                    Log.e(
                        TAG,
                        "User successfully logged in but is null. Potential error with Firebase Auth."
                    )
                }
            } else {
                Log.e(TAG, "Failed to sign in.")
                /// TODO: Show error message
            }
        }

        signInActivityResultLauncher.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
        )
    }
}