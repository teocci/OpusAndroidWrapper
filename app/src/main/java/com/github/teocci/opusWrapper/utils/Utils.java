package com.github.teocci.opusWrapper.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Jan-08
 */

public class Utils
{

    public static Map<String, Object> castHash(Map input)
    {
        return castHash(input, String.class, Object.class);
    }

    public static <K, V> Map<K, V> castHash(Map input, Class<K> keyClass, Class<V> valueClass)
    {
        Map<K, V> output = new HashMap<>();
        if (input == null)
            return output;
        for (Object key : input.keySet().toArray()) {
            if ((key == null) || (keyClass.isAssignableFrom(key.getClass()))) {
                Object value = input.get(key);
                if ((value == null) || (valueClass.isAssignableFrom(value.getClass()))) {
                    K k = keyClass.cast(key);
                    V v = valueClass.cast(value);
                    output.put(k, v);
                } else {
                    throw new AssertionError(
                            "Cannot cast to HashMap<" + keyClass.getSimpleName()
                                    + ", " + valueClass.getSimpleName() + ">"
                                    + ", value " + value + " is not a " + valueClass.getSimpleName()
                    );
                }
            } else {
                throw new AssertionError(
                        "Cannot cast to HashMap<" + keyClass.getSimpleName()
                                + ", " + valueClass.getSimpleName() + ">"
                                + ", key " + key + " is not a " + keyClass.getSimpleName()
                );
            }
        }
        return output;
    }
}
