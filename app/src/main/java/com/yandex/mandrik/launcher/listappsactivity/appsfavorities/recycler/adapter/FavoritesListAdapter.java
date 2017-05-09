package com.yandex.mandrik.launcher.listappsactivity.appsfavorities.recycler.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yandex.mandrik.launcher.R;
import com.yandex.mandrik.launcher.listappsactivity.appdata.AppInfo;
import com.yandex.mandrik.launcher.listappsactivity.appdata.ContactInfo;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.support.v4.content.res.ResourcesCompat.getDrawable;
import static com.yandex.mandrik.launcher.util.preference.constants.LauncherConstants.APP_PREFERENCE_FAVORITES_LIST;
import static com.yandex.mandrik.launcher.util.preference.constants.LauncherConstants.COUNT_FAVORITES;

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
    }

    private class ApplicationViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView text;

        private ApplicationViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image_id);
            text = (TextView) itemView.findViewById(R.id.text_id);
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

    @Override
    public int getItemViewType(int position) {
        if(position == 0 || position == contactsList.size() + 1) {
            return 0;
        } else if(position < contactsList.size() + 1) {
            return 1;
        } else if(position < contactsList.size() + favoriteAppsList.size() + 2) {
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
                } else if(position == contactsList.size() + 1) {
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
        }
    }


    @Override
    public int getItemCount() {
        return contactsList.size() + favoriteAppsList.size() + 2;
    }

    /**
     * Remove a RecyclerView item by AppInfo position on recycler
     */
    public void removeFavorite(int position) {
        int id = getItemViewType(position);
        switch(id) {
            case 0:
            case 2:
                break;
            case 1:
                SharedPreferences favoritesSettings =
                        context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

                SharedPreferences.Editor e = favoritesSettings.edit();
                e.putString(String.valueOf(favoriteAppsList.get(position- contactsList.size()-2).first), "none");
                e.apply();

                favoriteAppsList.remove(position- contactsList.size()-2);
                notifyItemRemoved(position);
                break;
            default:
                break;
        }
    }

    public void addFavorite(AppInfo appInfo) {
        boolean isExist = false;
        for(Pair<Integer, AppInfo> favAppInfo: favoriteAppsList) {
            if(favAppInfo.second.equals(appInfo)) {
                isExist = true;
                break;
            }
        }
        if(!isExist) {
            SharedPreferences favoritesSettings =
                    context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

            Integer countFavorites = favoritesSettings.getInt(COUNT_FAVORITES, 0);

            SharedPreferences.Editor e = favoritesSettings.edit();
            e.putString(String.valueOf(countFavorites), appInfo.getPackageName());
            e.putInt(COUNT_FAVORITES, countFavorites + 1);
            e.apply();

            favoriteAppsList.add(new Pair(countFavorites, appInfo));

            notifyItemInserted(favoriteAppsList.size() + contactsList.size() + 1);
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
                return null;
            case 2:
                return favoriteAppsList.get(position - contactsList.size() - 2).second;
            default:
                return null;
        }
    }

    public ContactInfo getContactInfoById(int position) {
        int id = getItemViewType(position);
        switch(id) {
            case 0:
            case 2:
                return null;
            case 1:
                return contactsList.get(position - 1);
            default:
                return null;
        }
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }




}