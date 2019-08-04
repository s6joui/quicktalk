package tech.joeyck.quicktalk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.File
import java.util.*

class QuickTalkService : JobIntentService() {

    companion object {
        const val TAG = "QuickTalkService"
        const val AUDIO_EXPORT_UTTERANCE_ID = "qt_audio_export_uid"
        const val SPEECH_UTTERANCE_ID = "qt_speech_uid"
        const val QT_SERVICE_PARAM_SPEECH_TEXT_ARRAY = "speech_text_array"
        const val QT_SERVICE_PARAM_SYNTH_FILENAME = "synth_filename"
        const val QT_SERVICE_PARAM_SYNTH = "synth_mode"
        const val QT_SERVICE_ACTION_RESET = "action_reset"
        const val QT_SERVICE_MSG_SETUP_ERROR = "setup_error"
        const val QT_SERVICE_MSG_SETUP_COMPLETE = "setup_complete"
        const val QT_SERVICE_MSG_PLAYBACK_ERROR = "playback_error"
        const val QT_SERVICE_MSG_START_TALKING = "start_talking"
        const val QT_SERVICE_MSG_STOP_TALKING = "stop_talking"
        const val QT_SERVICE_MSG_START_PHRASE = "start_phrase"
        const val QT_SERVICE_MSG_END_PHRASE = "end_phrase"
        const val QT_SERVICE_MSG_AUDIO_EXPORT_COMPLETE = "audio_export_complete"
        const val QT_SERVICE_MSG_AUDIO_EXPORT_START = "audio_export_start"
        const val QT_SERVICE_MSG_AUDIO_EXPORT_PROGRESS = "audio_export_progress"
        const val QT_SERVICE_MSG_PARAM = "msg_param"

        private const val JOB_ID = 2

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, QuickTalkService::class.java, JOB_ID, intent)
        }
    }

    private var tts: TextToSpeech? = null
    private var uIds: Stack<String> = Stack()
    private var currentPhrase: Int = 0
    private var firstPhrase = true
    private var inputText: Array<String> = emptyArray()
    private var synthMode = false
    private var exportFileName = "quicktalk_default_filename"

    private var isSafeToDestroy = false

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service onCreate")
        tts = TextToSpeech(applicationContext) { status -> startTts(status) }

        val filter = IntentFilter()
        filter.addAction(QT_SERVICE_ACTION_RESET)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver,filter)
    }

    override fun onHandleWork(intent: Intent) {
        inputText = intent.getStringArrayExtra(QT_SERVICE_PARAM_SPEECH_TEXT_ARRAY)
        synthMode = intent.getBooleanExtra(QT_SERVICE_PARAM_SYNTH, false)
        if (intent.hasExtra(QT_SERVICE_PARAM_SYNTH_FILENAME)) {
            exportFileName = intent.getStringExtra(QT_SERVICE_PARAM_SYNTH_FILENAME)
        }
    }

    private fun startTts(status: Int) {
        Log.i(TAG,"Init code: $status")
        if(status == TextToSpeech.ERROR){
            respond(QT_SERVICE_MSG_SETUP_ERROR)
            return
        }
        respond(QT_SERVICE_MSG_SETUP_COMPLETE)
        setup()
        if (!synthMode) {
            inputText.forEach { speak(it) }
        } else {
            exportAudio(inputText.joinToString(), exportFileName)
        }
    }

    private fun speak(text: String){
        Log.i(TAG,"Queuing phrase $currentPhrase: $text")
        currentPhrase++
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val utteranceId = SPEECH_UTTERANCE_ID + currentPhrase
            uIds.push(utteranceId)
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
        }else{
            tts?.speak(text, TextToSpeech.QUEUE_ADD, null)
        }
    }

    private fun exportAudio(text: String, fileName: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            val file = File(fileName)
            tts?.synthesizeToFile(text,null,file, AUDIO_EXPORT_UTTERANCE_ID)
        }else{
            val params = HashMap<String,String>()
            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = AUDIO_EXPORT_UTTERANCE_ID
            tts?.synthesizeToFile(text,params,fileName)
        }
    }

    private fun setup() {
        tts?.setOnUtteranceProgressListener(object: UtteranceProgressListener(){
            override fun onDone(uId: String?) {
                uIds.remove(uId)
                if(uId == AUDIO_EXPORT_UTTERANCE_ID){
                    if(uIds.isEmpty()){
                        respond(QT_SERVICE_MSG_AUDIO_EXPORT_COMPLETE)
                        isSafeToDestroy = true
                    }else{
                        respond(QT_SERVICE_MSG_AUDIO_EXPORT_PROGRESS, uIds.size)
                    }
                }else{
                    if(uIds.isEmpty()){
                        respond(QT_SERVICE_MSG_STOP_TALKING)
                    }else{
                        respond(QT_SERVICE_MSG_END_PHRASE)
                    }
                }
            }

            override fun onError(uId: String?) {
                respond(QT_SERVICE_MSG_PLAYBACK_ERROR, -1)
                isSafeToDestroy = true
            }

            override fun onStart(uId: String?) {
                if(uId == AUDIO_EXPORT_UTTERANCE_ID) {
                    respond(QT_SERVICE_MSG_AUDIO_EXPORT_START)
                }else{
                    if(firstPhrase){
                        firstPhrase = false
                        respond(QT_SERVICE_MSG_START_TALKING)
                    }
                    respond(QT_SERVICE_MSG_START_PHRASE)
                }
            }

            override fun onError(uId: String?, errorCode: Int) {
                respond(QT_SERVICE_MSG_PLAYBACK_ERROR, errorCode)
                isSafeToDestroy = true
            }

            override fun onStop(uId: String?, interrupted: Boolean) {
                super.onStop(uId, interrupted)
                respond(QT_SERVICE_MSG_STOP_TALKING)
                isSafeToDestroy = true
            }

        })
    }

    private fun reset(){
        firstPhrase = true
        currentPhrase = 0
        inputText = emptyArray()
        uIds = Stack()
        tts?.stop()
    }

    val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) { return }
            Log.i(TAG,intent.action)
            when(intent.action) {
                QT_SERVICE_ACTION_RESET -> reset()
            }
        }
    }

    private fun respond(message: String, param: Int? = null) {
        val intent = Intent(message)
        if (param != null) {
            intent.putExtra(QT_SERVICE_MSG_PARAM,param)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    override fun onDestroy() {
        if (isSafeToDestroy) {
            if (tts != null) {
                tts!!.shutdown()
            }
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)
            super.onDestroy()
        }
    }

}