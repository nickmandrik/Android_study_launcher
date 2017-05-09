package com.yandex.mandrik.launcher.util.eventbus;

import android.support.v4.util.Pair;

import com.yandex.mandrik.launcher.listappsactivity.appdata.AppInfo;

import java.util.ArrayList;

/**
 * Created by Home on 08.05.2017.
 */

public class SetFavoritesEvent {

    public ArrayList<Pair<Integer, AppInfo>> favoritesAppsList;

    public SetFavoritesEvent(ArrayList<Pair<Integer, AppInfo>> favoritesAppsList) {
        this.favoritesAppsList = favoritesAppsList;
    }
}
