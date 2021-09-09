package com.example.android.inventoryapp;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class GoogleSheetsExport extends AppCompatActivity

   implements EasyPermissions.PermissionCallbacks {

    private InventoryCursorAdapter mAdapter;
        GoogleAccountCredential mCredential;
        private TextView mOutputText;
        private Cursor mCursor;
        private Button mCallApiButton;
        ProgressDialog mProgress;

        static final int REQUEST_ACCOUNT_PICKER = 1000;
        static final int REQUEST_AUTHORIZATION = 1001;
        static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
        static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

        private static final String BUTTON_TEXT = "Call Google Sheets API";
        private static final String PREF_ACCOUNT_NAME = "accountName";
        private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS };


    //Assign an index number for each of the columns in the projection

    public static final int INDEX_NAME = 0;
    public static final int INDEX_AMNT = 1;
    public static final int INDEX_UNIT = 2;
    public static final int INDEX_REQ = 3;
    public static final int INDEX_PRIORITY = 4;

    //Declare unique integer value for loader
    private static final int SHEETS_ENTRY_LOADER = 3;

    //Declare variable for whole table Uri
    Uri wholeTableUri;


    /**
         * Create the main activity.
         * @param savedInstanceState previously saved instance data.
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LinearLayout activityLayout = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            activityLayout.setLayoutParams(lp);
            activityLayout.setOrientation(LinearLayout.VERTICAL);
            activityLayout.setPadding(16, 16, 16, 16);

            ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            mCallApiButton = new Button(this);
            mCallApiButton.setText(BUTTON_TEXT);
            mCallApiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallApiButton.setEnabled(false);
                    mOutputText.setText("");
                    getResultsFromApi();
                    mCallApiButton.setEnabled(true);
                }
            });
            activityLayout.addView(mCallApiButton);

            mOutputText = new EditText(this);
            mOutputText.setLayoutParams(tlp);
            mOutputText.setPadding(16, 16, 16, 16);
            mOutputText.setVerticalScrollBarEnabled(true);
            mOutputText.setMovementMethod(new ScrollingMovementMethod());
            mOutputText.setText(
                    "Click the \'" + BUTTON_TEXT +"\' button to test the API.");
            activityLayout.addView(mOutputText);

            mProgress = new ProgressDialog(this);
            mProgress.setMessage("Calling Google Sheets API ...");

            setContentView(activityLayout);

            // Initialize credentials and service object.
            mCredential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());



        }



        /**
         * Attempt to call the API, after verifying that all the preconditions are
         * satisfied. The preconditions are: Google Play Services installed, an
         * account was selected and the device currently has online access. If any
         * of the preconditions are not satisfied, the app will prompt the user as
         * appropriate.
         */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Spreadsheet> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();

        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Spreadsheet doInBackground(Void... params) {
            try {

                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Create a new spreadsheet
         *
         * @return id of newly created sheet
         * @throws IOException
         */
        private Spreadsheet getDataFromApi() throws IOException {
           Cursor mCursor = getContentResolver().query(InventoryContract.InventoryEntry.CONTENT_URI,
                   null, null, null, InventoryContract.InventoryEntry.COLUMN_NAME);

            Spreadsheet requestBody = new Spreadsheet();
            SpreadsheetProperties properties = new SpreadsheetProperties();
            String title ="Inventory: 4/29/2018";
            properties.setTitle(title);
            requestBody.setProperties(properties);




            Sheets.Spreadsheets.Create request = mService.spreadsheets().create(requestBody);

            Spreadsheet newSheet = request.execute();
            String id = newSheet.getSpreadsheetId();


            Log.v("id", id);
            int rowCount = mCursor.getCount() + 1;
            String rowCountString = Integer.toString(rowCount);
            Log.d("rowCountString", rowCountString);

            String range = "Sheet1!A1:E" + rowCountString;

            List<List<Object>> values = new ArrayList<>();
            List<Object> row1 = new ArrayList<>();
            row1.add("Name");
            row1.add("Have");
            row1.add("Units");
            row1.add("Need");
            row1.add("Priority");
            values.add(row1);
            int cursorPosition = -1;
            mCursor.moveToPosition(cursorPosition);
            while (mCursor.moveToNext()) {
                Log.d("cursorPosition", Integer.toString(cursorPosition));

                List<Object> row2 = new ArrayList<>();
                int nameIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME);
                String name = mCursor.getString(nameIndex);
                Log.d("name", name);
                row2.add(name);
                int currentAmntIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_AMNT);
                String amount = mCursor.getString(currentAmntIndex);
                row2.add(amount);
                int unitIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_UNIT);
                String unit = mCursor.getString(unitIndex);
                row2.add(unit);
                int reqAmntIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_REQ);
                String requested = mCursor.getString(reqAmntIndex);
                row2.add(requested);
                int priorityIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRIORITY);
                String priority = mCursor.getString(priorityIndex);
                row2.add(priority);


                values.add(row2);
                cursorPosition +=1;
                mCursor.moveToPosition(cursorPosition);

            }

            ValueRange valueRange = new ValueRange();
            valueRange.setMajorDimension("ROWS");
            valueRange.setValues(values);

            UpdateValuesResponse response = this.mService.spreadsheets().values()
                    .update(id, range, valueRange)
                    .setValueInputOption("RAW")
                    .execute();

            Log.d("Update_response", response.toString());
            Log.d("SpreadSheetId: ", id);

            return newSheet;


        }



        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Spreadsheet output) {
            mProgress.hide();
            if (output == null) {
                mOutputText.setText("No results returned.");
            } else {
                String id = output.getSpreadsheetId();
                mOutputText.setText(id);
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GoogleSheetsExport.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }


    }


}
