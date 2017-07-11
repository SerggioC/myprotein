package com.cruz.sergio.myproteinpricechecker;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ratan on 7/29/2015.
 */
public class GraphsFragment extends Fragment {
    ArrayList<ArrayList<Double>> priceValues_Array_arrayList = null;
    ArrayList<ArrayList<Long>> dates_Array_arrayList = null;
    ArrayList<String> product_names_arrayList = null;
    ArrayList<String> currency_symb_arrayList = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.graph_fragment, null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long startTime = System.nanoTime();

        DBHelper dbHelper = new DBHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        String query1 = "SELECT DISTINCT " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS +
                " FROM " + ProductsContract.PricesEntry.TABLE_NAME +
                " ORDER BY " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS;

        Cursor cursor1 = db.rawQuery(query1, null);
        int cursor1Count = cursor1.getCount();
        if (cursor1Count > 0) {
            List<String> distinct_id_products = new ArrayList<>();
            while (cursor1.moveToNext()) {
                String _id_products = cursor1.getString(cursor1.getColumnIndex(ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS));
                distinct_id_products.add(_id_products);
            }
            priceValues_Array_arrayList = new ArrayList<>(cursor1Count);
            dates_Array_arrayList = new ArrayList<>(cursor1Count);

            for (int i = 0; i < distinct_id_products.size(); i++) {
                Cursor cursor2 = db.rawQuery("SELECT " +
                        ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE + " , " + ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE +
                        " FROM " + ProductsContract.PricesEntry.TABLE_NAME +
                        " WHERE " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS + " = '" + distinct_id_products.get(i) + "'" +
                        " ORDER BY " + ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE + " ASC", null);
                int cursor2Count = cursor2.getCount();
                if (cursor2Count > 0) {
                    ArrayList<Double> priceValues_arrayList = new ArrayList<>(cursor2Count);
                    ArrayList<Long> dates_arrayList = new ArrayList<>(cursor2Count);
                    while (cursor2.moveToNext()) {
                        double price_value = cursor2.getDouble(cursor2.getColumnIndex(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE));
                        long date_value = cursor2.getLong(cursor2.getColumnIndex(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE));
                        priceValues_arrayList.add(price_value);
                        dates_arrayList.add(date_value);
                    }
                    priceValues_Array_arrayList.add(priceValues_arrayList);
                    dates_Array_arrayList.add(dates_arrayList);

                } else {
                    // TODO ERROR!
                }

                cursor2.close();

                Cursor cursor3 = db.rawQuery("SELECT " +
                        ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME + " , " + ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL +
                        " FROM " + ProductsContract.ProductsEntry.TABLE_NAME +
                        " WHERE " + ProductsContract.ProductsEntry._ID + " = '" + distinct_id_products.get(i) + "'" +
                        " ORDER BY " + ProductsContract.ProductsEntry._ID + " ASC", null);

                int cursor3Count = cursor2.getCount();
                if (cursor3Count > 0) {
                    product_names_arrayList = new ArrayList<>(cursor3Count);
                    currency_symb_arrayList = new ArrayList<>(cursor3Count);
                    while (cursor3.moveToNext()) {
                        String name = cursor3.getString(cursor3.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME));
                        String symbol = cursor3.getString(cursor3.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL));
                        product_names_arrayList.add(name);
                        currency_symb_arrayList.add(symbol);
                    }
                } else {
                    // TODO ERROR!
                }

                cursor3.close();
            }


        } else {
            // TODO Warning Empty database!
        }
        cursor1.close();


        db.close();
        long elapsed = System.nanoTime() - startTime;
//        Log.d("Sergio>", this + " onCreate\npriceValues_Array_arrayList= " + priceValues_Array_arrayList);
//        Log.d("Sergio>", this + " dates_Array_arrayList= " + dates_Array_arrayList + "\n");
        Log.d("Sergio>", this + " onCreate\nelapsed Millis= " + TimeUnit.NANOSECONDS.toMillis(elapsed));
    }

    public class graphMarker extends MarkerView implements IMarker {
        private TextView marker_txt;

        public graphMarker(Context context, int layoutResource) {
            super(context, layoutResource);
            marker_txt = (TextView) findViewById(R.id.marker_tv);
        }

        private MPPointF mOffset;

        @Override
        public MPPointF getOffset() {
            if (mOffset == null) {
                // center the marker horizontally and vertically
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }
            return mOffset;
        }

        @Override
        public MPPointF getOffsetForDrawingAtPoint(float v, float v1) {
            return null;
        }

        @Override
        public void refreshContent(Entry entry, Highlight highlight) {
            marker_txt.setText("" + entry.getY() + "\n" +
                    entry.getX());

            // this will perform necessary layouting
            super.refreshContent(entry, highlight);
        }

        @Override
        public void draw(Canvas canvas, float v, float v1) {

        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        LineChart lineChart = (LineChart) getActivity().findViewById(R.id.line_chart);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setGridBackgroundColor(Color.WHITE);
        lineChart.setDrawGridBackground(true);
        lineChart.setDrawBorders(true);
        lineChart.getDescription().setEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(true);
        lineChart.getAxisRight().setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setDrawInside(false);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
//        List<LegendEntry> legendEntries = new ArrayList<>();
//
//        legendEntries.add(new Entry());
//        legend.setEntries(legendEntries);

        graphMarker graphMarker = new graphMarker(getContext(), R.layout.graph_marker);
        lineChart.setMarker(graphMarker);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.RED);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);


        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMaximum(200f);
        yAxis.setAxisMinimum(0f);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawZeroLine(true);
        yAxis.setDrawGridLines(true);

        if (dates_Array_arrayList != null && priceValues_Array_arrayList != null) {
            int numgraphs = dates_Array_arrayList.size();
            ArrayList<ILineDataSet> dataSets = new ArrayList<>(numgraphs);
            for (int k = 0; k < numgraphs; k++) {
                ArrayList<Long> datesArray = dates_Array_arrayList.get(k);
                ArrayList<Double> pricesArray = priceValues_Array_arrayList.get(k);

                List<Entry> entries = new ArrayList<>();
                int datasize = dates_Array_arrayList.get(k).size();
                for (int i = 0; i < datasize; i++) {
                    // cada ponto -> (x,y) => Entry(float x, float y);
                    float y = pricesArray.get(i).floatValue();
                    if (y > 0) {
                        entries.add(new Entry(datesArray.get(i).floatValue(), y));
                    }
                }

                LineDataSet lineDataSet = new LineDataSet(entries, "Product " + k); // add entries to dataset
                lineDataSet.setMode(LineDataSet.Mode.LINEAR);
                lineDataSet.setCircleColor(Color.LTGRAY);
                lineDataSet.setDrawHighlightIndicators(true);
                lineDataSet.setColor(Color.DKGRAY);
                lineDataSet.setLineWidth(2f);

                dataSets.add(lineDataSet);
            }
            LineData data = new LineData(dataSets);
            data.setValueTextSize(10f);
            data.setHighlightEnabled(true);
            lineChart.setData(data);
        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
