package com.yandex.mandrik.launcher.appdata;

import android.net.Uri;

/**
 * Created by Home on 09.05.2017.
 */

public class ContactInfo {
    public String name = "";
    public Uri photoUri;
    public String number;
    public String id;

    public ContactInfo(String id, String name, String number, Uri photoUri) {
        this.photoUri = photoUri;
        this.name = name;
        this.number = number;
        this.id = id;
    }
}
