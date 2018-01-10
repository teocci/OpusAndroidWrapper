package com.github.teocci.opuslib;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.github.teocci.opuslib.utils.LogHelper;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Dec-26
 */
public class OpusService extends Service
{
    private static final String TAG =  LogHelper.makeLogTag(OpusService.class);

    //This server
    private static final String ACTION_OPUSSERVICE = "com.github.teocci.opuslib.action.OPUSSERVICE";

    private static final String EXTRA_FILE_NAME = "FILE_NAME";
    private static final String EXTRA_FILE_NAME_OUT = "FILE_NAME_OUT";
    private static final String EXTRA_OPUS_CODING_OPTION = "OPUS_CODING_OPTION";
    private static final String EXTRA_CMD = "CMD";
    private static final String EXTRA_SEEKFILE_SCALE = "SEEKFILE_SCALE";

    private static final int CMD_PLAY = 10001;
    private static final int CMD_PAUSE = 10002;
    private static final int CMD_STOP_PLAYING = 10003;
    private static final int CMD_TOGGLE = 10004;
    private static final int CMD_SEEK_FILE = 10005;
    private static final int CMD_GET_TRACK_INFO = 10006;
    private static final int CMD_ENCODE = 20001;
    private static final int CMD_DECODE = 20002;
    private static final int CMD_RECORD = 30001;
    private static final int CMD_STOP_RECORDING = 30002;
    private static final int CMD_RECORD_TOGGLE = 30003;

    //Looper
    private volatile Looper serviceLooper;
    private volatile ServiceHandler serviceHandler;

    private OpusPlayer player;
    private OpusRecorder recorder;
    private OpusConverter converter;
    private OpusTrackInfo trackInfo;
    private OpusEvent event = null;


    @Override
    public void onCreate()
    {
        super.onCreate();
        player = OpusPlayer.getInstance();
        recorder = OpusRecorder.getInstance();
        converter = OpusConverter.getInstance();
        trackInfo = OpusTrackInfo.getInstance();

        event = new OpusEvent(getApplicationContext());

        trackInfo.setEvenSender(event);
        player.setEventSender(event);
        recorder.setEventSender(event);
        converter.setEventSender(event);

        // start looper in onCreate() instead of onStartCommand()
        HandlerThread thread = new HandlerThread("OpusServiceHandler");
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public void onDestroy()
    {
        // quit looper
        serviceLooper.quit();

        player.release();
        recorder.release();
        converter.release();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        serviceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public static void play(Context context, String fileName)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_PLAY);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        context.startService(intent);
    }

    public static void record(Context context, String fileName)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_RECORD);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        context.startService(intent);
    }

    public static void toggle(Context context, String fileName)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_TOGGLE);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        context.startService(intent);
    }

    public static void seekFile(Context context, float scale)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_SEEK_FILE);
        intent.putExtra(EXTRA_SEEKFILE_SCALE, scale);
        context.startService(intent);
    }

    /**
     * Request the Track info of all the opus files in the directory of this app
     *
     * @param context
     */
    public static void getTrackInfo(Context context)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_GET_TRACK_INFO);
        context.startService(intent);
    }

    public static void recordToggle(Context context, String fileName)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_RECORD_TOGGLE);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        context.startService(intent);
    }

    public static void pause(Context context)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_PAUSE);
        context.startService(intent);
    }

    public static void stopRecording(Context context)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_STOP_RECORDING);
        context.startService(intent);
    }

    public static void stopPlaying(Context context)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_STOP_PLAYING);
        context.startService(intent);
    }

    public static void encode(Context context, String fileName, String fileNameOut, String option)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_ENCODE);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        intent.putExtra(EXTRA_FILE_NAME_OUT, fileNameOut);
        intent.putExtra(EXTRA_OPUS_CODING_OPTION, option);
        context.startService(intent);
    }

    public static void decode(Context context, String fileName, String fileNameOut, String option)
    {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_DECODE);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        intent.putExtra(EXTRA_FILE_NAME_OUT, fileNameOut);
        intent.putExtra(EXTRA_OPUS_CODING_OPTION, option);
        context.startService(intent);
    }

    private void onHandleIntent(Intent intent)
    {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_OPUSSERVICE.equals(action)) {
                int request = intent.getIntExtra(EXTRA_CMD, 0);
                String fileName;
                String fileNameOut;
                String option;
                switch (request) {
                    case CMD_PLAY:
                        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                        handleActionPlay(fileName);
                        break;
                    case CMD_PAUSE:
                        handleActionPause();
                        break;
                    case CMD_TOGGLE:
                        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                        handleActionToggle(fileName);
                        break;
                    case CMD_STOP_PLAYING:
                        handleActionStopPlaying();
                        break;
                    case CMD_RECORD:
                        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                        handleActionRecord(fileName);
                        break;
                    case CMD_STOP_RECORDING:
                        handleActionStopRecording();
                        break;
                    case CMD_ENCODE:
                        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                        fileNameOut = intent.getStringExtra(EXTRA_FILE_NAME_OUT);
                        option = intent.getStringExtra(EXTRA_OPUS_CODING_OPTION);
                        handleActionEncode(fileName, fileNameOut, option);
                        break;
                    case CMD_DECODE:
                        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                        fileNameOut = intent.getStringExtra(EXTRA_FILE_NAME_OUT);
                        option = intent.getStringExtra(EXTRA_OPUS_CODING_OPTION);
                        handleActionDecode(fileName, fileNameOut, option);
                        break;
                    case CMD_RECORD_TOGGLE:
                        if (recorder.isWorking()) {
                            handleActionStopRecording();
                        } else {
                            fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                            handleActionRecord(fileName);
                        }
                        break;
                    case CMD_SEEK_FILE:
                        float scale = intent.getFloatExtra(EXTRA_SEEKFILE_SCALE, 0);
                        handleActionSeekFile(scale);
                        break;
                    case CMD_GET_TRACK_INFO:
                        trackInfo.sendTrackInfo();
                        break;
                    default:
                        LogHelper.e(TAG, "Unknown intent CMD,discarded!");
                }
            } else {
                LogHelper.e(TAG, "Unknown intent action,discarded!");
            }
        }
    }


    private void handleActionPlay(String fileName)
    {
        player.play(fileName);
    }

    private void handleActionStopPlaying()
    {
        player.stop();
    }

    private void handleActionPause()
    {
        player.pause();
    }

    private void handleActionToggle(String fileName)
    {
        player.toggle(fileName);
    }

    private void handleActionSeekFile(float scale)
    {
        player.seekOpusFile(scale);
    }

    private void handleActionRecord(String fileName)
    {
        recorder.startRecording(fileName);
    }

    private void handleActionStopRecording()
    {
        recorder.stopRecording();
    }

    private void handleActionEncode(String fileNameIn, String fileNameOut, String option)
    {
        converter.encode(fileNameIn, fileNameOut, option);
    }

    private void handleActionDecode(String fileNameIn, String fileNameOut, String option)
    {
        converter.decode(fileNameIn, fileNameOut, option);
    }

    private final class ServiceHandler extends Handler
    {
        public ServiceHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            onHandleIntent((Intent) msg.obj);
            //stopSelf()
        }
    }
}
