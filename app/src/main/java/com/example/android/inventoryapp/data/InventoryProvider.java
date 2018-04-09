package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.TABLE_NAME;

/**
 * Created by Arit on 4/8/2018.
 */

public class InventoryProvider extends ContentProvider {

    // Define final integer constants for the directory of tasks and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int INVENTORY = 100;
    public static final int INVENTORY_WITH_ID = 101;

    // Declare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Define a static buildUriMatcher method that associates URI's with their int match
    /**
     Initialize a new matcher object without any matches,
     then use .addURI(String authority, String path, int match) to add matches
     */
    public static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the task directory and a single item by ID.
         */
        uriMatcher.addURI(InventoryContract.AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        uriMatcher.addURI(InventoryContract.AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_WITH_ID);

        return uriMatcher;
    }

    // Member variable for a InventoryHelper that's initialized in the onCreate() method
    private InventoryHelper mInventoryHelper;
    @Override
    public boolean onCreate() {
        Context context = getContext();
        InventoryHelper mInventoryHelper = new InventoryHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[]selectionArgs, @Nullable String sortOrder) {
        //Get a readable database object
        final SQLiteDatabase db = mInventoryHelper.getReadableDatabase();

        //Use the uriMatcher and the uri passed in to get the correct integer code
        int match = sUriMatcher.match(uri);

        //Variable for the cursor that will be returned

        Cursor returnCursor;

        //Based on the integer code entered, run the appropriate query method


        switch(match){
            case INVENTORY:
                returnCursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case INVENTORY_WITH_ID:

                //get row id from Uri
                //1 is section to direct right of table name in path
                String id = uri.getPathSegments().get(1);

                String mSelection ="_id=?";
                String [] mSelectionArgs = new String[]{id};

                returnCursor = db.query(TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            //default exception
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        //tells cursor which uri it was created for
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;


    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mInventoryHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);

        // URI to be returned
        Uri returnUri;

        switch (match) {
            case INVENTORY:
                // Insert new values into the database
                // Inserting values into tasks table
                long id = db.insert(TABLE_NAME, null, contentValues);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mInventoryHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);

        //declare return variable for number of rows deleted
        int mNumberDeleted;

        switch (match) {
            case INVENTORY_WITH_ID:
            //get row id from Uri
            //1 is section to direct right of table name in path
            String id = uri.getPathSegments().get(1);

            String mSelection = "_id=?";
            String[] mSelectionArgs = new String[]{id};

            break;

            //default exception
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }




    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
