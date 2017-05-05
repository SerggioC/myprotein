package com.cruz.sergio.myproteinpricechecker;

import android.content.ContentValues;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

/**
 * Created by Ratan on 7/29/2015.
 */
public class GraphsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.graph_fragment, null);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Button button_add_price = (Button) getActivity().findViewById(R.id.button_add_price_to_db);
        button_add_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues priceContentValues = new ContentValues();
                priceContentValues.put(ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS, 1);
                priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE, "10€");
                priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE, 10);
                priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE, System.currentTimeMillis());

                DBHelper dbHelper = new DBHelper(getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long priceRowId = db.insert(ProductsContract.PricesEntry.TABLE_NAME, null, priceContentValues);
                if (priceRowId < 0L) {
                    DetailsFragment.showCustomToast(getActivity(), "Error inserting price to DataBase " +
                                    ProductsContract.PricesEntry.TABLE_NAME + "! Try again.",
                            R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
                } else {
                    DetailsFragment.showCustomToast(getActivity(), "Added Price!",
                            R.mipmap.ic_ok2, R.color.green, Toast.LENGTH_LONG);
                }
                db.close();
            }

        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
