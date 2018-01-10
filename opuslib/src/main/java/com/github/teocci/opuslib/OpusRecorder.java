package com.github.teocci.opuslib;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.github.teocci.opuslib.model.AudioTime;
import com.github.teocci.opuslib.utils.LogHelper;
import com.github.teocci.opuslib.utils.Utils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioRecord.ERROR_INVALID_OPERATION;
import static com.github.teocci.opuslib.OpusEvent.RECORD_FAILED;
import static com.github.teocci.opuslib.OpusEvent.RECORD_FINISHED;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Dec-26
 */
public class OpusRecorder
{
    private static final String TAG = LogHelper.makeLogTag(OpusRecorder.class);

    private static volatile OpusRecorder opusRecorder;

    private static final int STATE_NONE = 0;
    private static final int STATE_STARTED = 1;

    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private volatile int state = STATE_NONE;

    private AudioRecord recorder = null;

    private Thread recordingThread = new Thread();

    private OpusTool opusTool = new OpusTool();

    private int bufferSize = 0;
    // Should be 1920, to meet with function writeFrame()
    private ByteBuffer fileBuffer = ByteBuffer.allocateDirect(1920);

    private String filePath = null;
    private OpusEvent eventSender = null;
    private Timer progressTimer = null;
    private AudioTime recordTime = new AudioTime();

    private OpusRecorder() {}

    public static OpusRecorder getInstance()
    {
        if (opusRecorder == null) {
            synchronized (OpusRecorder.class) {
                if (opusRecorder == null) {
                    opusRecorder = new OpusRecorder();
                }
            }
        }
        return opusRecorder;
    }

    public void setEventSender(OpusEvent es)
    {
        eventSender = es;
    }

    public void startRecording(final String file)
    {
        if (state == STATE_STARTED) return;

        int minBufferSize = AudioRecord.getMinBufferSize(
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING
        );

        bufferSize = (minBufferSize / 1920 + 1) * 1920;

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSize
        );

        recorder.startRecording();

        state = STATE_STARTED;
        if (file.isEmpty()) {
            filePath = OpusTrackInfo.getInstance().getAValidFileName("OpusRecord");
        } else {
            filePath = file;
        }
//        filePath = file.isEmpty() ? initRecordFileName() : file;
        int rst = opusTool.startRecording(filePath);
        if (rst != 1) {
            if (eventSender != null) {
                eventSender.sendEvent(RECORD_FAILED);
            }
            LogHelper.e(TAG, "recorder initially error");
            return;
        }

        if (eventSender != null) {
            eventSender.sendEvent(OpusEvent.RECORD_STARTED);
        }

        recordingThread = new Thread(new RecordThread(), "OpusRecord Thread");
        recordingThread.start();
    }


    private void writeAudioDataToOpus(ByteBuffer buffer, int size)
    {
        ByteBuffer finalBuffer = ByteBuffer.allocateDirect(size);
        finalBuffer.put(buffer);
        finalBuffer.rewind();
        boolean flush = false;

        // write data to Opus file
        while (state == STATE_STARTED && finalBuffer.hasRemaining()) {
            int oldLimit = -1;
            if (finalBuffer.remaining() > fileBuffer.remaining()) {
                oldLimit = finalBuffer.limit();
                finalBuffer.limit(fileBuffer.remaining() + finalBuffer.position());
            }
            fileBuffer.put(finalBuffer);
            if (fileBuffer.position() == fileBuffer.limit() || flush) {
                int length = !flush ? fileBuffer.limit() : finalBuffer.position();

                int rst = opusTool.writeFrame(fileBuffer, length);
                if (rst != 0) {
                    fileBuffer.rewind();
                }
            }
            if (oldLimit != -1) {
                finalBuffer.limit(oldLimit);
            }
        }
    }

    private void writeAudioDataToFile()
    {
        if (state != STATE_STARTED) return;

        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);

        while (state == STATE_STARTED) {
            buffer.rewind();
            int len = recorder.read(buffer, bufferSize);
            LogHelper.d(TAG, "\n bufferSize's length is " + len);
            if (len != ERROR_INVALID_OPERATION) {
                try {
                    writeAudioDataToOpus(buffer, len);
                } catch (Exception e) {
                    if (eventSender != null)
                        eventSender.sendEvent(RECORD_FAILED);
                    Utils.printE(TAG, e);
                }
            }
        }
    }

    private void updateTrackInfo()
    {
        OpusTrackInfo info = OpusTrackInfo.getInstance();
        info.addOpusFile(filePath);
        if (eventSender != null) {
            File f = new File(filePath);
            eventSender.sendEvent(RECORD_FINISHED, f.getName());
        }
    }

    public void stopRecording()
    {
        if (state != STATE_STARTED) return;

        state = STATE_NONE;
        progressTimer.cancel();
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            Utils.printE(TAG, e);
        }

        if (null != recorder) {
            opusTool.stopRecording();
            recordingThread = null;
            recorder.stop();
            recorder.release();
            recorder = null;
        }

        updateTrackInfo();
    }

    public boolean isWorking()
    {
        return state != STATE_NONE;
    }

    public void release()
    {
        if (state != STATE_NONE) {
            stopRecording();
        }
    }

    private class ProgressTask extends TimerTask
    {
        public void run()
        {
            if (state != STATE_STARTED) {
                progressTimer.cancel();
            } else {
                recordTime.add(1);
                String progress = recordTime.getTime();
                if (eventSender != null) {
                    eventSender.sendRecordProgressEvent(progress);
                }
            }
        }
    }

    private class RecordThread implements Runnable
    {
        public void run()
        {
            progressTimer = new Timer();
            recordTime.setTimeInSecond(0);
            progressTimer.schedule(new ProgressTask(), 1000, 1000);

            writeAudioDataToFile();
        }
    }
}
