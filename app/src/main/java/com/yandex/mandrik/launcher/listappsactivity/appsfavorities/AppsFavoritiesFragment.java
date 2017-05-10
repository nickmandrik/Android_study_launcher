package com.yandex.mandrik.launcher.listappsactivity.appsfavorities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yandex.mandrik.launcher.R;
import com.yandex.mandrik.launcher.appdata.ContactsHelper;
import com.yandex.mandrik.launcher.listappsactivity.appsfavorities.adapter.FavoritesListAdapter;
import com.yandex.mandrik.launcher.settingsactivity.SettingsActivity;
import com.yandex.mandrik.launcher.util.eventbus.ChangeCountCeilsEvent;
import com.yandex.mandrik.launcher.util.eventbus.ChangeCountMemUriEvent;
import com.yandex.mandrik.launcher.util.eventbus.ChangePackageEvent;
import com.yandex.mandrik.launcher.util.eventbus.ClearFavoritesEvent;
import com.yandex.mandrik.launcher.util.eventbus.ClearHistoryUriEvent;
import com.yandex.mandrik.launcher.util.eventbus.FavoritesRecyclerChangedEvent;
import com.yandex.mandrik.launcher.util.eventbus.SetFavoritesEvent;
import com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper;
import com.yandex.mandrik.launcher.util.layout.RecyclerSpanSizeLookup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import static com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper.getCountCeilsInRowLandscape;
import static com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper.getCountCeilsInRowPortrait;

public class AppsFavoritiesFragment extends Fragment {
    private Context context;

    private RecyclerView favoritesRecycler;
    private FavoritesListAdapter listAdapter;

    private EditText editUri;

    private EventBus bus = EventBus.getDefault();

    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorites, container, false);

        context = rootView.getContext();

        editUri = (EditText) rootView.findViewById(R.id.editUri);

        setSpinner();

        editUri.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(editUri.getText())));


                    if(intent.resolveActivity(context.getPackageManager()) != null) {
                        // save uri in the file
                        SharedPreferencesHelper.addUri(context, String.valueOf(editUri.getText()));

                        setSpinner();

                        // start activity
                        startActivity(intent);
                    } else {
                        Toast toast = Toast.makeText(context, getString(R.string.incorrect_uri) + " " + editUri.getText(), Toast.LENGTH_LONG);
                        TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
                        view.setBackgroundColor(Color.DKGRAY);
                        view.setTextColor(Color.WHITE);
                        toast.show();
                    }

                    editUri.setText("");
                    editUri.clearFocus();
                    hideSoftKeyboard(editUri);
                    return true;
                }
                return false;
            }
        });

        favoritesRecycler = (RecyclerView) rootView.findViewById(R.id.favoritesRecyclerView);

        listAdapter = new FavoritesListAdapter(context);
        listAdapter.setHeaders(new String[] {getString(R.string.contacts), getString(R.string.favorites)});

        favoritesRecycler.setHasFixedSize(true);


        ImageButton imageButton = (ImageButton) rootView.findViewById(R.id.settings_but);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettings(v);
            }
        });

        favoritesRecycler.setAdapter(listAdapter);
        setLayoutManagerOnRecycler();

        updateContacts();

        return rootView;
    }

    public void setSpinner() {

        Integer countShownUri = SharedPreferencesHelper.getCountVisibleUris(context);
        Integer countUri = SharedPreferencesHelper.getCountSavedUris(context);

        final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);

        int min = countShownUri;
        if(min > countUri) {
            min = countUri;
        }

        if(min > 0) {
            spinner.setVisibility(View.VISIBLE);

            String[] savedUris = SharedPreferencesHelper.getUris(context);
            String[] visibleUris = new String[min];

            for(int i = 0; i < min; i++) {
                visibleUris[i] = savedUris[i];
            }

            ArrayAdapter<String> adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, visibleUris);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(adapter);
            spinner.setPrompt(context.getString(R.string.last_uris));

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    editUri.setText(spinner.getSelectedItem().toString());
                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        } else if(spinner != null){
            spinner.setVisibility(View.INVISIBLE);
        }
    }




    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayoutManagerOnRecycler();
    }

    private void setLayoutManagerOnRecycler() {

        int countInRow = getCountCeilsInRowLandscape(context);
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            countInRow = getCountCeilsInRowPortrait(context);
        }

        GridLayoutManager layoutManager = new GridLayoutManager(context, countInRow, GridLayoutManager.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new RecyclerSpanSizeLookup(countInRow, favoritesRecycler.getAdapter()));

        favoritesRecycler.setLayoutManager(layoutManager);
    }

    public void startSettings(View view) {
        Intent intentMainApps = new Intent (context, SettingsActivity.class);
        this.startActivity(intentMainApps);
    }



    public void updateContacts() {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(context.getString(R.string.contacts_access_needed));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage(context.getString(R.string.please_confirm_contacts_access));
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.READ_CONTACTS}
                                    , 1);
                        }
                    });
                    builder.show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
                }
            }
        } else {
            int countInRow = getCountCeilsInRowLandscape(context);
            if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                countInRow = getCountCeilsInRowPortrait(context);
            }
            listAdapter.contactsList = ContactsHelper.fetchContacts(context, countInRow);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    int countInRow = getCountCeilsInRowLandscape(context);
                    if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        countInRow = getCountCeilsInRowPortrait(context);
                    }
                    listAdapter.contactsList = ContactsHelper.fetchContacts(context, countInRow);
                    listAdapter.notifyDataSetChanged();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }







    @Subscribe
    public void onFavoritesRecyclerChangedEvent(FavoritesRecyclerChangedEvent event) {
        if(event.isInstall) {
            listAdapter.addFavorite(event.appInfo);
            listAdapter.notifyItemInserted(listAdapter.contactsList.size() + listAdapter.favoriteAppsList.size() + 2);
        } else {
            if(listAdapter.favoriteAppsList.contains(event.appInfo)) {
                int pos = listAdapter.favoriteAppsList.indexOf(event.appInfo);
                listAdapter.removeFavorite(pos + listAdapter.contactsList.size() + 2);
                listAdapter.notifyItemRemoved(pos + listAdapter.contactsList.size() + 2);
            }
        }
    }


    @Subscribe
    public void onClearFavoritesEvent(ClearFavoritesEvent event) {
        listAdapter.clearFavorites();
    }


    @Subscribe
    public void onClearHistoryUriEvent(ClearHistoryUriEvent event) {
        setSpinner();
    }


    @Subscribe
    public void onChangeCountMemUriEvent(ChangeCountMemUriEvent event) {

        SharedPreferencesHelper.changeCountVisibleUris(context, event.countMemUri);
        setSpinner();
    }


    @Subscribe
    public void onSetFavoritesEvent(SetFavoritesEvent event) {
        listAdapter.favoriteAppsList = new ArrayList();
        listAdapter.favoriteAppsList = event.favoritesAppsList;
    }


    @Subscribe
    public void onChangeCountCeilsEvent(ChangeCountCeilsEvent event) {
        updateContacts();
        setLayoutManagerOnRecycler();
        listAdapter.notifyDataSetChanged();
    }


    @Subscribe
    public void onChangePackageEvent(ChangePackageEvent event) {

        if(event.action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            for(int i = 0; i < listAdapter.favoriteAppsList.size(); i++) {
                if(listAdapter.favoriteAppsList.get(i).second.getPackageName().equals(event.namePackage)) {
                    listAdapter.removeFavorite(i + listAdapter.contactsList.size() + 2);
                }
            }
        }
    }
}
