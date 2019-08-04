# quicktalk
Android TTS wrapper

[![](https://jitpack.io/v/s6joui/quicktalk.svg)](https://jitpack.io/#s6joui/quicktalk)

## Setup
Step 1. Add the JitPack repository to your build file. Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
  repositories {
    //...
    maven { url 'https://jitpack.io' }
  }
}
```
Step 2. Add the dependency

```groovy
implementation 'com.github.s6joui:quicktalk:1.0.1-alpha'
```
## Usage
```kotlin
val qt = QuickTalk(this,this)
qt.play("Hello World!")
```
