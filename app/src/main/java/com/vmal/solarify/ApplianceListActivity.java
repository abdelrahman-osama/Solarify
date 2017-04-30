package com.vmal.solarify;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

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

    class  Appliance{
        String name;
        int value;

        public Appliance(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appliance_list);
        numOfAppliances = new ArrayList<>();

        mRecyclerView = (ListView) findViewById(R.id.recyclerView);
        findViewById(R.id.next_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser()!=null)
                    FirebaseDatabase.getInstance().getReference().child("appliances").child(FirebaseAuth.getInstance()
                            .getCurrentUser().getUid()).setValue(numOfAppliances);
            }
        });
        //Make call to AsyncTask
        new AsyncFetch().execute();



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



    private class AsyncFetch extends AsyncTask<Void, Void , String>{

        ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d("appliances","inpreexecute");
            //this method will be running on UI thread
            try {
                JSONObject obj = new JSONObject(loadJSONFromAsset());
                JSONArray m_jArry = obj.getJSONArray("appliances");
                HashMap<String, String> m_li;
                Log.d("appliances","inpreexecute try");

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
        @Override
        protected String doInBackground(Void... params) {
            Log.d("appliances","doinbackground");
            final ArrayList<String>input=new ArrayList<>();
            for(int i = 0;i<formList.size();i++){
                input.add(formList.get(i).get("appliances"));
                numOfAppliances.add(new Appliance(input.get(i),0));
                Log.d("appliances",input.get(i));
            }

            final ContentAdapter contentAdapter = new ContentAdapter(input);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
//                    mRecyclerView.setLayoutManager(mLayoutManager);
//                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//                    mRecyclerView.setHasFixedSize(true);
//
//
//                    mRecyclerView.setAdapter(contentAdapter);
//                    float offsetPx = getResources().getDimension(R.dimen.bottom_offset_dp);
//                    BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
//                    mRecyclerView.addItemDecoration(bottomOffsetDecoration);
                    ListAdapter itemsAdapter = new ListAdapter(ApplianceListActivity.this,R.layout.list_view,input);
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

    public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.MyViewHolder> {

        private List<String> applianceList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView name;
            public int position;
            TextView num;
            int number;
            public MyViewHolder(View view) {
                super(view);
                name = (TextView) view.findViewById(R.id.appi_name);
                num = (TextView) view.findViewById(R.id.ad_max_g_text_num);
                num.setText(numOfAppliances.get(position).value+"");
                final ImageView imageView = (ImageView) view.findViewById(R.id.ad_max_g_text_minus);
                view.findViewById(R.id.ad_max_g_text_minus).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        number = Integer.parseInt(num.getText().toString().trim());
                        if(number==1){
                            imageView.setAlpha(0.12f);
                            num.setText("0");
                        }else if(number>1){
                            num.setText(number-1 +"");
                        }

                        numOfAppliances.get(position).value = number-1;
                    }
                });
                view.findViewById(R.id.ad_max_g_text_plus).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        number = Integer.parseInt(num.getText().toString().trim());
                        if(number==0) {
                            imageView.setAlpha(1f);
                        }
                        num.setText(number+1 + "");
                        numOfAppliances.get(position).value = number+1;
                    }
                });
            }
        }


        public ContentAdapter(List<String> applianceList) {
            this.applianceList = applianceList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_view, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.name.setText(applianceList.get(position));
            holder.position = position;
        }

        @Override
        public int getItemCount() {
            return applianceList.size();
        }
    }

    static class BottomOffsetDecoration extends RecyclerView.ItemDecoration {
        private int mBottomOffset;

        public BottomOffsetDecoration(int bottomOffset) {
            mBottomOffset = bottomOffset;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int dataSize = state.getItemCount();
            int position = parent.getChildPosition(view);
            if (dataSize > 0 && position == dataSize - 1) {
                outRect.set(0, 0, 0, mBottomOffset);
            } else {
                outRect.set(0, 0, 0, 0);
            }

        }
    }

    public class ListAdapter extends ArrayAdapter<String> {

        public ListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public ListAdapter(Context context, int resource, List<String> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.list_view, null);
            }

            final String p = getItem(position);

            if (p != null) {
                TextView name;
                final TextView num;
                final int pos = position;
                name = (TextView) v.findViewById(R.id.appi_name);
                num = (TextView) v.findViewById(R.id.ad_max_g_text_num);
                name.setText(p);


                final ImageView imageView = (ImageView) v.findViewById(R.id.ad_max_g_text_minus);
                v.findViewById(R.id.ad_max_g_text_minus).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       int number = Integer.parseInt(num.getText().toString().trim());
                        if (number == 1) {
                            imageView.setAlpha(0.12f);
                            num.setText("0");
                        } else if (number > 1) {
                            num.setText(number - 1 + "");
                        }

                        numOfAppliances.get(pos).value = number - 1;
                    }
                });
                v.findViewById(R.id.ad_max_g_text_plus).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int number = Integer.parseInt(num.getText().toString().trim());
                        if (number == 0) {
                            imageView.setAlpha(1f);
                        }
                        num.setText(number + 1 + "");
                        numOfAppliances.get(pos).value = number + 1;
                    }
                });
            }
            return v;
        }

    }

}
