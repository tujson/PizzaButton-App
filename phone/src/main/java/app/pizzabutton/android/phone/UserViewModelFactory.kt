package app.pizzabutton.android.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.pizzabutton.android.phone.viewmodels.UserViewModel

class UserViewModelFactory(private val userId: String) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}