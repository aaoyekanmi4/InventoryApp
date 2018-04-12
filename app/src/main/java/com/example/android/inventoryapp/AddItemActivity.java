package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class AddItemActivity extends AppCompatActivity {

    //declare variables for each EditText view
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
    }

    //go back to MainActivity when back button clicked
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