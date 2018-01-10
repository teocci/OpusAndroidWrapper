package com.github.teocci.opusWrapper.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleAdapter;

import com.github.teocci.opusWrapper.utils.Utils;
import com.github.teocci.opuslib.OpusTrackInfo;
import com.github.teocci.opuslib.utils.LogHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Jan-02
 */

public class ListViewAdapter extends SimpleAdapter
{
    private static final String TAG = LogHelper.makeLogTag(ListViewAdapter.class);
    private int lastHighlightedItemPosition = -1;

    public ListViewAdapter(Context context, List<Map<String, Object>> data, int resource, String[] from, int[] to)
    {
        super(context, data, resource, from, to);
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setHighlightedItemPosition(int position)
    {
        lastHighlightedItemPosition = position;
    }

    public int getHighlightedItemPosition()
    {
        return lastHighlightedItemPosition;
    }

    public boolean highlightedItemByOffset(int offset)
    {
        return highlightedItem(lastHighlightedItemPosition + offset);
    }

    public boolean highlightedItem(int position)
    {
        try {
            Map<String, Object> item;
            if (lastHighlightedItemPosition >= 0 && lastHighlightedItemPosition < getCount()) {
                item = Utils.castHash((Map) getItem(lastHighlightedItemPosition));
                item.put(OpusTrackInfo.TITLE_IS_CHECKED, false);
            }
            if (position >= 0 && position < getCount()) {
                item = Utils.castHash((Map) getItem(position));
                item.put(OpusTrackInfo.TITLE_IS_CHECKED, true);
                lastHighlightedItemPosition = position;
                notifyDataSetChanged();
                return true;
            }
        } catch (Exception e) {
            LogHelper.e(TAG, e);
            return false;
        }
        return false;
    }

    public Map<String, Object> getHighlightedItem()
    {
        if (lastHighlightedItemPosition < 0 && getCount() > 0) {
            highlightedItem(0);
            return Utils.castHash((Map) getItem(0));
        }
        if (lastHighlightedItemPosition < 0 || lastHighlightedItemPosition >= getCount()) {
            return null;
        }
        return Utils.castHash((Map) getItem(lastHighlightedItemPosition));
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        return super.getView(position, convertView, parent);
    }

    class ViewHolder
    {
        public CheckBox checkBox = null;
    }
}