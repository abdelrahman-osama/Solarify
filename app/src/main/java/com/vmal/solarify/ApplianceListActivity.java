package com.vmal.solarify;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApplianceListActivity extends AppCompatActivity {

    ListView mRecyclerView;
    List<Appliance>numOfAppliances;
    ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
    ListAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appliance_list);
        numOfAppliances = new ArrayList<>();



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(" ");

        mRecyclerView = (ListView) findViewById(R.id.recyclerView);
        findViewById(R.id.next_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0;i<numOfAppliances.size();i++)
                if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
                    FirebaseDatabase.getInstance().getReference().child("appliances").child(FirebaseAuth.getInstance()
                            .getCurrentUser().getUid()).child(numOfAppliances.get(i).name).child("number_of_devices").setValue(numOfAppliances.get(i).value);
                    int power = Integer.parseInt(formList.get(i).get("value"));
                    FirebaseDatabase.getInstance().getReference().child("appliances").child(FirebaseAuth.getInstance()
                            .getCurrentUser().getUid()).child(numOfAppliances.get(i).name).child("power").setValue(power);
                }

                startActivity(new Intent(ApplianceListActivity.this, ApplianceConsumptionActivity.class));
                finish();
            }
        });

        FirebaseDatabase.getInstance().getReference("appliances").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()) {
                    int i = 0;
                    for (DataSnapshot snap : dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getChildren()) {
                        numOfAppliances.add(new Appliance(snap.getKey(),
                                snap.child("number_of_devices").getValue(int.class),i));
                        i++;
                    }
                    loadFormList();
                    itemsAdapter = new ListAdapter(ApplianceListActivity.this,R.layout.list_view,numOfAppliances);
                    mRecyclerView.setAdapter(itemsAdapter);
                }
                else {
                    //Make call to AsyncTask
                    new AsyncFetch().execute();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("dataofappliances.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    void loadFormList(){
        //this method will be running on UI thread
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            JSONArray m_jArry = obj.getJSONArray("appliances");
            HashMap<String, String> m_li;


            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                String formula_value = jo_inside.getString("A");
                String url_value = jo_inside.getString("B");

                //Add your values in your `ArrayList` as below:
                m_li = new HashMap<String, String>();
                m_li.put("appliances", formula_value);
                m_li.put("value", url_value);

                formList.add(m_li);
            }
        } catch (JSONException e) {
        }
    }

    private class AsyncFetch extends AsyncTask<Void, Void , String>{




        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            loadFormList();


        }
        @Override
        protected String doInBackground(Void... params) {
            Log.d("appliances","doinbackground");
            final ArrayList<String>input=new ArrayList<>();
            for(int i = 0;i<formList.size();i++){
                input.add(formList.get(i).get("appliances"));
                numOfAppliances.add(new Appliance(input.get(i),0,i));
                Log.d("appliances",input.get(i));
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    itemsAdapter = new ListAdapter(ApplianceListActivity.this,R.layout.list_view,numOfAppliances);
                    mRecyclerView.setAdapter(itemsAdapter);

                }
            });

            return null;
        }


        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

        }

    }


    public class ListAdapter extends ArrayAdapter<Appliance> implements Filterable {

        List<Appliance> items=new ArrayList<>();
        List<Appliance> mFilter;
        FilterClass filter;
        public ListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public ListAdapter(Context context, int resource, List<Appliance> items) {
            super(context, resource, items);
            this.items = items;
            mFilter = items;
        }

        class ViewHolder {
            TextView name;
            TextView num;
            ImageView minus;
            ImageView plus;
            int pos;
            public ViewHolder(View v){

                minus =(ImageView) v.findViewById(R.id.ad_max_g_text_minus);
                minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int number = Integer.parseInt(num.getText().toString().trim());
                        if (number == 1) {
                            minus.setAlpha(0.12f);
                            num.setText("0");
                            numOfAppliances.get(pos).value = number - 1;
                        } else if (number > 1) {
                            num.setText(number - 1 + "");
                            numOfAppliances.get(pos).value = number - 1;
                        }


                        notifyDataSetChanged();
                    }
                });

                plus =(ImageView) v.findViewById(R.id.ad_max_g_text_plus);
                plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int number = Integer.parseInt(num.getText().toString().trim());
                        if (number == 0) {
                            minus.setAlpha(1f);
                        }
                        num.setText(number + 1 + "");
                        numOfAppliances.get(pos).value = number + 1;
                        notifyDataSetChanged();
                    }
                });

            }
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(ApplianceListActivity.this);
                convertView = vi.inflate(R.layout.list_view, null);
                holder = new ViewHolder(convertView);
                holder.name = (TextView) convertView.findViewById(R.id.appi_name);
                holder.num = (TextView) convertView.findViewById(R.id.ad_max_g_text_num);
                holder.pos = position;
                convertView.setTag(holder);

            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();

            }


            holder.name.setText(mFilter.get(position).name);
            holder.num.setText(mFilter.get(position).value+"");
            holder.pos = mFilter.get(position).pos;

            if (mFilter.get(position).value == 0)
                    holder.minus.setAlpha(0.12f);
                else holder.minus.setAlpha(1f);


            filter = new FilterClass(items, this);
//            final String p = getItem(position);

//            if (p != null) {
//
//                final int pos = position;
//
//
//                holder.num.setTag(new Integer(position));
//                holder.num.setOnClickListener(null);
//
//                holder.name.setText(p);
//                holder.num.setText(numOfAppliances.get(position).value+"");
//                final ImageView imageView = (ImageView) v.findViewById(R.id.ad_max_g_text_minus);
//                if (numOfAppliances.get(position).value == 0)
//                    imageView.setAlpha(0.12f);
//                else imageView.setAlpha(1f);
//
//            }
            return convertView;
        }

        @Override
        public int getCount() {
            return mFilter.size();
        }

        @Override
        public Appliance getItem(int position) {
            return mFilter.get(position);
        }

        // set adapter filtered list
        public void setList(List<Appliance> list) {
            this.mFilter = list;
        }

        //call when you want to filter
        public void filterList(String text) {
            if(filter!=null)
                filter.filter(text);
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options_menu, menu);
        super.onCreateOptionsMenu(menu);


        MenuItem item = menu.findItem(R.id.search);
        SearchView search = (SearchView) item.getActionView();
        search.setIconifiedByDefault(false);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);



        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //filters list items from adapter

                itemsAdapter.filterList(newText);
                return false;
            }
        });

        return true;
    }


    class FilterClass extends Filter {

        private List<Appliance> cardItems;
        private List<Appliance> filteredLocation;
        private ListAdapter adapter;

        public FilterClass(List<Appliance> contactList, ListAdapter adapter) {
            this.adapter = adapter;
            this.cardItems = contactList;
            this.filteredLocation = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredLocation.clear();
            final FilterResults results = new FilterResults();

            //here you need to add proper items do filteredContactList

                for (final Appliance item : cardItems) {
                    loop:
                    {
                    String[] splitS = item.name.trim().split(" ");
                    for (String s : splitS)
                        if (s.toLowerCase().trim().startsWith(constraint.toString())) {
                            filteredLocation.add(item);
                            break loop;
                        }
                    }
                }

            results.values = filteredLocation;
            results.count = filteredLocation.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.setList(filteredLocation);
            adapter.notifyDataSetChanged();
        }

    }

}
