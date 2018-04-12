package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
/**
 * Created by Arit on 4/8/2018.
 */

public class InventoryHelper extends SQLiteOpenHelper {

    //database name
    private static final String DATABASE_NAME = "inventoryDb.db";

    //database version
    private static final int VERSION = 1;

    // Constructor

    public InventoryHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create inventory table
        final String CREATE_TABLE = "CREATE TABLE "  + InventoryEntry.TABLE_NAME + " (" +
                InventoryEntry._ID                + " INTEGER PRIMARY KEY, " +
                InventoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_AMNT    + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_UNIT    + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_REQ + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_PRIORITY    + " TEXT NOT NULL);";


        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME);
        onCreate(db);

    }
}
