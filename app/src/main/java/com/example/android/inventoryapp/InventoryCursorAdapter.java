package com.example.android.inventoryapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;

/**
 * Created by Arit on 4/9/2018.
 */

public class InventoryCursorAdapter extends RecyclerView.Adapter<InventoryCursorAdapter.EntryViewholder> {

    //Declare variables for cursor and context
    private Cursor mCursor;
    private Context mContext;

    //Interface to handle clicks on list item
    public interface ListItemClickListener {
        //index of item that was clicked
        void onListItemClick(int id);
    }

    // member variable in GreenAdapter class to store the ListItemClickListener
    final private ListItemClickListener mOnClickListener;


    //Constructor for cursorAdapter
    public InventoryCursorAdapter(Context mContext, ListItemClickListener listener) {

        this.mContext = mContext;
        mOnClickListener = listener;
    }

    @Override
    public EntryViewholder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        EntryViewholder viewholder = new EntryViewholder(view);
        return viewholder;
    }

    @Override
    public void onBindViewHolder(EntryViewholder holder, int position) {
        // Get indices for each column in the inventory table
        int idIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        int nameIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME);
        int currentAmntIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_AMNT);
        int unitIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_UNIT);
        int reqAmntIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_REQ);
        int priorityIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRIORITY);


        mCursor.moveToPosition(position); // get to the right location in the cursor


        // Determine the values of the wanted data
        final int id = mCursor.getInt(idIndex);
        String name = mCursor.getString(nameIndex);
        String amount = mCursor.getString(currentAmntIndex);
        String unit = mCursor.getString(unitIndex);
        String requested = mCursor.getString(reqAmntIndex);
        String priority = mCursor.getString(priorityIndex);


        //Set values
        holder.itemView.setTag(id);
        holder.nameEntryView.setText(name);
        holder.amntEntryView.setText(amount);
        holder.unitEntryView.setText(unit);
        holder.reqAmntEntryView.setText(requested);
        holder.priorityEntryView.setText(priority);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }


    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor c) that is passed in.
     */
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }


    class EntryViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameEntryView;
        TextView amntEntryView;
        TextView unitEntryView;
        TextView reqAmntEntryView;
        TextView priorityEntryView;

        public EntryViewholder(View itemView) {
            super(itemView);
            nameEntryView = (TextView) itemView.findViewById(R.id.name);
            amntEntryView = (TextView) itemView.findViewById(R.id.amnt);
            unitEntryView = (TextView) itemView.findViewById(R.id.units);
            reqAmntEntryView = (TextView) itemView.findViewById(R.id.requested);
            priorityEntryView = (TextView) itemView.findViewById(R.id.priority);

            //set click listener on viewholder
            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int idIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
            int id = mCursor.getInt(idIndex);
            mOnClickListener.onListItemClick(id);

        }
    }
            public void sendEmailMessage() {
                int cursorPosition = 0;
                String emailString = "";
                mCursor.moveToPosition(cursorPosition);

                while (mCursor.moveToNext()) {
                    int nameIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME);
                    String name = mCursor.getString(nameIndex);
                    emailString = emailString +"Name: " + name;
                    int currentAmntIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_AMNT);
                    String amount = mCursor.getString(currentAmntIndex);
                    emailString = emailString + "   " + "Have: " + amount;
                    int unitIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_UNIT);
                    String unit = mCursor.getString(unitIndex);
                    emailString += "   " + "Unit: "+ unit;
                    int reqAmntIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_REQ);
                    String requested = mCursor.getString(reqAmntIndex);
                    emailString += "   " + "Need: " +requested;
                    int priorityIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRIORITY);
                    String priority = mCursor.getString(priorityIndex);
                    emailString +="   " +"Priority: "+ priority + "\n\n";
                    cursorPosition +=1;
                    mCursor.moveToPosition(cursorPosition);

                }
                Log.d("message", emailString);

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT, "Inventory for 4/15/18");
                intent.putExtra(Intent.EXTRA_TEXT, emailString);
                mContext.startActivity(intent);



            }


        }






