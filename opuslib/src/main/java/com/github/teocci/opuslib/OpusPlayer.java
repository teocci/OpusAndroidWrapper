package com.github.teocci.opuslib;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.github.teocci.opuslib.utils.LogHelper;
import com.github.teocci.opuslib.utils.Utils;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.media.AudioFormat.ENCODING_PCM_16BIT;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Dec-26
 */
public class OpusPlayer
{
    private static final String TAG = LogHelper.makeLogTag(OpusPlayer.class);

    private static volatile OpusPlayer opusPlayer;

    private static final int STATE_NONE = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_PAUSED = 2;

    private volatile int state = STATE_NONE;

    private OpusTool opusTool = new OpusTool();
    private Lock libLock = new ReentrantLock();
    private AudioTrack audioTrack;

    private static final int minBufferSize = 1024 * 8 * 8;
    private int bufferSize = 0;
    private int channel = 0;

    private long lastNotificationTime = 0;
    private String currentFileName = "";

    private volatile Thread playTread = new Thread();
    private OpusEvent eventSender = null;

    private OpusPlayer() {}

    public static OpusPlayer getInstance()
    {
        if (opusPlayer == null)
            synchronized (OpusPlayer.class) {
                if (opusPlayer == null)
                    opusPlayer = new OpusPlayer();
            }
        return opusPlayer;
    }

    public void setEventSender(OpusEvent es)
    {
        eventSender = es;
    }

    public void play(String fileName)
    {
        // if already playing, stop current playback
        if (state != STATE_NONE) {
            stop();
        }

        state = STATE_NONE;
        currentFileName = fileName;

        if (!Utils.isFileExist(currentFileName) || opusTool.isOpusFile(currentFileName) == 0) {
            LogHelper.e(TAG, "File does not exist, or it is not an opus file!");
            if (eventSender != null)
                eventSender.sendEvent(OpusEvent.PLAYING_FAILED);
            return;
        }

        libLock.lock();
        int res = opusTool.openOpusFile(currentFileName);
        libLock.unlock();
        if (res == 0) {
            LogHelper.e(TAG, "Open opus file error!");
            if (eventSender != null)
                eventSender.sendEvent(OpusEvent.PLAYING_FAILED);
            return;
        }

        try {
            channel = opusTool.getChannelCount();
            int trackChannel;
            if (channel == 1)
                trackChannel = AudioFormat.CHANNEL_OUT_MONO;
            else
                trackChannel = AudioFormat.CHANNEL_OUT_STEREO;

            bufferSize = AudioTrack.getMinBufferSize(
                    48000,
                    trackChannel,
                    ENCODING_PCM_16BIT
            );

            bufferSize = (bufferSize > minBufferSize) ? bufferSize : minBufferSize;

            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    48000,
                    trackChannel,
                    ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
            );

            audioTrack.play();
        } catch (Exception e) {
            Utils.printE(TAG, e);
            destroyPlayer();
            return;
        }

        state = STATE_STARTED;
        playTread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                readAudioDataFromFile();
            }
        }, "OpusPlay Thread");
        playTread.start();

        if (eventSender != null)
            eventSender.sendEvent(OpusEvent.PLAYING_STARTED);
    }

    protected void readAudioDataFromFile()
    {
        if (state != STATE_STARTED) return;

        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        boolean isFinished = false;

        while (state != STATE_NONE) {
            if (state == STATE_PAUSED) {
                try {
                    Thread.sleep(10);
                    continue;
                } catch (Exception e) {
                    LogHelper.e(TAG, e.toString());
                    continue;
                }
            } else if (state == STATE_STARTED) {
                libLock.lock();
                opusTool.readOpusFile(buffer, bufferSize);
                int size = opusTool.getSize();
                libLock.unlock();

                if (size != 0) {
                    buffer.rewind();
                    byte[] data = new byte[size];
                    buffer.get(data);
                    audioTrack.write(data, 0, size);
                }

                notifyProgress();
                isFinished = opusTool.getFinished() != 0;
                if (isFinished) {
                    break;
                }
            }
        }
        if (state != STATE_NONE)
            state = STATE_NONE;
        if (eventSender != null)
            eventSender.sendEvent(OpusEvent.PLAYING_FINISHED);
    }

    public void pause()
    {
        if (state == STATE_STARTED) {
            audioTrack.pause();
            state = STATE_PAUSED;
            if (eventSender != null)
                eventSender.sendEvent(OpusEvent.PLAYING_PAUSED);
        }
        notifyProgress();
    }

    public void resume()
    {
        if (state == STATE_PAUSED) {
            audioTrack.play();
            state = STATE_STARTED;
            if (eventSender != null)
                eventSender.sendEvent(OpusEvent.PLAYING_STARTED);
        }
    }

    public void stop()
    {
        state = STATE_NONE;
        while (true) {
            try {
                Thread.sleep(20);
            } catch (Exception e) {
                LogHelper.e(TAG, e.toString());
            }

            if (!playTread.isAlive())
                break;
        }
        Thread.yield();
        destroyPlayer();
    }

    public String toggle(String fileName)
    {
        if (state == STATE_PAUSED && currentFileName.equals(fileName)) {
            resume();
            return "Pause";
        } else if (state == STATE_STARTED && currentFileName.equals(fileName)) {
            pause();
            return "Resume";
        } else {
            play(fileName);
            return "Pause";
        }
    }

    /**
     * Get duration, whose unit is second
     *
     * @return duration
     */
    public long getDuration()
    {
        return opusTool.getTotalDuration();
    }

    /**
     * Get Position of current playback, whose unit is second
     *
     * @return duration
     */
    public long getPosition()
    {
        return opusTool.getCurrentPosition();
    }

    public void seekOpusFile(float scale)
    {
        if (state == STATE_PAUSED || state == STATE_STARTED) {
            libLock.lock();
            opusTool.seekOpusFile(scale);
            libLock.unlock();
        }
    }

    private void notifyProgress()
    {
        // notify every 1 second
        if (System.currentTimeMillis() - lastNotificationTime >= 1000) {
            if (eventSender != null) {
                eventSender.sendProgressEvent(getPosition(), getDuration());
            }
        }
    }

    private void destroyPlayer()
    {
        libLock.lock();
        opusTool.closeOpusFile();
        libLock.unlock();
        try {
            if (audioTrack != null) {
                audioTrack.pause();
                audioTrack.flush();
                audioTrack.release();
                audioTrack = null;
            }
        } catch (Exception e) {
            Utils.printE(TAG, e);
        }
    }

    public boolean isWorking()
    {
        return state != STATE_NONE;
    }

    public void release()
    {
        if (state != STATE_NONE)
            stop();
    }
}