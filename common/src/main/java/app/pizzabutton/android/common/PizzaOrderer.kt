package app.pizzabutton.android.common

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import app.pizzabutton.android.common.models.Store
import app.pizzabutton.android.common.models.User
import com.twilio.voice.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.concurrent.thread

private val TAG = PizzaOrderer::class.java.simpleName
private const val TWILIO_API_URL = BuildConfig.TWILIO_API_URL

class PizzaOrderer(
    private val user: User,
    private val store: Store,
    private val pizzaCallCallback: PizzaCallInterface
) {

    var activeCall: Call? = null
    private var fileAndMicAudioDevice: FileAndMicAudioDevice? = null
    private var tts: TextToSpeech? = null
    private var ttsFile: File? = null
    private lateinit var audioManager: AudioManager
    private var savedAudioMode = AudioManager.MODE_INVALID

    fun orderPizza(context: Context) {
        val script =
            generateScript(user).repeat(6) // There's a demo restriction that starts file playback earlier than expected.

        generateSpeech(script, context) {
            thread { callPizzaStore(ttsFile!!, context) }
        }
    }

    private fun callPizzaStore(ttsFile: File, context: Context) {
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

        fileAndMicAudioDevice =
            FileAndMicAudioDevice(context, ttsFile)
        Voice.setAudioDevice(fileAndMicAudioDevice!!)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = true

        val connectOptions = ConnectOptions.Builder(accessToken)
            .params(params)
            .build()
        activeCall = Voice.connect(context, connectOptions, object : Call.Listener {
            override fun onConnectFailure(call: Call, callException: CallException) {
                setAudioFocus(false)
                pizzaCallCallback.onDisconnected()
                Log.e(TAG, "Call connect failed: ${callException.errorCode}", callException)
            }

            override fun onRinging(call: Call) {
                Log.v(TAG, "Call ringing")
            }

            override fun onConnected(call: Call) {
                setAudioFocus(true)
                fileAndMicAudioDevice?.switchInput(true)
                pizzaCallCallback.onConnected()
                Log.v(TAG, "Connected")
            }

            override fun onReconnecting(call: Call, callException: CallException) {
                Log.e(TAG, "Call reconnecting", callException)
            }

            override fun onReconnected(call: Call) {
                Log.v(TAG, "Call reconnected")
            }

            override fun onDisconnected(call: Call, callException: CallException?) {
                setAudioFocus(false)
                Log.e(TAG, "Disconnected", callException)
                pizzaCallCallback.onDisconnected()
            }
        })
    }

    fun toggleMic(isMicOn: Boolean) {
        fileAndMicAudioDevice?.switchInput(!isMicOn)
    }

    private fun setAudioFocus(setFocus: Boolean) {
        if (setFocus) {
            savedAudioMode = audioManager.getMode()
            // Request audio focus before making any device switch.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val focusRequest =
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(playbackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener { i: Int -> }
                        .build()
                audioManager.requestAudioFocus(focusRequest)
            } else {
                audioManager.requestAudioFocus(
                    AudioManager.OnAudioFocusChangeListener { focusChange: Int -> },
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            }
            /*
             * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
             * required to be in this mode when playout and/or recording starts for
             * best possible VoIP performance. Some devices have difficulties with speaker mode
             * if this is not set.
             */audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        } else {
            audioManager.mode = savedAudioMode
            audioManager.abandonAudioFocus(null)
        }
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

    private fun generateSpeech(
        script: String,
        context: Context,
        onFinishSynthesis: () -> Unit
    ) {
        tts = TextToSpeech(context) {
            tts?.language = Locale.US
            val filename = "${System.currentTimeMillis()}-${user.name}.wav"
            ttsFile = File(context.filesDir, filename)
            Log.v(TAG, "Started synthesis to $filename.")
            tts?.synthesizeToFile(script, null, ttsFile, filename)
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.v(TAG, "Start TTS Synthesis: $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                Log.v(TAG, "Done TTS Synthesis: $utteranceId")

                tts?.shutdown()
                onFinishSynthesis()
            }

            override fun onError(utteranceId: String?) {
                Log.e(TAG, "TTS Synthesis error: $utteranceId")
            }
        })
    }
}


interface PizzaCallInterface {
    fun onConnected()
    fun onDisconnected()
}