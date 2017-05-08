package com.yandex.mandrik.launcher.listappsactivity.appsfavorities.recycler.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yandex.mandrik.launcher.R;
import com.yandex.mandrik.launcher.listappsactivity.appdata.AppInfo;

import java.io.File;
import java.util.ArrayList;

import static com.yandex.mandrik.launcher.util.preference.constants.LauncherConstants.APP_PREFERENCE_FAVORITES_LIST;
import static com.yandex.mandrik.launcher.util.preference.constants.LauncherConstants.COUNT_FAVORITES;

/**
 * Adapter that use the views with appList image and text.
 * Created by Nick Mandrik on 21.03.2017.
 * @author Nick Mandrik
 */

public class FavoritesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<Pair<Integer, AppInfo>> favoriteAppsList = new ArrayList();
    private String header = "Favorites";
    private Context context;

    public FavoritesListAdapter(Context context) {
        this.context = context;

        SharedPreferences favoritesSettings =
                context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

        Integer countFavorites = favoritesSettings.getInt(COUNT_FAVORITES, 0);
        for(int i = 0; i < countFavorites; i++) {
            String packageNameApp = favoritesSettings.getString(String.valueOf(i), "none");
            if(!packageNameApp.equals("none")) {
                Intent intent = new Intent();
                intent.setPackage(packageNameApp);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ResolveInfo ri = context.getPackageManager().resolveActivity(intent, 0);

                AppInfo app = new AppInfo();
                app.setLabel(ri.loadLabel(context.getPackageManager()).toString());
                app.setPackageName(ri.activityInfo.packageName);
                app.setIcon(ri.activityInfo.loadIcon(context.getPackageManager()));
                String appFile = ri.activityInfo.applicationInfo.sourceDir;
                app.setLastModified(new File(appFile).lastModified());

                favoriteAppsList.add(new Pair(i, app));
            }
        }
    }

    public class ApplicationViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image;
        protected TextView text;

        public ApplicationViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image_id);
            text = (TextView) itemView.findViewById(R.id.text_id);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        protected View line;
        protected TextView text;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            line = itemView.findViewById(R.id.line_view);
            text = (TextView) itemView.findViewById(R.id.section);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return 0;
        } else if(position < favoriteAppsList.size() + 1) {
            return 1;
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case 0:
                View v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.section_header, parent, false);
                HeaderViewHolder holder = new HeaderViewHolder(v);
                return holder;
            case 1:
                View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.layout_app_view_on_recycler, parent, false);
                ApplicationViewHolder viewHolder = new ApplicationViewHolder(view);
                return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch(holder.getItemViewType()) {
            case 0:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                if(position == 0) {
                    headerViewHolder.text.setText(header);
                }
                break;
            case 1:
                ApplicationViewHolder viewHolder = (ApplicationViewHolder) holder;
                viewHolder.image.setImageDrawable(getAppInfoById(position).getIcon());
                viewHolder.text.setText(getAppInfoById(position).getLabel());
                break;
        }
    }


    @Override
    public int getItemCount() {
        return favoriteAppsList.size() + 1;
    }

    /**
     * Remove a RecyclerView item by AppInfo position on recycler
     */
    public void removeFavorite(int position) {
        int id = getItemViewType(position);
        switch(id) {
            case 0:
                break;
            case 1:
                SharedPreferences favoritesSettings =
                        context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

                SharedPreferences.Editor e = favoritesSettings.edit();
                e.putString(String.valueOf(favoriteAppsList.get(position-1).first), "none");
                e.apply();

                favoriteAppsList.remove(position-1);
                notifyItemRemoved(position);
                break;
            default:
                break;
        }
    }

    public void addFavorite(AppInfo appInfo) {
        if(!favoriteAppsList.contains(appInfo)) {
            SharedPreferences favoritesSettings =
                    context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

            Integer countFavorites = favoritesSettings.getInt(COUNT_FAVORITES, 0);

            SharedPreferences.Editor e = favoritesSettings.edit();
            e.putString(String.valueOf(countFavorites), appInfo.getPackageName());
            e.putInt(COUNT_FAVORITES, countFavorites + 1);
            e.apply();

            favoriteAppsList.add(new Pair(countFavorites, appInfo));

            notifyItemInserted(favoriteAppsList.size());
        }
    }


    public void clearFavorites() {
        SharedPreferences favoritesSettings =
                context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

        Integer countFavorites = favoritesSettings.getInt(COUNT_FAVORITES, 0);

        SharedPreferences.Editor e = favoritesSettings.edit();

        for(int i = 0; i < countFavorites; i++) {
            e.putString(String.valueOf(countFavorites), "none");
        }

        e.putInt(COUNT_FAVORITES, 0);
        e.apply();

        favoriteAppsList = new ArrayList();
        notifyDataSetChanged();
    }

    public AppInfo getAppInfoById(int position) {
        int id = getItemViewType(position);
        switch(id) {
            case 0:
                return null;
            case 1:
                return favoriteAppsList.get(position - 1).second;
            default:
                return null;
        }
    }

    public void setHeader(String header) {
        this.header = header;
    }
}