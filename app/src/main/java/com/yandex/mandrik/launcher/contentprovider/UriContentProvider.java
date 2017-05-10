package com.yandex.mandrik.launcher.contentprovider;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.yandex.mandrik.launcher.util.preference.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static android.support.v4.content.PermissionChecker.checkCallingPermission;

public class UriContentProvider extends ContentProvider {

    private final UriMatcher uriMatcher;

    interface UriContract {
        final String FIELD_VALUE = "value";
    }

    public UriContentProvider() {
        uriMatcher = new UriMatcher(0);
        uriMatcher.addURI("com.yandex.mandrik.launcher", "uri/last", 1);
        uriMatcher.addURI("com.yandex.mandrik.launcher", "uri/all", 2);
        uriMatcher.addURI("com.yandex.mandrik.launcher", "uri/last_day", 3);
        uriMatcher.addURI("com.yandex.mandrik.launcher", "uri", 4);
    }


    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        List<String> result = new ArrayList<>();
        switch (uriMatcher.match(uri)) {
            case 1:
                String[] listUri = SharedPreferencesHelper.getUris(getContext());
                result.add(listUri[listUri.length-1]);
                break;
            case 2:
                if (getContext() != null &&
                        checkCallingPermission(getContext(),
                                "com.yandex.mandrik.launcher.permission.READ_ALL",
                                null) == PERMISSION_GRANTED) {
                    result.addAll(Arrays.asList(SharedPreferencesHelper.getUris(getContext())));
                }
                break;
            case 3:
                result.addAll(Arrays.asList(SharedPreferencesHelper.getLastDayUris(getContext())));
                break;
            default:
                result = null;
                break;

        }

        final List<String> returnValue = result;
        if (returnValue != null) {
            Cursor cursor = new MatrixCursor(new String[]{UriContract.FIELD_VALUE}) {{
                for (int i = 0; i < returnValue.size(); i++) {
                    newRow().add(returnValue.get(i));
                }
            }};
            return cursor;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if(uriMatcher.match(uri) == 4) {
            String name = values.getAsString("value");
            SharedPreferencesHelper.addUri(getContext(), name);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return -1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
