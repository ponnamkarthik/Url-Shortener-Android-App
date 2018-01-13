package io.github.ponnamkarthik.urlshortner.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import retrofit2.Response;

/**
 * Created by ponna on 05-08-2017.
 */

public class Network {

    /**
     * Check Connectivity
     * @param context
     * @param roamingOK
     * @return
     */
    public static boolean hasConnectivity(Context context, boolean roamingOK) {

        boolean hasConnectivity = true;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        try {

            if(info.getTypeName() == "WIFI") {

            }

            if(info.getTypeName() == "MOBILE" ) {
                if(info.getSubtypeName() == "EDGE") {
                    //3G Network
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        hasConnectivity = info != null && (info.isConnected() || (roamingOK && info.isRoaming()));

        return hasConnectivity;

    }

}
