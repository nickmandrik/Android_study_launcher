package com.yandex.mandrik.launcher.listappsactivity.appsfavorities.adapter;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yandex.mandrik.launcher.R;
import com.yandex.mandrik.launcher.appdata.AppInfo;
import com.yandex.mandrik.launcher.appdata.ApplicationListManager;
import com.yandex.mandrik.launcher.appdata.ContactInfo;
import com.yandex.mandrik.launcher.util.eventbus.AddContactEvent;
import com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper;

import org.greenrobot.eventbus.EventBus;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper.changeStatusOfHideFavorites;
import static com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper.getCountCeilsInRowLandscape;
import static com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper.getCountCeilsInRowPortrait;
import static com.yandex.mandrik.launcher.util.preference.constants.SharedPreferenceConstants.APP_PREFERENCE_FAVORITES_LIST;
import static com.yandex.mandrik.launcher.util.preference.constants.SharedPreferenceConstants.COUNT_FAVORITES;

/**
 * Adapter that use the views with appList image and text.
 * Created by Nick Mandrik on 21.03.2017.
 * @author Nick Mandrik
 */

public class FavoritesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<Pair<Integer, AppInfo>> favoriteAppsList = new ArrayList<>();
    public ArrayList<ContactInfo> contactsList = new ArrayList<>();
    private String[] headers = new String[] {"Contacts", "Favorites"};
    private Context context;

    public FavoritesListAdapter(Context context) {
        this.context = context;
        SharedPreferences favoritesSettings =
                context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

        Integer countFavorites = favoritesSettings.getInt(COUNT_FAVORITES, 0);
        favoriteAppsList = new ArrayList();
        HashMap<String, AppInfo> namePackageFavorites = new HashMap();
        int countInRow = getCountCeilsInRowLandscape(context);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            countInRow = getCountCeilsInRowPortrait(context);
        }
        ApplicationListManager appManager = new ApplicationListManager(context, countInRow);
        for(AppInfo appInfo: appManager.getAppsList()) {
            namePackageFavorites.put(appInfo.getPackageName(), appInfo);
        }

        for(int i = 0; i < countFavorites; i++) {
            String packageNameApp = favoritesSettings.getString(String.valueOf(i), "none");
            if(!packageNameApp.equals("none")) {
                if(namePackageFavorites.keySet().contains(packageNameApp)) {
                    favoriteAppsList.add(new Pair(i, namePackageFavorites.get(packageNameApp)));
                }
            }
        }
    }

    private class ApplicationViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView text;

        private ApplicationViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image_id);
            text = (TextView) itemView.findViewById(R.id.text_id);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (getAppInfoById(position) != null) {
                        Intent intent = context.getPackageManager().getLaunchIntentForPackage(
                                getAppInfoById(position).
                                        getPackageName());

                        SharedPreferences appSettings =
                                context.getSharedPreferences("count_clicks_settings", Context.MODE_PRIVATE);

                        SharedPreferences.Editor e = appSettings.edit();
                        e.putInt(getAppInfoById(position).getPackageName(),
                                appSettings.getInt(getAppInfoById(position).getPackageName(), 0) + 1);
                        e.apply();

                        context.startActivity(intent);
                    } else if (getContactInfoById(position) != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" +
                                getContactInfoById(position).number));
                        context.startActivity(intent);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final int position = getAdapterPosition();
                    if (getAppInfoById(position) != null) {
                        new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.delete_favorite))
                                .setMessage(context.getString(R.string.confirm_delete_favorite) +
                                        getAppInfoById(position).getLabel() + "?")
                                .setIcon(getAppInfoById(position).getIcon())
                                .setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        removeFavorite(position);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    } else if (getContactInfoById(position) != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,
                                getContactInfoById(position).id);
                        intent.setData(uri);
                        context.startActivity(intent);
                    }
                    return true;
                }
            });

        }


    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        View line;
        TextView text;

        private HeaderViewHolder(View itemView) {
            super(itemView);
            line = itemView.findViewById(R.id.line_view);
            text = (TextView) itemView.findViewById(R.id.section);
        }
    }


    private class AddContactViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;

        private AddContactViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.image_add_contact);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if(position == 0 || position == contactsList.size() + 2) {
            return 0;
        } else if(position == 1) {
            return 3;
        } else if(position < contactsList.size() + 2) {
            return 1;
        } else if(position < contactsList.size() + favoriteAppsList.size() + 3) {
            return 2;
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case 0:
                View v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.section_header, parent, false);
                return new HeaderViewHolder(v);
            case 1:
            case 2:
                View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.layout_app_view_on_recycler, parent, false);
                return new ApplicationViewHolder(view);
            case 3:
                View add_contact = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.layout_icon_add_contact, parent, false);
                return new AddContactViewHolder(add_contact);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch(holder.getItemViewType()) {
            case 0:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                if(position == 0) {
                    headerViewHolder.text.setText(headers[0]);
                } else if(position == contactsList.size() + 2) {
                    headerViewHolder.text.setText(headers[1]);
                }
                break;
            case 1:
                ApplicationViewHolder viewHolder1 = (ApplicationViewHolder) holder;
                if(getContactInfoById(position).photoUri != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = context.getContentResolver().
                                openInputStream(getContactInfoById(position).photoUri);
                        Drawable drawable = Drawable.createFromStream(inputStream,
                                String.valueOf(getContactInfoById(position).photoUri));
                        viewHolder1.image.setImageDrawable(drawable);
                    } catch (FileNotFoundException e) {
                        viewHolder1.image.setImageResource(R.drawable.photo_contact);
                    }
                } else {
                    viewHolder1.image.setImageResource(R.drawable.photo_contact);
                }
                viewHolder1.text.setText(getContactInfoById(position).name);
                break;
            case 2:
                ApplicationViewHolder viewHolder = (ApplicationViewHolder) holder;
                viewHolder.image.setImageDrawable(getAppInfoById(position).getIcon());
                viewHolder.text.setText(getAppInfoById(position).getLabel());
                break;
            case 3:
                AddContactViewHolder contactHolder = (AddContactViewHolder) holder;
                contactHolder.icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new AddContactEvent());
                    }
                });
                break;
        }
    }


    @Override
    public int getItemCount() {
        return contactsList.size() + favoriteAppsList.size() + 3;
    }

    /**
     * Remove a RecyclerView item by AppInfo position on recycler
     */
    public void removeFavorite(int position) {
        int id = getItemViewType(position);
        switch(id) {
            case 0:
            case 1:
                break;
            case 2:
                SharedPreferences favoritesSettings =
                        context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

                SharedPreferences.Editor e = favoritesSettings.edit();
                e.putString(String.valueOf(favoriteAppsList.get(position- contactsList.size()-2).first), "none");
                e.apply();

                favoriteAppsList.remove(position-contactsList.size()-3);
                notifyItemRemoved(position);
                break;
            default:
                break;
        }
    }

    public void addFavorite(AppInfo appInfo) {
        SharedPreferences favoritesSettings =
                context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

        Integer countFavorites = favoritesSettings.getInt(COUNT_FAVORITES, 0);

        boolean isExist = false;
        for(int i = 0; i < countFavorites; i++) {
            String namePackage = favoritesSettings.getString(String.valueOf(i), "none");
            if(appInfo.getPackageName().equals(namePackage)) {
                isExist = true;
                break;
            }
        }
        Log.d("ok", String.valueOf(isExist));
        if(!isExist) {
            SharedPreferences.Editor e = favoritesSettings.edit();
            e.putString(String.valueOf(countFavorites), appInfo.getPackageName());
            e.putInt(COUNT_FAVORITES, countFavorites + 1);
            e.apply();

            favoriteAppsList.add(new Pair(countFavorites, appInfo));
        }
    }


    public void clearFavorites() {
        SharedPreferences favoritesSettings =
                context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

        Integer countFavorites = favoritesSettings.getInt(COUNT_FAVORITES, 0);

        SharedPreferences.Editor e = favoritesSettings.edit();

        for(int i = 0; i < countFavorites; i++) {
            e.putString(String.valueOf(i), "none");
        }

        e.putInt(COUNT_FAVORITES, 0);
        e.apply();

        favoriteAppsList = new ArrayList<>();
        notifyDataSetChanged();
    }

    public AppInfo getAppInfoById(int position) {
        int id = getItemViewType(position);
        switch(id) {
            case 0:
            case 1:
            case 3:
                return null;
            case 2:
                return favoriteAppsList.get(position - contactsList.size() - 3).second;
            default:
                return null;
        }
    }

    public ContactInfo getContactInfoById(int position) {
        int id = getItemViewType(position);
        switch(id) {
            case 0:
            case 2:
            case 3:
                return null;
            case 1:
                return contactsList.get(position - 2);
            default:
                return null;
        }
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }


    public void updateContacts() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            if(SharedPreferencesHelper.isExistContactList(context)) {
                contactsList = SharedPreferencesHelper.getContacts(context);
            }
            notifyDataSetChanged();
        }
    }


    public boolean isContainIdContact(String id) {
        for(ContactInfo contactInfo: contactsList) {
            if(contactInfo.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

}