package com.cruz.sergio.myproteinpricechecker.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
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

    public static final void createBroadcast(final Activity mActivity) {
        makeNoNetworkSnackBar(mActivity);

        BCReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                NetworkInfo info = extras.getParcelable("networkInfo");
                NetworkInfo.State state = info.getState();

                if (noNetworkSnackBar.isShown())
                    noNetworkSnackBar.dismiss();
                if (state != NetworkInfo.State.CONNECTED)
                    noNetworkSnackBar.show();
                Log.w("Sergio>", this + "onReceive:\nnoNetworkSnackBarisShown()=\n" + noNetworkSnackBar.isShown());
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mActivity.registerReceiver(BCReceiver, intentFilter);
    }

    public static final void makeNoNetworkSnackBar(Activity mActivity) {
        noNetworkSnackBar = Snackbar.make(mActivity.getWindow().findViewById(android.R.id.content), "No Network Connection", Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(Color.RED)
                .setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        noNetworkSnackBar.dismiss();
                    }
                });
    }

    public static boolean hasActiveNetworkConnection(Activity mActivity) {
        ConnectivityManager connManager = (ConnectivityManager)
                mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable();
        if (isConnected) {
            if (noNetworkSnackBar != null && noNetworkSnackBar.isShownOrQueued()) noNetworkSnackBar.dismiss(); // Tem network connection
            try {
                if (ping()) {
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

    private static boolean ping() throws InterruptedException, IOException {
        String command = "ping -c 1 " + PING_URL;
        return (Runtime.getRuntime().exec(command).waitFor() == 0);
    }
}
