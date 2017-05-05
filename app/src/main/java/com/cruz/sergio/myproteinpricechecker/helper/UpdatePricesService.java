package com.cruz.sergio.myproteinpricechecker.helper;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by Sergio on 05/05/2017.
 */

public class UpdatePricesService extends IntentService {

    public UpdatePricesService() {
        super(null);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdatePricesService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

        /*
        *  1º Query DB to get ProductsEntry._ID, ProductsEntry.COLUMN_PRODUCT_BASE_URL, ProductsEntry.COLUMN_MP_JSON_URL_DETAILS,

        *
        *
        * */

        // I need a cursor from a query containing All product IDs in the DB + json_url_details OR

        // Do work here, based on the contents of dataString


    }
}
