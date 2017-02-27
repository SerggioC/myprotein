package com.cruz.sergio.myproteinpricechecker.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/*****
 *
 *  Project MyProteinPriceChecker 
 *  Package com.cruz.sergio.myproteinpricechecker.helper
 *  Created by Sergio on 24/02/2017 23:23
 *
 ******/

public class NetworkUtils {
    private static final String PING_URL = "www.myprotein.com";
    public static Snackbar noNetworkSnackBar;
    public static BroadcastReceiver BCReceiver;

    public static final void UnregisterBroadcastReceiver(Activity mActivity) {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(BCReceiver);
        Toast.makeText(mActivity, "Unregistering Broadcast Receiver", Toast.LENGTH_SHORT).show();
    }

    public static void createBroadcast(final Activity mActivity) {
        noNetworkSnackBar = Snackbar.make(mActivity.findViewById(android.R.id.content), "No Network Connection", Snackbar.LENGTH_INDEFINITE);

        BCReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                NetworkInfo info = extras.getParcelable("networkInfo");
                NetworkInfo.State state = info.getState();

                Log.i("Sergio>>>", "onReceive: noNetworkSnackBar.isShownOrQueued() " + noNetworkSnackBar.isShown());
                if (noNetworkSnackBar.isShown()) noNetworkSnackBar.dismiss();
                if (state == NetworkInfo.State.CONNECTED) {
                    Toast toast1 = Toast.makeText(mActivity, "Connected to Network", Toast.LENGTH_SHORT);
                    toast1.show();
                } else {
                    noNetworkSnackBar.show();
                    Log.i("Sergio>>>", "onReceive: noNetworkSnackBar.isShown() " + noNetworkSnackBar.isShown());

                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mActivity.registerReceiver(BCReceiver, intentFilter);
    }

    public static boolean hasActiveNetworkConnection(Activity mActivity) {
        ConnectivityManager connManager = (ConnectivityManager)
                mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable();
        if (isConnected) {
            if (noNetworkSnackBar != null && noNetworkSnackBar.isShownOrQueued()) noNetworkSnackBar.dismiss(); // Tem network connection
            try {
                if (ping(PING_URL)) {
                    return true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private static boolean ping(String hostAddr) throws InterruptedException, IOException {
        String command = "ping -c 1 " + hostAddr;
        return (Runtime.getRuntime().exec(command).waitFor() == 0);
    }
}
