package com.github.teocci.opuslib;

import com.github.teocci.opuslib.utils.Utils;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Dec-26
 */
public class OpusConverter
{
    private static String TAG = OpusConverter.class.getName();

    private static final int STATE_NONE = 0;
    private static final int STATE_CONVERTING = 1;
    private static final boolean TYPE_ENC = true;
    private static final boolean TYPE_DEC = false;

    private volatile int state = STATE_NONE;
    private boolean convertType;

    private String inputFile;
    private String outputFile;
    private String option;

    private OpusTool opusTool = new OpusTool();
    private Thread workerThread = new Thread();
    private OpusEvent eventSender = null;

    private static volatile OpusConverter converter;


    private OpusConverter() {}

    public static OpusConverter getInstance()
    {
        if (converter == null) {
            synchronized (OpusConverter.class) {
                if (converter == null) {
                    converter = new OpusConverter();
                }
            }
        }
        return converter;
    }

    public void setEventSender(OpusEvent es)
    {
        eventSender = es;
    }

    class ConvertThread implements Runnable
    {
        public void run()
        {
            if (eventSender != null) {
                eventSender.sendEvent(OpusEvent.CONVERT_STARTED);
            }

            if (convertType == TYPE_ENC) {
                opusTool.encode(inputFile, outputFile, option);
            } else if (convertType == TYPE_DEC) {
                opusTool.decode(inputFile, outputFile, option);
            }
            state = STATE_NONE;

            OpusTrackInfo.getInstance().addOpusFile(outputFile);
            if (eventSender != null) {
                eventSender.sendEvent(OpusEvent.CONVERT_FINISHED, outputFile);
            }
        }
    }

    public void encode(String fileNameIn, String fileNameOut, String opt)
    {
        if (!Utils.isWAVFile(fileNameIn)) {
            if (eventSender != null)
                eventSender.sendEvent(OpusEvent.CONVERT_FAILED);
            return;
        }
        state = STATE_CONVERTING;
        convertType = TYPE_ENC;
        inputFile = fileNameIn;
        outputFile = fileNameOut;
        option = opt;
        workerThread = new Thread(new ConvertThread(), "Opus Enc Thread");
        workerThread.start();
    }

    public void decode(String fileNameIn, String fileNameOut, String opt)
    {
        if (!Utils.isFileExist(fileNameIn) || opusTool.isOpusFile(fileNameIn) == 0) {
            if (eventSender != null) {
                eventSender.sendEvent(OpusEvent.CONVERT_FAILED);
            }
            return;
        }
        state = STATE_CONVERTING;
        convertType = TYPE_DEC;
        inputFile = fileNameIn;
        outputFile = fileNameOut;
        option = opt;
        workerThread = new Thread(new ConvertThread(), "Opus Dec Thread");
        workerThread.start();
    }

    public boolean isWorking()
    {
        return state != STATE_NONE;
    }

    public void release()
    {
        try {
            if (state == STATE_CONVERTING && workerThread.isAlive()) {
                workerThread.interrupt();
            }
        } catch (Exception e) {
            Utils.printE(TAG, e);
        } finally {
            state = STATE_NONE;
            if (eventSender != null) {
                eventSender.sendEvent(OpusEvent.CONVERT_FAILED);
            }
        }
    }
}