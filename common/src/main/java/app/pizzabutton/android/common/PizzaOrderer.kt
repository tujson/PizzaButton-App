package app.pizzabutton.android.common

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import app.pizzabutton.android.common.models.Store
import app.pizzabutton.android.common.models.User
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.*

private val TAG = PizzaOrderer::class.java.simpleName
private const val TWILIO_API_URL = BuildConfig.TWILIO_API_URL

class PizzaOrderer(private val user: User, private val store: Store) {

    var activeCall: Call? = null
    private var fileAndMicAudioDevice: FileAndMicAudioDevice? = null
    private var tts: TextToSpeech? = null


    fun orderPizza(context: Context) {
        // TODO: Call store.
        val script = generateScript(user)

        generateSpeech(script, context)
        val accessToken = getTwilioAccessToken()

        val params = hashMapOf<String, String>()
//        params["to"] = store.phoneNumber THIS WILL CALL THE PIZZA STORE!!!
        params["to"] = BuildConfig.TEST_PHONE_NUMBER
        // !!!!! DO NOT DELETE UNLESS YOU WANT TO CALL PIZZA STORE. EXTRA CHECK.
        if (params.keys.contains(store.phoneNumber)) {
            Log.e(TAG, "Attempting to call pizza store's real phone number.")
            return
        }
        // !!!!! DO NOT DELETE UNLESS YOU WANT TO CALL PIZZA STORE. EXTRA CHECK.

        fileAndMicAudioDevice = FileAndMicAudioDevice(context.applicationContext).also {
            Voice.setAudioDevice(it)
        }

        val connectOptions = ConnectOptions.Builder(accessToken)
            .params(params)
            .build()
        activeCall = Voice.connect(context, connectOptions, object : Call.Listener {
            override fun onConnectFailure(call: Call, callException: CallException) {
                Log.v(TAG, "Call connect failed")
            }

            override fun onRinging(call: Call) {
                Log.v(TAG, "Call ringing")
            }

            override fun onConnected(call: Call) {
                TODO("Not yet implemented")
            }

            override fun onReconnecting(call: Call, callException: CallException) {
                Log.v(TAG, "Call reconnecting")
            }

            override fun onReconnected(call: Call) {
                Log.v(TAG, "Call reconnected")
            }

            override fun onDisconnected(call: Call, callException: CallException?) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun getTwilioAccessToken(): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(TWILIO_API_URL)
            .build()

        client.newCall(request).execute().use { response -> return response.body!!.string() }
    }

    private fun generateScript(user: User): String {
        val processedAddress = user.address.toLowerCase(Locale.getDefault()).replace("st", "street")
        val processedPhone = user.phoneNumber.replace("", " ").trim()
        return "Hi. I would like to order delivery to $processedAddress. " +
                "My phone number is $processedPhone. " +
                "I would like a ${user.defaultPizza} pizza. " +
                "I will pay using cash. Thanks!"
    }

    private fun generateSpeech(script: String, context: Context) {
        tts = TextToSpeech(context) {
            tts?.language = Locale.US
            val filename = "${System.currentTimeMillis()}-${user.name}.mp3"
            val file = File(context.filesDir, filename)
            tts?.synthesizeToFile(script, null, file, filename)
            Log.v(TAG, "Finished TTS synthesis. Wrote to $filename.")
        }
    }
}
