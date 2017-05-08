package com.yandex.mandrik.launcher.listappsactivity.appsrecycler;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yandex.mandrik.launcher.R;
import com.yandex.mandrik.launcher.listappsactivity.appdata.AppInfo;
import com.yandex.mandrik.launcher.listappsactivity.appsfavorities.recycler.adapter.CustomFavoritiesTouchListener;
import com.yandex.mandrik.launcher.listappsactivity.appdata.ApplicationListManager;
import com.yandex.mandrik.launcher.listappsactivity.appsrecycler.recycler.adapter.ApplicationListAdapter;
import com.yandex.mandrik.launcher.util.clicker.RecyclerViewItemClickListener;
import com.yandex.mandrik.launcher.util.eventbus.ChangeCountCeilsEvent;
import com.yandex.mandrik.launcher.util.eventbus.ChangePackageEvent;
import com.yandex.mandrik.launcher.util.eventbus.FavoritesRecyclerChangedEvent;
import com.yandex.mandrik.launcher.util.layout.RecyclerSpanSizeLookup;
import com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import static com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper.getCountCeilsInRowLandscape;
import static com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper.getCountCeilsInRowPortrait;

/**
 * Created by Home on 27.04.2017.
 */

public class AppsRecyclerFragment extends Fragment {

    private RecyclerView appRecycler;
    private ApplicationListAdapter appAdapter;
    private ApplicationListManager appManager;

    private Context context;

    private View rootView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_recycler_apps, container, false);

        context = rootView.getContext();

        appRecycler = (RecyclerView) rootView.findViewById(R.id.apps_recycler);

        appManager = new ApplicationListManager(context, getCountInRow());
        appAdapter = new ApplicationListAdapter(appManager, context);

        appRecycler.setHasFixedSize(true);

        appRecycler.addOnItemTouchListener(new CustomFavoritiesTouchListener(context, appRecycler,
                new RecyclerViewItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if (appAdapter.getAppInfoById(position) != null) {
                            Intent intent = context.getPackageManager().getLaunchIntentForPackage(
                                    appAdapter.getAppInfoById(position).
                                            getPackageName().toString());
                            appManager.incrementClicksInAppsList(appAdapter.getIndexInArray(position));

                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onLongClick(View view, final int position) {
                        if (appAdapter.getAppInfoById(position) != null) {
                            /*remCountClicks(position);*/
                            view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                                @Override
                                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                                    boolean isHiddenPreferences = SharedPreferencesHelper.isHiddenFavorites(context);

                                    int[] values = new int[2];

                                    if(isHiddenPreferences) {
                                        values[0] = 0;
                                        values[1] = 1;
                                    } else {
                                        menu.add(0, v.getId(), 0, R.string.add_to_favorite);
                                        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                EventBus bus = EventBus.getDefault();
                                                bus.post(new FavoritesRecyclerChangedEvent
                                                        (appAdapter.getAppInfoById(position), true));
                                                return false;
                                            }
                                        });
                                        values[0] = 1;
                                        values[1] = 2;
                                     }

                                    menu.add(0, v.getId(), values[0], R.string.see_info);
                                    menu.add(0, v.getId(), values[1], R.string.delete);

                                    menu.getItem(values[0]).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            Uri uri = Uri.fromParts("package", appAdapter
                                                    .getAppInfoById(position).getPackageName(), null);
                                            Intent intent = new Intent(Intent.ACTION_ALL_APPS, uri);
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            startActivity(intent);
                                            return false;
                                        }
                                    });

                                    menu.getItem(values[1]).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem item) {
                                            Uri uri = Uri.fromParts("package", appAdapter
                                                    .getAppInfoById(position).getPackageName(), null);
                                            Intent it = new Intent(Intent.ACTION_DELETE, uri);
                                            startActivity(it);
                                            //appRecycler.getAdapter().remove(position);
                                            return false;
                                        }
                                    });
                                }
                            });
                        }
                    }
                }));

        appRecycler.setAdapter(appAdapter);
        setLayoutManagerOnRecycler();


        return rootView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayoutManagerOnRecycler();

        int countInRow = getCountInRow();
        appManager.updateNewApps(countInRow);
        appManager.updatePopularApps(countInRow);
        appRecycler.getAdapter().notifyDataSetChanged();
    }

    private void setLayoutManagerOnRecycler() {
        int countInRow = getCountInRow();

        GridLayoutManager layoutManager = new GridLayoutManager(context, countInRow, GridLayoutManager.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new RecyclerSpanSizeLookup(countInRow, appRecycler.getAdapter()));

        appRecycler.setLayoutManager(layoutManager);
    }

    public int getCountInRow() {
        int countInRow = getCountCeilsInRowLandscape(context);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            countInRow = getCountCeilsInRowPortrait(context);
        }
        return countInRow;
    }


    @Subscribe
    public void onChangeCountCeilsEvent(ChangeCountCeilsEvent event) {

        int countInRow = getCountInRow();;
        appManager.updateNewApps(countInRow);
        appManager.updatePopularApps(countInRow);

        setLayoutManagerOnRecycler();
        appRecycler.getAdapter().notifyDataSetChanged();
    }

    @Subscribe
    public void onChangePackageEvent(ChangePackageEvent event) {

        if(event.action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            List<AppInfo> appInfoList = appManager.getAppsList();
            for(int i = 0; i < appInfoList.size(); i++) {
                AppInfo appInfo = appInfoList.get(i);
                if(appInfo.getPackageName().equals(event.namePackage)) {
                    int pos = appAdapter.getPositionByIndexInArrayAppsList(i);
                    appAdapter.remove(pos);
                }
            }
        }


        if(event.action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            appManager.addAppInAppsList(event.appInfo);
        }

        if(event.action.equals(Intent.ACTION_PACKAGE_CHANGED)) {
            List<AppInfo> appInfoList = appManager.getAppsList();
            for(int i = 0; i < appInfoList.size(); i++) {
                AppInfo appInfo = appInfoList.get(i);
                if(appInfo.getPackageName().equals(event.namePackage)) {
                    appManager.getAppsList().remove(event.appInfo);
                    appManager.addAppInAppsList(event.appInfo);
                    appAdapter.notifyItemChanged(appAdapter.getPositionByIndexInArrayAppsList(i));
                    break;
                }
            }
        }

        int countInRow = getCountInRow();
        appManager.updateNewApps(countInRow);
        appManager.updatePopularApps(countInRow);

        appRecycler.getAdapter().notifyDataSetChanged();
    }
}
