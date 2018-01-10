package com.github.teocci.opusWrapper.model;

import android.support.annotation.NonNull;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Jan-08
 */

public class TrackItem extends AbstractMap<String, Object>
{
    private final Map<String, Object> content = new HashMap<>();

    @NonNull
    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        return content.entrySet();
    }

    @NonNull
    @Override
    public Set<String> keySet()
    {
        return content.keySet();
    }

    @NonNull
    @Override
    public Collection<Object> values()
    {
        return content.values();
    }

    @Override
    public Object put(final String key, final Object value)
    {
        return content.put(key, value);
    }
}
