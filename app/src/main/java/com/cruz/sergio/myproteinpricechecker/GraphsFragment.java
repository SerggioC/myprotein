package com.cruz.sergio.myproteinpricechecker;

import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

import java.util.ArrayList;
import java.util.List;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DBHelper dbHelper = new DBHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<String> distinct_id_products = new ArrayList<>();

        String query1 = "SELECT DISTINCT " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS +
                " FROM " + ProductsContract.PricesEntry.TABLE_NAME +
                " ORDER BY " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS;
        Cursor cursor1 = db.rawQuery(query1, null);
        int cursor1Count = cursor1.getCount();
        if (cursor1Count > 0) {
            while (cursor1.moveToNext()) {
                String _id_poducts = cursor1.getString(cursor1.getColumnIndex(ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS));
                distinct_id_products.add(_id_poducts);
            }
            // cursor ended
        } else {
            // TODO Empty database!
        }
        cursor1.close();

        ArrayList<ArrayList<Double>> priceValues_Array_arrayList = new ArrayList<>();

        for (int i = 0; i < distinct_id_products.size(); i++) {
            Cursor cursor2 = db.rawQuery("SELECT * FROM " + ProductsContract.PricesEntry.TABLE_NAME +
                    " WHERE " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS + " = '" + distinct_id_products.get(i) + "'" +
                    " ORDER BY " + ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE + " ASC", null);
            int cursor2Count = cursor2.getCount();
            if (cursor2Count > 0) {
                ArrayList<Double> priceValues_arrayList = new ArrayList<>(cursor2Count);
                while (cursor2.moveToNext()) {
                    double price_value = cursor2.getDouble(cursor2.getColumnIndex(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE));
                    priceValues_arrayList.add(price_value);
                }
                priceValues_Array_arrayList.add(priceValues_arrayList);
                // cursor ended
            } else {
                // TODO Empty database!
            }

            cursor2.close();
        }


        db.close();
        Log.i("Sergio>", this + " onCreate\npriceValues_Array_arrayList= " + priceValues_Array_arrayList);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
