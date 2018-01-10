package com.github.teocci.opuslib.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Jan-02
 */
public class AudioPlayList implements Serializable
{
    public static final long serialVersionUID = 1234567890987654321L;

    public AudioPlayList() {}

    private List<Map<String, Object>> audioInfoList = new ArrayList<>(32);

    public void add(Map<String, Object> map)
    {
        audioInfoList.add(map);
    }

    public List<Map<String, Object>> getList()
    {
        return audioInfoList;
    }

    public boolean isEmpty()
    {
        return audioInfoList.isEmpty();
    }

    public int size()
    {
        return audioInfoList.size();
    }

    public void clear()
    {
        audioInfoList.clear();
    }
}
