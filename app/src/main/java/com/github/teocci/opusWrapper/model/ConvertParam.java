package com.github.teocci.opusWrapper.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Dec-26
 */
public class ConvertParam implements Serializable
{
    public static final long serialVersionUID = 1234567890987654322L;

    private Map<Integer, String> types = new HashMap<>(8);
    private Map<Integer, String[]> data = new HashMap<>(16);
    private Map<Integer, Integer> selection = new HashMap<>(8);

    public ConvertParam() {}

    public void add(int id, String parameter, String[] valueRange)
    {
        Integer key = id;
        types.put(key, parameter);
        data.put(key, valueRange);
    }

    public void select(int id, int valueIndex)
    {
        Integer key = id;
        Integer valueIdx = valueIndex;
        selection.put(key, valueIdx);
    }

    public String getFinalSelections()
    {
        StringBuilder result = new StringBuilder();
        for (Integer i : selection.keySet()) {
            Integer offset = selection.get(i);
            result.append(types.get(i));
            result.append(data.get(i)[offset]);
        }

        return result.toString();
    }

    public int getSelectedIndex(int id)
    {
        Integer i = id;
        return selection.get(i);
    }

    public String[] getValues(int id)
    {
        Integer i = id;
        return data.get(i);
    }
}