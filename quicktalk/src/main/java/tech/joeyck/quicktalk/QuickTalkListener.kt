package tech.joeyck.quicktalk

interface QuickTalkListener {

    fun onStartTalking()

    fun onStopTalking()

    fun onFinishPhrase(progress: Int)

    fun onStartPhrase()

    fun onPlaybackError(error: Int)

    fun onAudioExportStart()

    fun onAudioExportProgress(progress: Int)

    fun onAudioExported()

    fun onSetupError()

    fun onSetupComplete()

}