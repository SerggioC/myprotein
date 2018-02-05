package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cruz.sergio.myproteinpricechecker.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.SHORT;
import static java.text.DateFormat.getDateTimeInstance;
import static java.text.DateFormat.getTimeInstance;

/**
 * Created by Sergio on 08/01/2018.
 */

public class MPUtils {
   public static String getMillisecondsToDate(long milliseconds) {
      long timeDif = System.currentTimeMillis() - milliseconds;

      if (timeDif < 60_000) { // há menos de 60 segundos atrás
         return "Now";
      } else if (timeDif >= 60_000 && timeDif <= 3_600_000) { // uma hora atrás
         return TimeUnit.MILLISECONDS.toMinutes(timeDif) + " " + "Minutes ago";

      } else if (timeDif > 3_600_000 && timeDif < 7_200_000) { // Dentro de 1h - 2hr
         return TimeUnit.MILLISECONDS.toHours(timeDif) + " " + "Hour ago";

      } else if (timeDif >= 7_200_000 && timeDif <= 86_400_000) { // Dentro do dia de hoje até 24h atrás
         return TimeUnit.MILLISECONDS.toHours(timeDif) + " " + "Hours ago";

      } else if (timeDif > 86_400_000 && timeDif <= 172_800_000) { // Ontem 24 a 48h
         DateFormat df = getTimeInstance(SHORT);
         Date resultDate = new Date(milliseconds);
         return "Yesterday" + " " + df.format(resultDate);

      } else {
         String pattern;
         if (timeDif < TimeUnit.DAYS.toMillis(365L)) {
            pattern = "dd MMM kk:mm";
         } else {
            pattern = "dd MMM yy kk:mm";
         }
         java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern); // dia Mês 14:55 p.ex.
         DateFormat dateFormat = getDateTimeInstance(SHORT, SHORT);
         Date resultdate = new Date(milliseconds);
         return sdf.format(resultdate);
      }

   }

   public static void showCustomSlimToast(Context context, String toastText, int duration) {

      // Alternative Layout inflaters
      // Activity mActivity;
      // LayoutInflater inflater = mActivity.getLayoutInflater();
      // inflater.inflate(R.layout.custom_toast, (ViewGroup) mActivity.findViewById(R.id.toast_layout_root));
      //  --- ou ---
      // LayoutInflater inflater = LayoutInflater.from(context);
      // => igual a context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      //


      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View layout = inflater.inflate(R.layout.slim_toast, null);

      ((TextView) layout.findViewById(R.id.slimtoast)).setText(toastText);
      Toast customToast = new Toast(context);
      customToast.setGravity(Gravity.CENTER, 0, 0);
      customToast.setDuration(duration);
      customToast.setView(layout);
      customToast.show();
   }

   public static void showCustomToast(Context context, String toastText, int icon_RID, int text_color_RID, int duration) {

      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View layout = inflater.inflate(R.layout.custom_toast, null);

      TextView text = layout.findViewById(R.id.toast_layout_text);
      text.setText(toastText);
      text.setTextColor(ContextCompat.getColor(context, text_color_RID));
      ImageView imageV = layout.findViewById(R.id.toast_img);
      imageV.setImageResource(icon_RID);
      Toast theCustomToast = new Toast(context);
      theCustomToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
      theCustomToast.setDuration(duration);
      theCustomToast.setView(layout);
      theCustomToast.show();
   }

}
