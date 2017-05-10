package com.yandex.mandrik.launcher.appdata;

/**
 * Created by Home on 09.05.2017.
 */

public class UriInfo {
    public Long time = new Long(0);
    public String uri;

    public UriInfo(String uri, Long time) {
        this.time = time;
        this.uri = uri;
    }

    public UriInfo(String uri) {
        this.uri = uri;
    }
}
