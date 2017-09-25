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
import android.support.design.internal.SnackbarContentLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cruz.sergio.myproteinpricechecker.R;

import java.io.IOException;
import java.net.InetAddress;

import static com.cruz.sergio.myproteinpricechecker.MainActivity.BC_Registered;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.scale;

/*****
 *
 *  Project MyProteinPriceChecker
 *  Package com.cruz.sergio.myproteinpricechecker.helper
 *  Created by Sergio on 24/02/2017 23:23
 *
 ******/

public class NetworkUtils {
    public static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
    private static final String PING_URL = "www.google.com";
    public static BroadcastReceiver BCReceiver = null;
    public static Snackbar noNetworkSnackBar = null;
    public static int NET_TIMEOUT = 11000; // milliseconds (11 seconds)
    public static int STATUS_OK = 0;
    public static int TIMEOUT = 1;
    public static int MALFORMED_URL = 2;
    public static int STATUS_NOT_OK = 3;
    public static int IOEXCEPTION = 4;
    public static int UNSUPPORTED_MIME_TYPE = 5;
    static Snackbar.SnackbarLayout snack_layout;

    public static void UnregisterBroadcastReceiver(Activity mActivity) {
        if (BCReceiver != null) {
            LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(BCReceiver);
            BCReceiver = null;
            BC_Registered = false;
            Log.i("Sergio>", "NetworkUtils: UnregisterBroadcastReceiver");
        }
    }

    public static final Boolean createBroadcast(final Activity mActivity) {
        if (!BC_Registered && BCReceiver == null) {
            makeNoNetworkSnackBar(mActivity);
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            BCReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle extras = intent.getExtras();
                    NetworkInfo info = extras.getParcelable("networkInfo");
                    NetworkInfo.State state = info != null ? info.getState() : null;
                    if (state == NetworkInfo.State.CONNECTED) {
                        noNetworkSnackBar.dismiss();
                    } else if (state != NetworkInfo.State.CONNECTED) {
                        noNetworkSnackBar.show();
                    }
                }
            };
            try {
                mActivity.registerReceiver(BCReceiver, intentFilter);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public static void showCustomSlimToast(Activity cActivity, String toastText, int duration) {
        LayoutInflater inflater = cActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.slim_toast, (LinearLayout) cActivity.findViewById(R.id.slim_toast_root));
        TextView text = (TextView) layout.findViewById(R.id.slimtoast);
        text.setText(toastText);
        Toast customToast = new Toast(cActivity);
        customToast.setGravity(Gravity.CENTER, 0, 0);
        customToast.setDuration(duration);
        customToast.setView(layout);
        customToast.show();
    }

    public static void showCustomToast(Activity cActivity, String toastText, int icon_RID, int text_color_RID, int duration) {
        LayoutInflater inflater = cActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) cActivity.findViewById(R.id.toast_layout_root));
        TextView text = (TextView) layout.findViewById(R.id.toast_layout_text);
        text.setText(toastText);
        text.setTextColor(ContextCompat.getColor(cActivity, text_color_RID));
        ImageView imageV = (ImageView) layout.findViewById(R.id.toast_img);
        imageV.setImageResource(icon_RID);
        Toast theCustomToast = new Toast(cActivity);
        theCustomToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        theCustomToast.setDuration(duration);
        theCustomToast.setView(layout);
        theCustomToast.show();
    }

    public static void makeNoNetworkSnackBar(Activity mActivity) {
        if (noNetworkSnackBar == null) {
            noNetworkSnackBar = Snackbar.make(mActivity.findViewById(android.R.id.content), "No Network Connection", Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(Color.RED)
                    .setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            noNetworkSnackBar.dismiss();
                        }
                    });

            snack_layout = (Snackbar.SnackbarLayout) noNetworkSnackBar.getView();
            if (snack_layout != null) {
                ImageView imageView = new ImageView(mActivity);
                imageView.setImageResource(R.mipmap.offline);
                int width = (int) (40 * scale + 0.5f);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
                params.gravity = Gravity.CENTER_VERTICAL;
                imageView.setLayoutParams(params);
                snack_layout.setForegroundGravity(Gravity.CENTER_VERTICAL);
                ((SnackbarContentLayout) snack_layout.findViewById(R.id.snackbar_text).getParent()).addView(imageView, 0);
            }
        }
    }

    public static boolean hasActiveNetworkConnection(Activity mActivity) {
        ConnectivityManager connManager = (ConnectivityManager)
                mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable();
        if (isConnected) {
            if (noNetworkSnackBar != null && noNetworkSnackBar.isShownOrQueued()) noNetworkSnackBar.dismiss(); // Tem network connection
//            try {
//                InetAddress inet = InetAddress.getByName(PING_URL);
//                Boolean isOnline = inet.isReachable(3000); // Timeout 5000 ms
//                return isOnline;
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            }
        }
        return isConnected;
    }

    // unreliable ping methods on android...
    public static boolean ping() throws InterruptedException, IOException {
//        String command = "ping -c 1 " + PING_URL;
//        int ping = Runtime.getRuntime().exec(command).waitFor();
//        Log.i("Sergio>", " ping = " + ping);
//        return (ping == 0);

        Boolean isOnline;

        try {
            InetAddress inet = InetAddress.getByName(PING_URL);
            isOnline = inet.isReachable(5000); // Timeout 5000 ms
            Log.i("Sergio>", " ping\nisOnline= " + isOnline);
        } catch (IOException e) {
            e.printStackTrace();
            isOnline = false;
        }

        return isOnline;

    }


}
