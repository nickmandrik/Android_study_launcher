package com.yandex.mandrik.launcher.listappsactivity.appsfavorities.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yandex.mandrik.launcher.R;
import com.yandex.mandrik.launcher.appdata.AppInfo;
import com.yandex.mandrik.launcher.appdata.ContactInfo;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (getAppInfoById(position) != null) {
                        Intent intent = context.getPackageManager().getLaunchIntentForPackage(
                                getAppInfoById(position).
                                        getPackageName());
                        getAppInfoById(position).setCountClicks(
                                getAppInfoById(position)
                                        .getCountClicks() + 1
                        );
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
            case 1:
                break;
            case 2:
                SharedPreferences favoritesSettings =
                        context.getSharedPreferences(APP_PREFERENCE_FAVORITES_LIST, Context.MODE_PRIVATE);

                SharedPreferences.Editor e = favoritesSettings.edit();
                e.putString(String.valueOf(favoriteAppsList.get(position- contactsList.size()-2).first), "none");
                e.apply();

                favoriteAppsList.remove(position-contactsList.size()-2);
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

            notifyItemInserted(favoriteAppsList.size() + contactsList.size() + 2);
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