package com.yandex.mandrik.launcher.listappsactivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yandex.mandrik.launcher.appdata.ApplicationListManager;
import com.yandex.mandrik.launcher.listappsactivity.pageadapter.AppsRecyclerScreenSlidePagerAdapter;

import com.yandex.mandrik.launcher.R;
import com.yandex.mandrik.launcher.util.eventbus.ChangeCountCeilsEvent;
import com.yandex.mandrik.launcher.util.eventbus.ChangeThemeEvent;
import com.yandex.mandrik.launcher.util.eventbus.HideFavoritesEvent;
import com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper;
import com.yandex.mandrik.launcher.util.receiver.UpdateApplicationReceiver;
import com.yandex.mandrik.launcher.welcomeactivity.WelcomeActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.yandex.mandrik.launcher.util.preference.constants.LauncherConstants.*;

public class ListAppsViewPagerActivity extends AppCompatActivity {

    private ApplicationListManager appManager;

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().post(new ChangeCountCeilsEvent());
    }

    AppsRecyclerScreenSlidePagerAdapter pagerAdapter;
    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);






        setTheme(SharedPreferencesHelper.getIdResTheme(ListAppsViewPagerActivity.this));
        setContentView(R.layout.activity_recycler);

        if(!isVisitedWelcomeActivity()) {
            this.finish();
            Intent intentWelcome = new Intent(this, WelcomeActivity.class);
            this.startActivity(intentWelcome);
        }


        /**/
        pager = (ViewPager) findViewById(R.id.rec_pager);
        String[] headers = new String[2];
        headers[0] = getString(R.string.all_apps);
        headers[1] = getString(R.string.favorite_apps);
        pagerAdapter = new AppsRecyclerScreenSlidePagerAdapter
                (getSupportFragmentManager(), 2,
                        SharedPreferencesHelper.isHiddenFavorites(ListAppsViewPagerActivity.this),
                        headers, appManager);
        pager.setAdapter(pagerAdapter);

        UpdateApplicationReceiver receiver = new UpdateApplicationReceiver();
    }

    private boolean isVisitedWelcomeActivity() {
        SharedPreferences welcomeSettings =
                getSharedPreferences(APP_PREFERENCE_WELCOME_SETTINGS, Context.MODE_PRIVATE);
        boolean isVisited = welcomeSettings.getBoolean(IS_VISITED_WELCOME_ACTIVITY, false);
        return isVisited;
    }

    @Subscribe
    public void onHideFavoritesEvent(HideFavoritesEvent event) {

        boolean isHiddenFavorites = SharedPreferencesHelper.isHiddenFavorites(ListAppsViewPagerActivity.this);

        if(isHiddenFavorites && pager.getCurrentItem() == 1) {
            pager.setCurrentItem(0);
        }

        pagerAdapter.setHiddenFavorites(isHiddenFavorites);

        pagerAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onChangeThemeEvent(ChangeThemeEvent event) {
        setTheme(SharedPreferencesHelper.getIdResTheme(ListAppsViewPagerActivity.this));
        recreate();
    }
}
