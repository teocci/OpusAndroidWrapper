package com.github.teocci.opuslib.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Dec-26
 */
public class Utils
{
    private static final String TAG = LogHelper.makeLogTag(Utils.class);

    public static String getExtension(String fileName)
    {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    public static void printE(String tag, Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        LogHelper.e(tag, sw.toString());
    }

    public static String getFileName(String path)
    {
        String rst = null;
        try {
            File f = new File(path);
            rst = f.getName();
        } catch (Exception e) {
//            printE("OpusTool", e);
            LogHelper.e(TAG, e);
        }
        return rst;
    }

    public static boolean isWAVFile(String fileName)
    {
        byte header[] = new byte[16];
        try {
            File f = new File(fileName);
            if (!f.exists()) {
                LogHelper.d(TAG, fileName + ":" + "File does not exist.");
                return false;
            }
            long actualLength = f.length();
            FileInputStream io = new FileInputStream(f);
            io.read(header, 0, 16);
            io.close();

            String tag = new String(header, 0, 4) + new String(header, 8, 8);
            if (!tag.equals("RIFFWAVEfmt ")) {
                LogHelper.d(TAG, fileName + ":" + "It's not a WAV file!");
                return false;
            }

            long paraLength = (header[4] & 0x000000ff) | ((header[5] << 8) & 0x0000ff00) |
                    ((header[6] << 16) & 0x00ff0000) | ((header[7] << 24) & 0xff000000);
            if (paraLength != actualLength - 8) {
                LogHelper.d(TAG, fileName + ":" + "It might be a WAV file, but it's corrupted!");
                return false;
            }
            return true;

        } catch (Exception e) {
            LogHelper.d(TAG, fileName + ":" + "File Error");
            return false;
        }
    }

    public static boolean isFileExist(String fileName)
    {
        return new File(fileName).exists();
    }

    public static void saveObj(String fileName, Object obj)
    {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            ObjectOutputStream oout = new ObjectOutputStream(fout);
            oout.writeObject(obj);
            fout.close();
            oout.close();
        } catch (Exception e) {
            LogHelper.d(TAG, e);
        }
    }

    public static Object readObj(String fileName)
    {
        Object obj = new Object();
        try {
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream oin = new ObjectInputStream(fin);
            obj = oin.readObject();
            fin.close();
            oin.close();
        } catch (Exception e) {
            LogHelper.d(TAG, e);
        } finally {
            return obj;
        }
    }
}
