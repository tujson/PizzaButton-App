package app.pizzabutton.android.phone.fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import app.pizzabutton.android.common.PizzaFinder
import app.pizzabutton.android.common.PizzaOrderer
import app.pizzabutton.android.common.models.User
import app.pizzabutton.android.phone.R
import app.pizzabutton.android.phone.adapters.OrderAdapter
import app.pizzabutton.android.phone.databinding.FragmentHomeBinding
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

    private var isVolumeOn = false
    private var isMicOn = false

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

        binding.ibVolume.setOnClickListener {
            if (isVolumeOn) {
                binding.ibVolume.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.red_light)
                binding.ibVolume.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.volume_off
                    )
                )

                isVolumeOn = false
            } else {
                binding.ibVolume.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSecondary)
                binding.ibVolume.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.volume_on
                    )
                )

                isVolumeOn = true
            }
        }

        binding.ibMic.setOnClickListener {
            if (isMicOn) {
                binding.ibMic.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.red_light)
                binding.ibMic.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.mic_off
                    )
                )
                
                isMicOn = false
            } else {
                binding.ibMic.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorSecondary)
                binding.ibMic.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.mic_on
                    )
                )

                isMicOn = true
            }
        }
    }

    private fun submitOrder() {
        val pizzaFinder = PizzaFinder(requireContext().applicationContext)
        pizzaFinder.getNearestPizza(user.address) { closestStore ->
            Log.v(TAG, "Closest pizza store: $closestStore")
            val pizzaOrderer = PizzaOrderer(user, closestStore!!)
            pizzaOrderer.orderPizza(requireContext())
        }
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