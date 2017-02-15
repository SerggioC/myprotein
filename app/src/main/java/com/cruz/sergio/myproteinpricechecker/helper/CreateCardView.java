package com.cruz.sergio.myproteinpricechecker.helper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cruz.sergio.myproteinpricechecker.R;

import java.io.InputStream;


public class CreateCardView {

    public static void create(final Activity mActivity, final String productID, String productTitleStr, String productHref, String productPrice, String imgURL, String pptListStr) {

        ListView linearLayoutResults = (ListView) mActivity.findViewById(R.id.results);

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

        LinearLayout ll_vertical_root = new LinearLayout(mActivity);
        ll_vertical_root.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ll_vertical_root.setOrientation(LinearLayout.VERTICAL);

        TextView titleTextView = new TextView(mActivity);
        titleTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,   //largura
                LinearLayout.LayoutParams.WRAP_CONTENT)); //altura
        titleTextView.setGravity(Gravity.LEFT);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        titleTextView.setText(productTitleStr);

        ll_vertical_root.addView(titleTextView);

        LinearLayout ll_horizontal_details = new LinearLayout(mActivity);
        ll_horizontal_details.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ll_horizontal_details.setOrientation(LinearLayout.HORIZONTAL);

        ll_vertical_root.addView(ll_horizontal_details);

        ImageView productImageView = new ImageView(mActivity);
        LinearLayout.LayoutParams imageViewLayoutParams = new LinearLayout.LayoutParams(300, 300);
        productImageView.setLayoutParams(imageViewLayoutParams);
        productImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Abrir imagem maior
                Toast.makeText(mActivity, "Abrir imagem em 600x600 ou largura máxima do ecrã" + "\n" + productID , Toast.LENGTH_SHORT).show();
            }
        });

        if (imgURL.contains(".jpg") || imgURL.contains(".bmp") || imgURL.contains(".png")) {

            Glide.with(mActivity).load(imgURL).into(productImageView);
            //new DownloadImageTask(productImageView).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, imgURL);
        } else {
            //failed getting product image
            //TODO imagem em branco produto não encontrado
        }

        ll_horizontal_details.addView(productImageView);

        TextView propertiesTextView = new TextView(mActivity);
        propertiesTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,   //largura
                LinearLayout.LayoutParams.WRAP_CONTENT)); //altura
        propertiesTextView.setGravity(Gravity.LEFT);
        propertiesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        propertiesTextView.setText(pptListStr);

        ll_horizontal_details.addView(propertiesTextView);

        LinearLayout ll_vertical_price_details = new LinearLayout(mActivity);
        ll_vertical_price_details.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ll_vertical_price_details.setOrientation(LinearLayout.VERTICAL);

        TextView priceTextView = new TextView(mActivity);
        priceTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,   //largura
                LinearLayout.LayoutParams.WRAP_CONTENT)); //altura
        priceTextView.setGravity(Gravity.RIGHT);
        priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        priceTextView.setText(productPrice);

        ll_vertical_price_details.addView(priceTextView);

        Button buttonAddProduct = new Button(mActivity);
        buttonAddProduct.setLayoutParams(new LinearLayout.LayoutParams(
                30,   //largura
                30)); //altura
        buttonAddProduct.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        buttonAddProduct.setText("ADD");

        ll_vertical_price_details.addView(buttonAddProduct);

        ll_horizontal_details.addView(ll_vertical_price_details);

        // Adicionar o root view vertical ao CardView
        cardview.addView(ll_vertical_root);

        int cv_color = ContextCompat.getColor(mActivity, R.color.cardsColor);
        cardview.setCardBackgroundColor(cv_color);

        // Add cardview to Search Result layout
        linearLayoutResults.addView(cardview);

    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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
            if (result != null) {
                bmImage.setImageBitmap(result);
            } else {
                //failed getting product image
                //TODO imagem em branco / produto não encontrado / erro
            }
        }
    }
}