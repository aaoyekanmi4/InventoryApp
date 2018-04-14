package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class AddItemActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    //Define a projection for displaying values in Edit mode
    public static final String[] ENTRY_PROJECTION = {
            InventoryEntry.COLUMN_NAME,
            InventoryEntry.COLUMN_AMNT,
            InventoryEntry.COLUMN_UNIT,
            InventoryEntry.COLUMN_REQ,
            InventoryEntry.COLUMN_PRIORITY
    };

    //Assign an index number for each of the columns in the projection

    public static final int INDEX_NAME = 0;
    public static final int INDEX_AMNT = 1;
    public static final int INDEX_UNIT = 2;
    public static final int INDEX_REQ = 3;
    public static final int INDEX_PRIORITY = 4;

    //Declare unique integer value for loader
    private static final int ID_ENTRY_LOADER = 2;

    //Declare variable to store Uri passed from MainActivity with intent
    Uri currentEntryUri;


    //Declare variables for each EditText view
    private EditText mNameEditText;
    private EditText mAmountEditText;
    private EditText mUnitEditText;
    private EditText mRequestedEditText;
    private EditText mPriorityEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        //assign each edit text field to the variables declared above
        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mAmountEditText = (EditText) findViewById(R.id.edit_current);
        mUnitEditText = (EditText) findViewById(R.id.edit_units);
        mRequestedEditText = (EditText) findViewById(R.id.edit_req);
        mPriorityEditText = (EditText) findViewById(R.id.edit_priority);


        //show back button for up navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get Intent from main activity if item clicked on
        Intent intent = getIntent();

        //get Uri from the intent
        currentEntryUri = intent.getData();

        //If there is no Uri, set title to Add Item. If there is a Uri, set title to Edit Item
        if (currentEntryUri == null){
            setTitle("Add Item");
        }
        else {
            setTitle("Edit Item");

            //Initialize loader connecting it to this Activity if editing
            getSupportLoaderManager().initLoader(ID_ENTRY_LOADER, null, this);
        }


    }
    //Creates CursorLoader using the Uri passed in to form a cursor
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch(loaderId) {
            case ID_ENTRY_LOADER:

                return new CursorLoader(this,
                        currentEntryUri,
                        ENTRY_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }

    }
    //When load is finished, check to see if valid data is present, else return
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }
        //Add each desired value to the UI
        String itemName = data.getString(INDEX_NAME);
        mNameEditText.setText(itemName);

        String itemAmnt = data.getString(INDEX_AMNT);
        mAmountEditText.setText(itemAmnt);

        String itemUnit = data.getString(INDEX_UNIT);
        mUnitEditText.setText(itemUnit);

        String itemReq = data.getString(INDEX_REQ);
        mRequestedEditText.setText(itemReq);

        String itemPriority = data.getString(INDEX_PRIORITY);
        mPriorityEditText.setText(itemPriority);
    }

    //Change edit text fields back to empty
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mAmountEditText.setText("");
        mUnitEditText.setText("");
        mRequestedEditText.setText("");
        mPriorityEditText.setText("");

    }

    //Go back to MainActivity when back button clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    //method to add item to database when button is clicked
    public void addItem(View view) {

        //obtain string values from each edit text field
        String itemName = mNameEditText.getText().toString();
        String itemAmount = mAmountEditText.getText().toString();
        String itemUnit = mUnitEditText.getText().toString();
        String itemRequested = mRequestedEditText.getText().toString();
        String itemPriority = mPriorityEditText.getText().toString();

        //Create new ContentValues object
        ContentValues inventoryValues = new ContentValues();

        //place each value into contentValues object
        inventoryValues.put(InventoryEntry.COLUMN_NAME, itemName);
        inventoryValues.put(InventoryEntry.COLUMN_AMNT, itemAmount);
        inventoryValues.put(InventoryEntry.COLUMN_UNIT, itemUnit);
        inventoryValues.put(InventoryEntry.COLUMN_REQ, itemRequested);
        inventoryValues.put(InventoryEntry.COLUMN_PRIORITY, itemPriority);

        //insert row into database by passing Uri and inventoryValues to content provider
        Uri addedUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, inventoryValues);

    }


}