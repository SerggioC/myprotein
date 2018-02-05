package com.cruz.sergio.myproteinpricechecker.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.internal.SnackbarContentLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cruz.sergio.myproteinpricechecker.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import info.guardianproject.netcipher.NetCipher;

import static android.content.Context.MODE_PRIVATE;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.PREFERENCE_FILE_NAME;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.scale;
import static com.cruz.sergio.myproteinpricechecker.helper.MPUtils.showCustomToast;

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
    public static Snackbar noNetworkSnackBar = null;
    public static int NET_TIMEOUT = 11000; // milliseconds (11 seconds)
    public static int STATUS_OK = 0;
    public static int TIMEOUT = 1;
    public static int MALFORMED_URL = 2;
    public static int STATUS_NOT_OK = 3;
    public static int IOEXCEPTION = 4;
    public static int UNSUPPORTED_MIME_TYPE = 5;
    static BroadcastReceiver BCReceiver = null;
    static Snackbar.SnackbarLayout snack_layout;

    public static void UnregisterBroadcastReceiver(Activity mActivity) {
        if (BCReceiver != null) {
            LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(BCReceiver);
            BCReceiver = null;
            noNetworkSnackBar = null;
            Log.i("Sergio>", "NetworkUtils: UnregisterBroadcastReceiver");
        }
    }

    public static final Boolean createBroadcast(final Activity mActivity) {
        if (BCReceiver == null) {
            makeNoNetworkSnackBar(mActivity);
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            BCReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle extras = intent.getExtras();
                    NetworkInfo info = extras.getParcelable("networkInfo");
                    NetworkInfo.State state = info != null ? info.getState() : null;
                    if (noNetworkSnackBar != null) {
                        return;
                    }
                    if (state == NetworkInfo.State.CONNECTED) {
                        noNetworkSnackBar.dismiss();
                    } else if (state != NetworkInfo.State.CONNECTED) {
                        noNetworkSnackBar.show();
                    }
                }
            };
            try {
                mActivity.registerReceiver(BCReceiver, intentFilter);
                Log.i("Sergio>", "NetworkUtils: registerReceiver");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }


    public static void makeNoNetworkSnackBar(Activity mActivity) {
        noNetworkSnackBar = Snackbar.make(mActivity.findViewById(android.R.id.content), "No Network Connection", Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(Color.RED)
                .setAction("Dismiss", v -> noNetworkSnackBar.dismiss());

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

    public static void redrawNoNetworkSnackBar(Activity mActivity) {
        if (noNetworkSnackBar != null) {
            if (noNetworkSnackBar.isShown()) {
                noNetworkSnackBar.dismiss();
            }
            noNetworkSnackBar = null;
            makeNoNetworkSnackBar(mActivity);
            noNetworkSnackBar.show();
        } else {
            makeNoNetworkSnackBar(mActivity);
            noNetworkSnackBar.show();
        }
    }

    public static boolean hasActiveNetworkConnection(Activity mActivity) {
        ConnectivityManager connManager = (ConnectivityManager)
                mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable();
        if (isConnected) {
            if (noNetworkSnackBar != null && noNetworkSnackBar.isShownOrQueued())
                noNetworkSnackBar.dismiss(); // Tem network connection

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

    public static Document getHTMLDocument_with_NetCipher(String url) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            HttpsURLConnection netCipherconnection = NetCipher.getHttpsURLConnection(url);

            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            SSLSocketFactory noSSLv3Factory = new TLSSocketFactory(sslContext.getSocketFactory());
            netCipherconnection.setSSLSocketFactory(noSSLv3Factory);
            netCipherconnection.connect();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(netCipherconnection.getInputStream()));
            String stringHTML;
            while ((stringHTML = bufferedReader.readLine()) != null)
                stringBuilder.append(stringHTML);
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return Jsoup.parse(String.valueOf(stringBuilder));
    }

    public static void CheckGooglePlayServices(Context context) {
        // This method should run on the main UI thread!
        SharedPreferences sharedPrefEditor = context.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
        boolean gms_installed = sharedPrefEditor.getBoolean("gms_installed", context.getResources().getBoolean(R.bool.gms_installed));
        if (gms_installed) return;
        try {
            ProviderInstaller.installIfNeeded(context);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play services is out of date, disabled, etc.
            // Prompt the user to install/update/enable Google Play services.
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            if (googleAPI.isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
                googleAPI.showErrorDialogFragment((Activity) context, 1, 2, new DialogInterface.OnCancelListener() {
                    /**
                     * This method will be invoked when the dialog is canceled.
                     * @param dialog the dialog that was canceled will be passed into the method
                     */
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        showCustomToast(context, "This app might not function properly until you install GooglePlayServices",
                                R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
                    }
                });
            }
            return;

        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates a non-recoverable error; the ProviderInstaller is not able
            // to install an up-to-date Provider.
            showCustomToast(context, "GooglePlayServices not available.\n" +
                            "This app might not function properly until you install GooglePlayServices.",
                    R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
            //"https://www.apkmirror.com/apk/google-inc/google-play-store/google-play-store-8-8-12-release/"
            return;
        }

        // If this is reached, you know that the provider was already up-to-date,
        // or was successfully updated.
        sharedPrefEditor.edit().putBoolean("gms_installed", true);

    }


    public static class TLSSocketFactory extends SSLSocketFactory {

        /*
         * Utility methods
         */
        static String TLS_v1_1 = "TLSv1.1";
        static String TLS_v1_2 = "TLSv1.2";
        private SSLSocketFactory internalSSLSocketFactory;

        public TLSSocketFactory(SSLSocketFactory delegate) throws KeyManagementException, NoSuchAlgorithmException {
            internalSSLSocketFactory = delegate;
        }

        private static Socket enableTLSOnSocket(Socket socket) {
            if (socket != null && (socket instanceof SSLSocket) && isTLSServerEnabled((SSLSocket) socket)) { // skip the fix if server doesn't provide there TLS version
                ((SSLSocket) socket).setEnabledProtocols(new String[]{TLS_v1_1, TLS_v1_2});
            }
            return socket;
        }

        private static boolean isTLSServerEnabled(SSLSocket sslSocket) {
            System.out.println("__prova__ :: " + sslSocket.getSupportedProtocols().toString());
            for (String protocol : sslSocket.getSupportedProtocols()) {
                if (protocol.equals(TLS_v1_1) || protocol.equals(TLS_v1_2)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return internalSSLSocketFactory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return internalSSLSocketFactory.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
        }
    }

}
