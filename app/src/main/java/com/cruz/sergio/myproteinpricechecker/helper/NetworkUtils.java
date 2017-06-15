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
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cruz.sergio.myproteinpricechecker.R;

import java.io.IOException;

import static com.cruz.sergio.myproteinpricechecker.MainActivity.BC_Registered;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.scale;
import static com.cruz.sergio.myproteinpricechecker.R.id.snackbar_text;

/*****
 *
 *  Project MyProteinPriceChecker
 *  Package com.cruz.sergio.myproteinpricechecker.helper
 *  Created by Sergio on 24/02/2017 23:23
 *
 ******/

public class NetworkUtils {
    private static final String PING_URL = "www.myprotein.com";
    public static BroadcastReceiver BCReceiver = null;
    public static Snackbar noNetworkSnackBar = null;
    static Snackbar.SnackbarLayout snack_layout;

    public static final void UnregisterBroadcastReceiver(Activity mActivity) {
        if (BCReceiver != null) {
            LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(BCReceiver);
            //showCustomSlimToast(mActivity, "Unregistering Broadcast Receiver", Toast.LENGTH_SHORT);
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
                    NetworkInfo.State state = info.getState();
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
        Toast theCustomToast = new Toast(cActivity);
        theCustomToast.setGravity(Gravity.CENTER, 0, 0);
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
                ((SnackbarContentLayout) snack_layout.findViewById(snackbar_text).getParent()).addView(imageView, 0);
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

    public static boolean ping() throws InterruptedException, IOException {
        String command = "ping -c 1 " + PING_URL;
        return (Runtime.getRuntime().exec(command).waitFor() == 0);
    }
}
