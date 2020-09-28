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
import androidx.lifecycle.ViewModelProvider
import app.pizzabutton.android.common.PizzaCallInterface
import app.pizzabutton.android.common.PizzaFinder
import app.pizzabutton.android.common.PizzaOrderer
import app.pizzabutton.android.phone.R
import app.pizzabutton.android.phone.UserViewModelFactory
import app.pizzabutton.android.phone.adapters.OrderAdapter
import app.pizzabutton.android.phone.databinding.FragmentHomeBinding
import app.pizzabutton.android.phone.viewmodels.UserViewModel
import kotlin.concurrent.thread

private val TAG = HomeFragment::class.java.simpleName

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel
    private lateinit var userViewModelFactory: UserViewModelFactory

    private var pizzaOrderer: PizzaOrderer? = null
    private var isVolumeOn = false
    private var isMicOn = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        userViewModelFactory =
            UserViewModelFactory(HomeFragmentArgs.fromBundle(requireArguments()).user.id)
        userViewModel = ViewModelProvider(this, userViewModelFactory).get(UserViewModel::class.java)

        val adapter = OrderAdapter()
        binding.rvOrderHistory.adapter = adapter
        subscribeUi(adapter)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOrder.setOnClickListener {
            submitOrder()
        }

        binding.ibVolume.setOnClickListener {
            isVolumeOn = !isVolumeOn

            binding.ibVolume.backgroundTintList =
                ContextCompat.getColorStateList(
                    requireContext(),
                    if (isVolumeOn) {
                        R.color.colorSecondary
                    } else {
                        R.color.red_light
                    }
                )

            binding.ibVolume.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (isVolumeOn) {
                        R.drawable.volume_on
                    } else {
                        R.drawable.volume_off
                    }
                )
            )
        }

        binding.ibMic.setOnClickListener {
            isMicOn = !isMicOn
            pizzaOrderer?.toggleMic(isMicOn)

            binding.ibMic.backgroundTintList =
                ContextCompat.getColorStateList(
                    requireContext(),
                    if (isVolumeOn) {
                        R.color.colorSecondary
                    } else {
                        R.color.red_light
                    }
                )

            binding.ibMic.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (isMicOn) {
                        R.drawable.mic_on
                    } else {
                        R.drawable.mic_off
                    }
                )
            )
        }
    }

    // TODO: Clean up using coroutines
    private fun submitOrder() {
        val pizzaFinder = PizzaFinder(requireContext().applicationContext)
        pizzaFinder.getNearestPizza(userViewModel.userLiveData.value!!.address) { closestStore ->
            Log.v(TAG, "Closest pizza store: $closestStore")
            pizzaOrderer = PizzaOrderer(
                userViewModel.userLiveData.value!!,
                closestStore!!,
                object : PizzaCallInterface {
                    override fun onConnected() {
                        binding.cardActiveOrder.visibility = View.VISIBLE
                    }

                    override fun onDisconnected() {
                        binding.cardActiveOrder.visibility = View.GONE
                    }
                })

            thread {
                pizzaOrderer?.orderPizza(requireContext())
            }
        }
    }

    private fun subscribeUi(adapter: OrderAdapter) {
        userViewModel.userLiveData.observe(viewLifecycleOwner) { newUser ->
            val defaultPizza = SpannableString(newUser.defaultPizza).apply {
                setSpan(UnderlineSpan(), 0, this.length, 0)
            }
            binding.tvDefaultPizza.text = defaultPizza

            val address = SpannableString(newUser.address).apply {
                setSpan(UnderlineSpan(), 0, this.length, 0)
            }
            binding.tvAddress.text = address

            adapter.submitList(newUser.orderHistory)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}