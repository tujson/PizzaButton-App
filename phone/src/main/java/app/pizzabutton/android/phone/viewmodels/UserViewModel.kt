package app.pizzabutton.android.phone.viewmodels

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import app.pizzabutton.android.common.models.User
import app.pizzabutton.android.phone.utils.FirebaseQueryLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class UserViewModel(userId: String) : ViewModel() {
    private val liveData = FirebaseQueryLiveData(
        Firebase.database.reference.child(userId)
    )

    val userLiveData = Transformations.map(liveData) {
        it.getValue<User>()!!
    }
}