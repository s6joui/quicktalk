package tech.joeyck.quicktalk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.IntentFilter
import android.util.Log
import tech.joeyck.quicktalk.QuickTalkService.Companion.QT_SERVICE_ACTION_RESET
import tech.joeyck.quicktalk.QuickTalkService.Companion.QT_SERVICE_MSG_END_PHRASE
import tech.joeyck.quicktalk.QuickTalkService.Companion.QT_SERVICE_MSG_SETUP_COMPLETE
import tech.joeyck.quicktalk.QuickTalkService.Companion.QT_SERVICE_MSG_SETUP_ERROR
import tech.joeyck.quicktalk.QuickTalkService.Companion.QT_SERVICE_MSG_START_PHRASE
import tech.joeyck.quicktalk.QuickTalkService.Companion.QT_SERVICE_MSG_START_TALKING
import tech.joeyck.quicktalk.QuickTalkService.Companion.QT_SERVICE_MSG_STOP_TALKING

class QuickTalk(val context: Context, val listener: QuickTalkListener?): QuickTalkInterface {

    companion object{
        const val TAG = "QuickTalk"
    }

    val receiver: BroadcastReceiver  = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) { return }
            Log.i(TAG,intent.action)
            when(intent.action) {
                QT_SERVICE_MSG_SETUP_ERROR -> listener?.onSetupError()
                QT_SERVICE_MSG_SETUP_COMPLETE -> listener?.onSetupComplete()
                QT_SERVICE_MSG_START_TALKING -> listener?.onStartTalking()
                QT_SERVICE_MSG_STOP_TALKING -> listener?.onStopTalking()
                QT_SERVICE_MSG_START_PHRASE -> listener?.onStartPhrase()
                QT_SERVICE_MSG_END_PHRASE -> listener?.onFinishPhrase(0)
            }
        }
    }

    fun register() {
        val filter = IntentFilter()
        filter.addAction(QT_SERVICE_MSG_SETUP_COMPLETE)
        filter.addAction(QT_SERVICE_MSG_SETUP_ERROR)
        filter.addAction(QT_SERVICE_MSG_START_TALKING)
        filter.addAction(QT_SERVICE_MSG_STOP_TALKING)
        filter.addAction(QT_SERVICE_MSG_START_PHRASE)
        filter.addAction(QT_SERVICE_MSG_END_PHRASE)
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver,filter)
    }

    fun unregister() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
    }

    private fun reset(){
        val intent = Intent(QT_SERVICE_ACTION_RESET)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun play(text: String){
        play(arrayOf(text))
    }

    override fun play(text: Array<String>) {
        reset()
        val intent = Intent()
        intent.putExtra(QuickTalkService.QT_SERVICE_PARAM_SPEECH_TEXT_ARRAY,text)
        QuickTalkService.enqueueWork(context, intent)
    }

    override fun exportAudio(text: String, fileName: String) {
        reset()
        val intent = Intent()
        intent.putExtra(QuickTalkService.QT_SERVICE_PARAM_SPEECH_TEXT_ARRAY, arrayOf(text))
        intent.putExtra(QuickTalkService.QT_SERVICE_PARAM_SYNTH,true)
        intent.putExtra(QuickTalkService.QT_SERVICE_PARAM_SYNTH_FILENAME,fileName)
        QuickTalkService.enqueueWork(context, intent)
    }

    override fun getAvailableLanguages() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAvailableVoices() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}