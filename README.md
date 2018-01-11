# Opus Wrapper for Android
This is a very simple and straight-forward Android application that records audio and playback it using the [Opus Interactive Audio Codec][1].

### Disclaimer

This repository contains a sample code intended to demonstrate the capabilities of the [Opus Interactive Audio Codec][1]. The current version is not intended to be used as-is in applications as a library dependency, and will not be maintained as such. Bug fix contributions are welcome, but issues and feature requests will not be addressed.

## Summary

This is an Android library transplanted from official [Opus codec][1]. With this library, Opus format audio can be operated in an easy way. Application level function includes audio record, playback, encode and decode.

### OpusLib codes

This project is based on Opus source code, Opus-tools, and Opusfile. Opus-tools provides command-line utilities to encode, inspect, and decode .opus files. Opusfile provides application developers with a high-level API for decoding and seeking in .opus files.
The java implements an OpusService, this is the highest level interface. It's a background Server running automatically. All you need to do is sending Intents to it, and receiving the feedback messages through a Broadcast Receiver. The approach is recommended over the Method 2.

#### Sending message.

Many static public method can be called directly. For details, please refer to the source code of OpusService.java
```
OpusService.play(Context context, String fileName);
OpusService.record(Context context, String fileName);
......
```
#### Receiving message.

A Broadcast Receiver is needed to receive the feedback messages while playing, recording or converting a opus file. Below is an example.

```
//register a broadcast receiver
receiver = new OpusReceiver();
IntentFilter filter = new IntentFilter();
filter.addAction(OpusEvent.ACTION_OPUS_UI_RECEIVER);
registerReceiver(receiver, filter);

//define a broadcast receiver
class OpusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            int type = bundle.getInt(OpusEvent.EVENT_TYPE, 0);
            switch (type) {
                case OpusEvent.CONVERT_FINISHED:
                    break;
                case OpusEvent.CONVERT_FAILED:
                    break;
                case OpusEvent.CONVERT_STARTED:
                    break;
                case OpusEvent.RECORD_FAILED:
                    break;
                case OpusEvent.RECORD_FINISHED:
                    break;
                case OpusEvent.RECORD_STARTED:
                    break;
                case OpusEvent.RECORD_PROGRESS_UPDATE:
                    break;
                case OpusEvent.PLAY_PROGRESS_UPDATE:
                    break;
                case OpusEvent.PLAY_GET_AUDIO_TRACK_INFO:
                    break;
                case OpusEvent.PLAYING_FAILED:
                    break;
                case OpusEvent.PLAYING_FINISHED:
                    break;
                case OpusEvent.PLAYING_PAUSED:
                    break;
                case OpusEvent.PLAYING_STARTED:
                    break;
                default:
                    Log.d(TAG, intent.toString() + "Invalid request,discarded");
                    break;
            }
        }
    }
```


### How to use the OpusLib codes
* Encode and Decode
```
OpusTool oTool = new OpusTool();
oTool.decode(fileName,fileNameOut, null);
oTool.encode(fileName, fileNameOut, null);
```
* Playback
```
OpusPlayer opusPlayer = OpusPlayer.getInstance();
opusPlayer.play(fileName);
opusPlayer.stop();
```
* Record
```
OpusRecorder opusRecorder = OpusRecorder.getInstance();
opusRecorder.startRecording(fileName);
opusRecorder.stopRecording();
```

### Pre-requisites

1. JDK v1.8 or higher  
2. SDK v2.2.1 or higher  
3. NDK r10d or higher (Note: remember to export NDK's path) 
4. Android Studio (with SDK) 1.2.1 or higher

## Credits

This project was based on a Java interface to OpenCV called **JavaCV**.

* [Opus][2]
* [Opus-tools][3]
* [Opusfile][4]


## License

The code supplied here is covered under the MIT Open Source License..

[1]: https://opus-codec.org
[2]: git://git.opus-codec.org/opus.git
[3]: git://git.xiph.org/opus-tools.git
[4]: git://git.xiph.org/opusfile.git


