package tech.joeyck.quicktalk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), QuickTalkListener {

    val qt = QuickTalk(this,this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            val text = editText.text.toString()
            qt.play(text)
        }

        exportBtn.setOnClickListener {
            val text = editText.text.toString()
            qt.exportAudio(text,"talk_file")
        }
    }

    override fun onResume() {
        super.onResume()
        qt.register()
    }

    override fun onPause() {
        super.onPause()
        qt.unregister()
    }

    override fun onStartTalking() {
        Log.i("QuickTalk","===== onStartTalking =====")
    }

    override fun onStopTalking() {
        Log.i("QuickTalk","===== onStopTalking =====")
    }

    override fun onFinishPhrase(progress: Int) {
        Log.i("QuickTalk","onFinishPhrase $progress")
    }

    override fun onPlaybackError(error: Int) {
        Log.i("QuickTalk","onPlaybackError $error")
    }

    override fun onAudioExportStart() {
        Log.i("QuickTalk","onAudioExportStart")
    }

    override fun onAudioExportProgress(progress: Int) {
        Log.i("QuickTalk","onAudioExportProgress $progress")
    }

    override fun onAudioExported() {
        Log.i("QuickTalk","onAudioExported")
    }

    override fun onSetupComplete() {
        Log.i("QuickTalk","onSetupComplete")
    }

    override fun onSetupError() {
        Log.i("QuickTalk","onSetupError")
    }

    override fun onStartPhrase() {
        Log.i("QuickTalk","onStartPhrase")
    }

}
