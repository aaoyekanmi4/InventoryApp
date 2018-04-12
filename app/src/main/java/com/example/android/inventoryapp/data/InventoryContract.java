package com.example.android.inventoryapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Arit on 4/7/2018.
 */

public class InventoryContract {
    //Defining constants to build Uri
    public static final String AUTHORITY = "com.example.android.inventoryapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryEntry implements BaseColumns {

        //building content_uri to be used in collecting data through content provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_INVENTORY).build();

        //Table name and columns

        public static final String TABLE_NAME = "inventory";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_AMNT = "amount";
        public static final String COLUMN_UNIT = "units";
        public static final String COLUMN_REQ = "requested";
        public static final String COLUMN_PRIORITY = "priority";


    }
}
