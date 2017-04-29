package com.yandex.mandrik.launcher.util.eventbus;

import com.yandex.mandrik.launcher.listappsactivity.appdata.AppInfo;

/**
 * Created by Home on 29.04.2017.
 */

public class ChangePackageEvent {
    public AppInfo appInfo;
    public String action;
    public String namePackage;

    public ChangePackageEvent(AppInfo appInfo, String idOperation, String namePackage) {
        this.appInfo = appInfo;
        this.action = idOperation;
        this.namePackage = namePackage;
    }
}
