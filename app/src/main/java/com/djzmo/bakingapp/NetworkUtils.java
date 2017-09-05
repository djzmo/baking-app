package com.djzmo.bakingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {

    public static final String DATA_SOURCE_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    public static boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    protected static URL buildCommonUrl(Context c, String urlString) {
        Uri uri = Uri.parse(urlString).buildUpon()
                .build();
        URL url = null;

        try {
            url = new URL(uri.toString());
        }
        catch(MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

}
