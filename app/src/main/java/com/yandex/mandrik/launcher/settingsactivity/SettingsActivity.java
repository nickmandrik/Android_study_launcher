package com.yandex.mandrik.launcher.settingsactivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.yandex.mandrik.launcher.R;
import com.yandex.mandrik.launcher.util.eventbus.ChangeCountCeilsEvent;
import com.yandex.mandrik.launcher.util.eventbus.ChangeCountMemUriEvent;
import com.yandex.mandrik.launcher.util.eventbus.ChangeThemeEvent;
import com.yandex.mandrik.launcher.util.eventbus.ClearFavoritesEvent;
import com.yandex.mandrik.launcher.util.eventbus.ClearHistoryUriEvent;
import com.yandex.mandrik.launcher.util.eventbus.HideFavoritesEvent;
import com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Home on 28.04.2017.
 */

public class SettingsActivity extends AppCompatActivity {

    /**
     * The context of represent this activity
     */
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = SettingsActivity.this;
        setTheme(SharedPreferencesHelper.getIdResTheme(context));
        setContentView(R.layout.activity_settings);


        boolean isHiddenFavorites = SharedPreferencesHelper.isHiddenFavorites(context);
        Button but = (Button) findViewById(R.id.but_hide_fav);
        if(isHiddenFavorites) {
            but.setText(R.string.add_favorites);
        } else {
            but.setText(R.string.hide_favorites);
        }


    }

    // +
    public void hideFavorites(View view) {

        final boolean isHiddenFavorites = SharedPreferencesHelper.isHiddenFavorites(context);
        String temp = context.getString(R.string.hide_favorites);
        if(isHiddenFavorites) {
            temp = context.getString(R.string.add_favorites);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm))
                .setMessage( temp + "?")
                .setIcon(R.drawable.yandex_icon)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {


                        SharedPreferencesHelper.changeStatusOfHideFavorites(context);


                        EventBus.getDefault().post(new HideFavoritesEvent());


                        Button but = (Button) findViewById(R.id.but_hide_fav);
                        if(!isHiddenFavorites) {
                            but.setText(R.string.add_favorites);
                        } else {
                            but.setText(R.string.hide_favorites);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();

    }

    // +
    public void clearFavorites(View view) {

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm))
                .setMessage(context.getString(R.string.clear_favorites) + "?")
                .setIcon(R.drawable.yandex_icon)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {



                        EventBus.getDefault().post(new ClearFavoritesEvent());




                        dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();
    }

    // +
    public void changeTheme(View view) {

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm))
                .setMessage(context.getString(R.string.change_theme) + "?")
                .setIcon(R.drawable.yandex_icon)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {



                        SharedPreferencesHelper.changeResTheme(context);


                        setTheme(SharedPreferencesHelper.getIdResTheme(context));
                        recreate();


                        EventBus.getDefault().post(new ChangeThemeEvent());




                        dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();
    }

    // +
    public void changeCountCeils(View view) {

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm))
                .setMessage(context.getString(R.string.change_count_ceils_in_row) + "?")
                .setIcon(R.drawable.yandex_icon)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {



                        SharedPreferencesHelper.changeCountCeilsInRowLandscape(context);
                        SharedPreferencesHelper.changeCountCeilsInRowPortrait(context);

                        EventBus.getDefault().post(new ChangeCountCeilsEvent());




                        dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();
    }

    // +
    public void changeMemorableUri(View view) {

        RelativeLayout linearLayout = new RelativeLayout(context);
        final NumberPicker aNumberPicker = new NumberPicker(context);
        aNumberPicker.setMaxValue(50);
        aNumberPicker.setMinValue(0);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(aNumberPicker,numPicerParams);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.choose_count_of_mem_uri);
        alertDialogBuilder.setView(linearLayout);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.change,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                EventBus.getDefault().post(new ChangeCountMemUriEvent(aNumberPicker.getValue()));

                                Toast.makeText(context, R.string.change_count_of_mem_uri_success, Toast.LENGTH_LONG);
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // +
    public void clearHistoryUri(View view) {

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm))
                .setMessage(context.getString(R.string.clear_history_uri) + "?")
                .setIcon(R.drawable.yandex_icon)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {



                        SharedPreferencesHelper.clearHistoryUri(context);

                        EventBus.getDefault().post(new ClearHistoryUriEvent());




                        dialog.dismiss();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.show();
    }
}