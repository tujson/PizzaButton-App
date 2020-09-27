package app.pizzabutton.android.phone.fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import app.pizzabutton.android.phone.adapters.OrderAdapter
import app.pizzabutton.android.phone.databinding.FragmentHomeBinding
import app.pizzabutton.android.common.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

private val TAG = HomeFragment::class.java.simpleName

class HomeFragment : Fragment() {
    private val args: HomeFragmentArgs by navArgs()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var user: User
    private lateinit var adapter: OrderAdapter

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
        user.orderHistory.sortBy { it.timePlaced }

        val defaultPizza = SpannableString(user.defaultPizza).apply {
            setSpan(UnderlineSpan(), 0, this.length, 0)
        }
        binding.tvDefaultPizza.text = defaultPizza

        val address = SpannableString(user.address).apply {
            setSpan(UnderlineSpan(), 0, this.length, 0)
        }
        binding.tvAddress.text = address

        binding.btnOrder.setOnClickListener {
            submitOrder()
        }

        adapter = OrderAdapter(user.orderHistory)
        binding.rvOrderHistory.adapter = adapter

        Firebase.database.reference.child(user.id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue<User>()?.let {
                        updateUserView(it)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        TAG,
                        "onCancelled while listening to Firebase Realtime Database",
                        error.toException()
                    )
                }
            })
    }

    private fun submitOrder() {
        TODO()
    }

    private fun updateUserView(updatedUser: User) {
        user.orderHistory.clear()
        user.orderHistory.addAll(updatedUser.orderHistory)
        user.orderHistory.sortByDescending { it.timePlaced }

        user.name = updatedUser.name
        user.address = updatedUser.address
        user.phoneNumber = updatedUser.phoneNumber
        user.defaultPizza = updatedUser.defaultPizza

        activity?.runOnUiThread {
            binding.tvDefaultPizza.text = SpannableString(user.defaultPizza).apply {
                setSpan(UnderlineSpan(), 0, this.length, 0)
            }
            binding.tvAddress.text = SpannableString(user.address).apply {
                setSpan(UnderlineSpan(), 0, this.length, 0)
            }

            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}