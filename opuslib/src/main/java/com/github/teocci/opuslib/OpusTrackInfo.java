package com.github.teocci.opuslib;

import android.os.Environment;

import com.github.teocci.opuslib.model.AudioPlayList;
import com.github.teocci.opuslib.model.AudioTime;
import com.github.teocci.opuslib.utils.LogHelper;
import com.github.teocci.opuslib.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Dec-26
 */
public class OpusTrackInfo
{
    private String TAG = LogHelper.makeLogTag(OpusTrackInfo.class);

    private static volatile OpusTrackInfo opusTrackInfo;

    private OpusEvent eventSender;
    private OpusTool opusTool = new OpusTool();

    private String appExtDir;
    private File requestDirFile;

    private Thread workThread = new Thread();
    private AudioPlayList audioPlayList = new AudioPlayList();
    private AudioTime audioTime = new AudioTime();

    public static final String TITLE_TITLE = "TITLE";
    public static final String TITLE_ABS_PATH = "ABS_PATH";
    public static final String TITLE_DURATION = "DURATION";
    public static final String TITLE_IMG = "TITLE_IMG";
    public static final String TITLE_IS_CHECKED = "TITLE_IS_CHECKED";

    private OpusTrackInfo()
    {
        // create OPlayer directory if it does not exist.
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return;
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        appExtDir = sdcardPath + "/OpusLib/";
        File fp = new File(appExtDir);
        if (!fp.exists()) {
            fp.mkdir();
        }

        getTrackInfo(appExtDir);
    }

    public static OpusTrackInfo getInstance()
    {
        if (opusTrackInfo == null)
            synchronized (OpusTrackInfo.class) {
                if (opusTrackInfo == null)
                    opusTrackInfo = new OpusTrackInfo();
            }
        return opusTrackInfo;
    }

    public void setEvenSender(OpusEvent opusEven)
    {
        eventSender = opusEven;
    }

    public void addOpusFile(String file)
    {
        try {
            Thread.sleep(10);
        } catch (Exception e) {
            Utils.printE(TAG, e);
        }

        File f = new File(file);
        if (f.exists() && "opus".equalsIgnoreCase(Utils.getExtension(file))
                && opusTool.openOpusFile(file) != 0) {
            Map<String, Object> map = new HashMap<>();
            map.put(TITLE_TITLE, f.getName());
            map.put(TITLE_ABS_PATH, file);
            audioTime.setTimeInSecond(opusTool.getTotalDuration());
            map.put(TITLE_DURATION, audioTime.getTime());
            map.put(TITLE_IS_CHECKED, false);
            //TODO: get image from opus files
            map.put(TITLE_IMG, 0);
            audioPlayList.add(map);
            opusTool.closeOpusFile();

            if (eventSender != null) {
                eventSender.sendTrackInfoEvent(audioPlayList);
            }
        }
    }

    public String getAppExtDir()
    {
        return appExtDir;
    }

    public void sendTrackInfo()
    {
        if (eventSender != null) {
            eventSender.sendTrackInfoEvent(audioPlayList);
        }
    }

    public AudioPlayList getTrackInfo()
    {
        return audioPlayList;
    }

    private void getTrackInfo(String Dir)
    {
        if (Dir.length() == 0)
            Dir = appExtDir;
        File file = new File(Dir);
        if (file.exists() && file.isDirectory())
            requestDirFile = file;

        workThread = new Thread(new WorkingThread(), "Opus Trc Trd");
        workThread.start();
    }

    public String getAValidFileName(String prefix)
    {
        String name = prefix;
        String extention = ".opus";
        HashSet<String> set = new HashSet<>(100);
        List<Map<String, Object>> lst = getTrackInfo().getList();
        for (Map<String, Object> map : lst) {
            set.add(map.get(OpusTrackInfo.TITLE_TITLE).toString());
        }
        int i = 0;
        while (true) {
            i++;
            if (!set.contains(name + i + extention)) {
                break;
            }
        }

        return appExtDir + name + i + extention;
    }

    private void prepareTrackInfo(File file)
    {
        try {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    String name = f.getName();
                    String absPath = f.getAbsolutePath();
                    if ("opus".equalsIgnoreCase(Utils.getExtension(name))
                            && opusTool.openOpusFile(absPath) != 0) {
                        Map<String, Object> map = new HashMap<>();
                        map.put(TITLE_TITLE, f.getName());
                        map.put(TITLE_ABS_PATH, absPath);
                        audioTime.setTimeInSecond(opusTool.getTotalDuration());
                        map.put(TITLE_DURATION, audioTime.getTime());
                        map.put(TITLE_IS_CHECKED, false);
                        // TODO: get image from opus files
                        map.put(TITLE_IMG, 0);
                        audioPlayList.add(map);
                        opusTool.closeOpusFile();
                    }

                } else if (f.isDirectory()) {
                    prepareTrackInfo(f);
                }
            }
        } catch (Exception e) {
            Utils.printE(TAG, e);
        }
    }

    class WorkingThread implements Runnable
    {
        public void run()
        {
            prepareTrackInfo(requestDirFile);
            sendTrackInfo();
        }
    }

    public void release()
    {
        try {
            if (workThread.isAlive())
                workThread.interrupt();
        } catch (Exception e) {
            Utils.printE(TAG, e);
        }
    }
}
