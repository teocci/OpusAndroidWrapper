package com.github.teocci.opusWrapper.Adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Dec-27
 */
public class PageViewAdapter extends FragmentPagerAdapter
{
    private ArrayList<Fragment> fragments;

    public PageViewAdapter(FragmentManager fm, ArrayList<Fragment> lst)
    {
        super(fm);
        fragments = lst;
    }

    public PageViewAdapter(FragmentManager fm)
    {
        super(fm);
        fragments = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position)
    {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        Bundle b = fragments.get(position).getArguments();
        return b != null ? b.getString("Title") : null;
    }

    @Override
    public int getCount()
    {
        return fragments.size();
    }
}