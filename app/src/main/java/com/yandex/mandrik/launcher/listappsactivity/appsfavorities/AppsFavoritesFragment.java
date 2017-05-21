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
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yandex.mandrik.launcher.R;
import com.yandex.mandrik.launcher.appdata.ContactInfo;
import com.yandex.mandrik.launcher.appdata.ContactsHelper;
import com.yandex.mandrik.launcher.listappsactivity.appsfavorities.adapter.FavoritesListAdapter;
import com.yandex.mandrik.launcher.settingsactivity.SettingsActivity;
import com.yandex.mandrik.launcher.util.eventbus.AddContactEvent;
import com.yandex.mandrik.launcher.util.eventbus.ChangeCountCeilsEvent;
import com.yandex.mandrik.launcher.util.eventbus.ChangeCountMemUriEvent;
import com.yandex.mandrik.launcher.util.eventbus.ChangePackageEvent;
import com.yandex.mandrik.launcher.util.eventbus.ClearFavoritesEvent;
import com.yandex.mandrik.launcher.util.eventbus.ClearHistoryUriEvent;
import com.yandex.mandrik.launcher.util.eventbus.FavoritesRecyclerChangedEvent;
import com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper;
import com.yandex.mandrik.launcher.util.layout.RecyclerSpanSizeLookup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper.getCountCeilsInRowLandscape;
import static com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper.getCountCeilsInRowPortrait;

public class AppsFavoritesFragment extends Fragment {

    private static final int RESULT_PICK_CONTACT = 8500;

    private Context context;

    private RecyclerView favoritesRecycler;
    private FavoritesListAdapter listAdapter;

    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favorites, container, false);

        context = rootView.getContext();

        final EditText editUri = (EditText) rootView.findViewById(R.id.editUri);

        favoritesRecycler = (RecyclerView) rootView.findViewById(R.id.favoritesRecyclerView);

        listAdapter = new FavoritesListAdapter(context);
        listAdapter.setHeaders(new String[] {getString(R.string.contacts), getString(R.string.favorites)});

        favoritesRecycler.setHasFixedSize(true);


        ImageView setButton = (ImageView) rootView.findViewById(R.id.settings_but);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettings(v);
            }
        });

        favoritesRecycler.setAdapter(listAdapter);
        setLayoutManagerOnRecycler();

        setOnSpinnerVisibleUris();

        editUri.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(editUri.getText())));

                    if(intent.resolveActivity(context.getPackageManager()) != null) {
                        // save uri in the file
                        SharedPreferencesHelper.addUri(context, String.valueOf(editUri.getText()));

                        setOnSpinnerVisibleUris();

                        // start activity
                        startActivity(intent);
                    } else {
                        Toast toast = Toast.makeText(context, getString(R.string.incorrect_uri) + " " + editUri.getText(), Toast.LENGTH_LONG);
                        TextView view = (TextView) toast.getView().findViewById(android.R.id.message);
                        view.setBackgroundColor(Color.DKGRAY);
                        view.setTextColor(Color.WHITE);
                        toast.show();
                    }

                    hideSoftKeyboard(editUri);
                    return true;
                }
                return false;
            }
        });

        listAdapter.updateContacts();


        return rootView;
    }

    public void setOnSpinnerVisibleUris() {

        Integer countShownUri = SharedPreferencesHelper.getCountVisibleUris(context);
        Integer countUri = SharedPreferencesHelper.getCountSavedUris(context);

        final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);

        int min = countShownUri;
        if(min > countUri) {
            min = countUri;
        }

        if(min > 0) {
            spinner.setVisibility(View.VISIBLE);

            ArrayList<String> visibleUris = SharedPreferencesHelper.getVisibleUris(context, min);

            visibleUris.add(0, "");

            ArrayAdapter<String> adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, visibleUris);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(adapter);
            spinner.setPrompt(context.getString(R.string.last_uris));

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    EditText editUri = (EditText) rootView.findViewById(R.id.editUri);
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


    public void requestPermissionAddContact() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    ContactInfo contactInfo = ContactsHelper.getContactInfoPicked(context, data);
                    if(!contactInfo.equals(null) && !listAdapter.isContainIdContact(contactInfo.id)) {
                        //listAdapter.contactsList.remove(listAdapter.contactsList.size()-1);
                        listAdapter.contactsList.add(contactInfo);
                        SharedPreferencesHelper.saveContacts(context, listAdapter.contactsList);
                        listAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        } else {
            Log.e("AppsFavoritesFragment", "Failed to pick contact");
        }
    }









    @Subscribe
    public void onFavoritesRecyclerChangedEvent(FavoritesRecyclerChangedEvent event) {
        if(event.isInstall) {
            listAdapter.addFavorite(event.appInfo);
            listAdapter.notifyDataSetChanged();
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
        setOnSpinnerVisibleUris();
    }


    @Subscribe
    public void onChangeCountMemUriEvent(ChangeCountMemUriEvent event) {

        SharedPreferencesHelper.changeCountVisibleUris(context, event.countMemUri);
        setOnSpinnerVisibleUris();
    }


    /*@Subscribe
    public void onSetFavoritesEvent(SetFavoritesEvent event) {
        listAdapter.favoriteAppsList = new ArrayList();
        listAdapter.favoriteAppsList = event.favoritesAppsList;
    }*/


    @Subscribe
    public void onChangeCountCeilsEvent(ChangeCountCeilsEvent event) {
        listAdapter.updateContacts();
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

    @Subscribe
    public void onAddContactEvent(AddContactEvent event) {
        requestPermissionAddContact();
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
        }
    }
}
