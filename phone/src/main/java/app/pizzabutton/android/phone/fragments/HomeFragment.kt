package app.pizzabutton.android.phone.fragments

import android.graphics.Paint
import android.os.Bundle
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
import app.pizzabutton.android.phone.BuildConfig
import app.pizzabutton.android.phone.R
import app.pizzabutton.android.phone.viewmodels.UserViewModelFactory
import app.pizzabutton.android.phone.adapters.OrderAdapter
import app.pizzabutton.android.phone.databinding.FragmentHomeBinding
import app.pizzabutton.android.phone.viewmodels.UserViewModel
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import java.util.*
import kotlin.concurrent.thread

private val TAG = HomeFragment::class.java.simpleName

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userViewModel: UserViewModel
    private lateinit var userViewModelFactory: UserViewModelFactory

    private var speechRecognizer: SpeechRecognizer? = null
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

        startSpeechRecognition()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            tvDefaultPizza.paintFlags =
                tvDefaultPizza.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            tvAddress.paintFlags =
                tvAddress.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            btnOrder.setOnClickListener {
                submitOrder()
            }

            ibVolume.setOnClickListener {
                isVolumeOn = !isVolumeOn

                ibVolume.backgroundTintList =
                    ContextCompat.getColorStateList(
                        requireContext(),
                        if (isVolumeOn) {
                            R.color.colorSecondary
                        } else {
                            R.color.red_light
                        }
                    )

                ibVolume.setImageDrawable(
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

            ibMic.setOnClickListener {
                isMicOn = !isMicOn
                pizzaOrderer?.toggleMic(isMicOn)

                ibMic.backgroundTintList =
                    ContextCompat.getColorStateList(
                        requireContext(),
                        if (isVolumeOn) {
                            R.color.colorSecondary
                        } else {
                            R.color.red_light
                        }
                    )

                ibMic.setImageDrawable(
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
                        speechRecognizer?.startContinuousRecognitionAsync()

                    }

                    override fun onDisconnected() {
                        binding.cardActiveOrder.visibility = View.GONE
                        speechRecognizer?.stopContinuousRecognitionAsync()
                    }
                })

            thread {
                pizzaOrderer?.orderPizza(requireContext())
            }
        }
    }

    private fun subscribeUi(adapter: OrderAdapter) {
        userViewModel.userLiveData.observe(viewLifecycleOwner) { newUser ->
            binding.tvDefaultPizza.text = newUser.defaultPizza
            binding.tvAddress.text = newUser.address

            adapter.submitList(newUser.orderHistory)
        }
    }

    private fun startSpeechRecognition() {
        val speechConfig = SpeechConfig.fromSubscription(
            BuildConfig.AZURE_SUBSCRIPTION_KEY,
            BuildConfig.AZURE_SERVICE_REGION
        )
        speechRecognizer = SpeechRecognizer(speechConfig)
        speechRecognizer?.recognizing?.addEventListener { _, speechRecognitionEventArgs ->
            Log.v(TAG, "Recognizing: ${speechRecognitionEventArgs.result.text}")
            requireActivity().runOnUiThread {
                binding.tvCallTranscript.text = speechRecognitionEventArgs.result.text
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        speechRecognizer?.stopContinuousRecognitionAsync()
    }
}