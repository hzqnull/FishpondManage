package com.meizhuo.fishpondmanage.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by 何子强 on 2018/12/12
 */
public class NetworkUtils {

    /**
     * 判断网络连接状态
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = null;
        if (context != null) {
            connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo networkInfo= null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if(networkInfo == null){
            return false;
        }
        return networkInfo.isConnected();
    }
}
