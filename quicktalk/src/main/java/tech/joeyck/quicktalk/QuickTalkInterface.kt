package tech.joeyck.quicktalk

interface QuickTalkInterface {

    fun play(text: String)

    fun play(text: Array<String>)

    fun exportAudio(text: String,fileName: String)

    fun getAvailableLanguages()

    fun getAvailableVoices()

}