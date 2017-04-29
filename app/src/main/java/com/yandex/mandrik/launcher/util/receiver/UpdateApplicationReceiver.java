package com.yandex.mandrik.launcher.util.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.yandex.mandrik.launcher.listappsactivity.appdata.AppInfo;
import com.yandex.mandrik.launcher.util.eventbus.ChangePackageEvent;
import com.yandex.mandrik.launcher.util.eventbus.HideFavoritesEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import static android.R.attr.data;

/**
 * Created by Home on 27.04.2017.
 */

public class UpdateApplicationReceiver extends BroadcastReceiver {

    public UpdateApplicationReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();


        if (Intent.ACTION_PACKAGE_REMOVED.equals(action))
        {
            Uri uri = Uri.parse(String.valueOf(intent.getData().toString()));
            Log.d("Removed package ", uri.getSchemeSpecificPart());

            ChangePackageEvent event = new ChangePackageEvent(null, Intent.ACTION_PACKAGE_REMOVED, uri.getSchemeSpecificPart());
            EventBus.getDefault().post(event);

        } else if (Intent.ACTION_PACKAGE_ADDED.equals(action))
        {
            Uri uri = Uri.parse(String.valueOf(intent.getData().toString()));
            Log.d("d", "good!!!");
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInformation;
            try {
                applicationInformation = packageManager.getApplicationInfo(uri.getSchemeSpecificPart(),
                        PackageManager.GET_UNINSTALLED_PACKAGES);

                AppInfo app = new AppInfo();
                app.setLabel(applicationInformation.loadLabel(packageManager).toString());
                app.setPackageName(applicationInformation.packageName);
                app.setIcon(applicationInformation.loadIcon(packageManager));

                String appFile = applicationInformation.sourceDir;
                app.setLastModified(new File(appFile).lastModified());
                Log.d("Added package ", app.getPackageName());



                ChangePackageEvent event = new ChangePackageEvent(app, Intent.ACTION_PACKAGE_ADDED, uri.getSchemeSpecificPart());
                EventBus.getDefault().post(event);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d("NameNotFoundException", "Can't find package " + uri.getSchemeSpecificPart());
            }
        } else if (Intent.ACTION_PACKAGE_CHANGED.equals(action))
        {
            Uri uri = Uri.parse(String.valueOf(intent.getData().toString()));
            Log.d("d", "good!!!");
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInformation;
            try {
                applicationInformation = packageManager.getApplicationInfo(uri.getSchemeSpecificPart(),
                        PackageManager.GET_UNINSTALLED_PACKAGES);

                AppInfo app = new AppInfo();
                app.setLabel(applicationInformation.loadLabel(packageManager).toString());
                app.setPackageName(applicationInformation.packageName);
                app.setIcon(applicationInformation.loadIcon(packageManager));

                String appFile = applicationInformation.sourceDir;
                app.setLastModified(new File(appFile).lastModified());
                Log.d("Changed package ", app.getPackageName());



                ChangePackageEvent event = new ChangePackageEvent(app,
                        Intent.ACTION_PACKAGE_CHANGED, uri.getSchemeSpecificPart());
                EventBus.getDefault().post(event);

            } catch (PackageManager.NameNotFoundException e) {
                Log.d("NameNotFoundException", "Can't find package " + uri.getSchemeSpecificPart());
            }
        }
    }
}
