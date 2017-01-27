package com.cruz.sergio.myproteinpricechecker.helper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cruz.sergio.myproteinpricechecker.R;

import java.io.InputStream;


public class CreateCardView {

    public static void create(final Activity mActivity, final LinearLayout resultsLinearLayout, String productID, String productTitleStr, String productHref, String productPrice, String imgURL, String pptListStr) {


        //criar novo cardview
        final CardView cardview = new CardView(mActivity);
        cardview.setLayoutParams(new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,   // width
                CardView.LayoutParams.WRAP_CONTENT)); // height
        cardview.setPreventCornerOverlap(true);
        //int pixels = (int) (dips * scale + 0.5f);
        final float scale = mActivity.getResources().getDisplayMetrics().density;
        int lr_dip = (int) (6 * scale + 0.5f);
        int tb_dip = (int) (8 * scale + 0.5f);
        cardview.setRadius((int) (2 * scale + 0.5f));
        cardview.setCardElevation((int) (2 * scale + 0.5f));
        cardview.setContentPadding(lr_dip, tb_dip, lr_dip, tb_dip);
        cardview.setUseCompatPadding(true);

        ImageView prodImgView = new ImageView(mActivity);
        if (imgURL.contains(".jpg") || imgURL.contains(".bmp") || imgURL.contains(".png")) {
            new DownloadImageTask(prodImgView).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imgURL);
        } else {
            //failed getting product image
            //TODO imagem em branco produto não encontrado
        }

        // Adicionar a imagem ao CardView
        cardview.addView(prodImgView);

        int cv_color = ContextCompat.getColor(mActivity, R.color.cardsColor);
        cardview.setCardBackgroundColor(cv_color);

        // Add cardview to Search Result layout
        resultsLinearLayout.addView(cardview);

        // criar novo Textview
        final TextView textView = new TextView(mActivity);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,   //largura
                LinearLayout.LayoutParams.WRAP_CONTENT)); //altura

        //Adicionar o texto com o resultado
        textView.setText(productID + "\n" + productTitleStr + "\n" + productHref + "\n" + productPrice + "\n" + pptListStr);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        // add the textview to the cardview
        cardview.addView(textView);

    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}