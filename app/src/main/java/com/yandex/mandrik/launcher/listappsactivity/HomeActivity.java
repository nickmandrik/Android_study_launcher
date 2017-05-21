package com.yandex.mandrik.launcher.listappsactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yandex.mandrik.launcher.listappsactivity.pageadapter.HomeScreenSlidePagerAdapter;

import com.yandex.mandrik.launcher.R;
import com.yandex.mandrik.launcher.util.eventbus.ChangeCountCeilsEvent;
import com.yandex.mandrik.launcher.util.eventbus.ChangeThemeEvent;
import com.yandex.mandrik.launcher.util.eventbus.HideFavoritesEvent;
import com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper;
import com.yandex.mandrik.launcher.util.receiver.UpdateApplicationsReceiver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.yandex.mandrik.launcher.util.preference.constants.SharedPreferenceConstants.*;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().post(new ChangeCountCeilsEvent());
    }

    HomeScreenSlidePagerAdapter pagerAdapter;
    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        setTheme(SharedPreferencesHelper.getIdResTheme(HomeActivity.this));
        setContentView(R.layout.activity_home);

        /*if(!isVisitedWelcomeActivity()) {
            this.finish();
            Intent intentWelcome = new Intent(this, WelcomeActivity.class);
            this.startActivity(intentWelcome);
        }*/



        pager = (ViewPager) findViewById(R.id.rec_pager);
        String[] headers = new String[2];
        headers[0] = getString(R.string.all_apps);
        headers[1] = getString(R.string.favorite_apps);
        pagerAdapter = new HomeScreenSlidePagerAdapter
                (getSupportFragmentManager(), 2,
                        SharedPreferencesHelper.isHiddenFavorites(HomeActivity.this),
                        headers, null);
        pager.setAdapter(pagerAdapter);

        UpdateApplicationsReceiver receiver = new UpdateApplicationsReceiver();
    }

    private boolean isVisitedWelcomeActivity() {
        SharedPreferences welcomeSettings =
                getSharedPreferences(APP_PREFERENCE_WELCOME_SETTINGS, Context.MODE_PRIVATE);
        boolean isVisited = welcomeSettings.getBoolean(IS_VISITED_WELCOME_ACTIVITY, false);
        return isVisited;
    }

    @Subscribe
    public void onHideFavoritesEvent(HideFavoritesEvent event) {

        boolean isHiddenFavorites = SharedPreferencesHelper.isHiddenFavorites(HomeActivity.this);

        if(isHiddenFavorites && pager.getCurrentItem() == 1) {
            pager.setCurrentItem(0);
        }

        pagerAdapter.setHiddenFavorites(isHiddenFavorites);

        pagerAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onChangeThemeEvent(ChangeThemeEvent event) {
        setTheme(SharedPreferencesHelper.getIdResTheme(HomeActivity.this));
        recreate();
    }
}
