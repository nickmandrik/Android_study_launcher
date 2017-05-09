package com.yandex.mandrik.launcher.listappsactivity.pageadapter;

import android.app.Application;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.yandex.mandrik.launcher.listappsactivity.appdata.ApplicationListManager;
import com.yandex.mandrik.launcher.listappsactivity.appsfavorities.AppsFavoritiesFragment;
import com.yandex.mandrik.launcher.listappsactivity.appsrecycler.AppsRecyclerFragment;

/**
 * A simple pager adapter that represents 5 ViewPageRecyclerFragment objects, in
 * sequence.
 */
public class AppsRecyclerScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    int numPages;
    String[] headers;
    ApplicationListManager applicationListManager;

    public AppsRecyclerScreenSlidePagerAdapter(FragmentManager fm, int numPages, boolean isHiddenFavorites,
                                               String[] headers, ApplicationListManager appManager) {
        super(fm);
        this.numPages = numPages;
        if(isHiddenFavorites) {
            this.numPages = numPages - 1;
        }
        this.headers = headers;
        this.applicationListManager = appManager;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return headers[0];
            case 1:
                return headers[1];
            default:
                return "None";
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AppsRecyclerFragment();
            case 1:
                return new AppsFavoritiesFragment();
            default:
                return null;
        }
    }


    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return numPages;
    }

    public void setHiddenFavorites(boolean isHiddenFavorites) {
        if(isHiddenFavorites) {
            numPages -= 1;
        } else {
            numPages += 1;
        }
    }
}