package com.yandex.mandrik.launcher.appdata;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.yandex.mandrik.launcher.R;


/**
 * Static class ApplicationListManager include method getInstalledApps
 * only to get system info about apps
 * Created by Nick Mandrik on 21.03.2017.
 * @author Nick Mandrik
 */

public class ApplicationListManager {

    private List<AppInfo> appsList;
    private List<AppInfo> newAppsList = new ArrayList();
    private List<AppInfo> popularAppsList = new ArrayList();

    private String[] headers;
    private Context context;

    public List<AppInfo> getAppsList() {
        return appsList;
    }

    public List<AppInfo> getNewAppsList() {
        return newAppsList;
    }

    public List<AppInfo> getPopularAppsList() {
        return popularAppsList;
    }

    public String[] getHeaders() {
        return headers;
    }

    private PackageManager packageManager;

    public ApplicationListManager(Context context, int maxElements) {

        this.context = context;
        packageManager = context.getPackageManager();

        setAppsList(getInstalledApps());

        setHeaders(new String[3]);
        getHeaders()[2] = context.getResources().getString(R.string.all_apps);;
        getHeaders()[1] = context.getResources().getString(R.string.new_apps);
        getHeaders()[0] = context.getResources().getString(R.string.popular_apps);


        updateNewApps(maxElements);
        updatePopularApps(maxElements);
    }

    /**
     * Used to get information about all executing apps on device that launcher can start.
     * @return List<AppInfo> apps that executing on Android device. Information about applications saved in
     * AppInfo
     */
    private List<AppInfo> getInstalledApps()
    {
        ArrayList<AppInfo> apps = new ArrayList();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(i, 0);
        for(ResolveInfo ri:availableActivities){
            AppInfo app = new AppInfo();
            app.setLabel(ri.loadLabel(packageManager).toString());
            app.setPackageName(ri.activityInfo.packageName);
            app.setIcon(ri.activityInfo.loadIcon(packageManager));
            String appFile = ri.activityInfo.applicationInfo.sourceDir;
            app.setLastModified(new File(appFile).lastModified());


            SharedPreferences appSettings =
                    context.getSharedPreferences("count_clicks_settings", Context.MODE_PRIVATE);

            Integer countClicks = appSettings.getInt(app.getPackageName(), 0);
            app.setCountClicks(countClicks);


            apps.add(app);
        }

        return apps;
    }

    public void updatePopularApps(int count) {
        setPopularAppsList(new ArrayList());
        SharedPreferences appSettings =
                context.getSharedPreferences("count_clicks_settings", Context.MODE_PRIVATE);

        List<AppInfo> sortedByClicksApps = new ArrayList();
        for(AppInfo appInfo: appsList) {
            appInfo.setCountClicks(appSettings.getInt(appInfo.getPackageName(), 0));
            sortedByClicksApps.add(appInfo);
        }

        Collections.sort(sortedByClicksApps, new ClicksComparator());

        for(int i = 0; i < count; i++) {
            getPopularAppsList().add(sortedByClicksApps.get(i));
        }
    }

    public void updateNewApps(int count) {
        setNewAppsList(new ArrayList());
        List<AppInfo> sortedByTimeApps = new ArrayList();
        sortedByTimeApps.addAll(getAppsList());

        Collections.sort(sortedByTimeApps, new TimeAppComparator());

        for(int i = 0; i < count; i++) {
            getNewAppsList().add(sortedByTimeApps.get(i));
        }
    }

    static class TimeAppComparator implements Comparator<AppInfo>
    {
        public int compare(AppInfo a1, AppInfo a2)
        {
            long def = -(a1.getLastModified()-a2.getLastModified());
            if(def < 0) {
                return -1;
            } else if(def == 0){
                return 0;
            } else {
                return 1;
            }
        }
    }

    static class ClicksComparator implements Comparator<AppInfo>
    {
        public int compare(AppInfo a1, AppInfo a2)
        {
            long def = -(a1.getCountClicks()-a2.getCountClicks());
            if(def < 0) {
                return -1;
            } else if(def == 0){
                return 0;
            } else {
                return 1;
            }
        }
    }

    public void incrementClicksInAppsList(int postionOfItem) {
        appsList.get(postionOfItem).setCountClicks(appsList.get(postionOfItem).getCountClicks()+1);

        SharedPreferences appSettings =
                context.getSharedPreferences("count_clicks_settings", Context.MODE_PRIVATE);

        SharedPreferences.Editor e = appSettings.edit();
        e.putInt(appsList.get(postionOfItem).getPackageName(), appsList.get(postionOfItem).getCountClicks());
        e.apply();
    }

    public void addAppInAppsList(AppInfo app) {
        appsList.add(app);
    }

    public void setAppsList(List<AppInfo> appsList) {
        this.appsList = appsList;
    }

    public void setNewAppsList(List<AppInfo> newAppsList) {
        this.newAppsList = newAppsList;
    }

    public void setPopularAppsList(List<AppInfo> popularAppsList) {
        this.popularAppsList = popularAppsList;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }
}