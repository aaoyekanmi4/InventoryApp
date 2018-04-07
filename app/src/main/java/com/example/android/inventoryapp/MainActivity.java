package com.example.android.inventoryapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Populate the spinner with choices
        Spinner spinner = (Spinner) findViewById(R.id.priority);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.priority_array, R.layout.list_item);
        adapter.setDropDownViewResource(R.layout.list_item);
        spinner.setAdapter(adapter);
    }


    public class entryViewholder extends RecyclerView.ViewHolder{

        public entryViewholder(View itemView) {
            super(itemView);
            TextView nameEntryView = (TextView) itemView.findViewById(R.id.name);
            TextView amntEntryView = (TextView) itemView.findViewById(R.id.amnt);
            TextView unitEntryView = (TextView) itemView.findViewById(R.id.units);
            TextView reqAmntEntryView = (TextView) itemView.findViewById(R.id.requested);
            Spinner priorityEntryView = (Spinner) itemView.findViewById(R.id.priority);

        }

        public class inventoryAdapter extends RecyclerView.Adapter<entryViewholder>{

            private int mNumberOfItems;

            public inventoryAdapter(int numberOfItems){
                mNumberOfItems = numberOfItems;
            }
            @Override
            public entryViewholder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
                Context context = viewGroup.getContext();
                int layoutIdForListItem = R.layout.list_item;
                LayoutInflater inflater = LayoutInflater.from(context);
                boolean shouldAttachToParentImmediately = false;

                View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
                entryViewholder viewholder = new entryViewholder(view);
                return viewholder;
            }

            @Override
            public void onBindViewHolder(entryViewholder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return mNumberOfItems;
            }
        }
    }
}
