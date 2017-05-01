package com.vmal.solarify;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ApplianceConsumptionActivity extends AppCompatActivity {

    ListView mListView;
    ArrayList<String> input;
    List<Appliance>numOfAppliances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appilance_consumption);

        numOfAppliances = new ArrayList<>();
        input = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.recyclerView);


        findViewById(R.id.next_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0;i<numOfAppliances.size();i++)
                    if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
                        FirebaseDatabase.getInstance().getReference().child("appliances").child(FirebaseAuth.getInstance()
                                .getCurrentUser().getUid()).child(numOfAppliances.get(i).name).child("number_of_hours").setValue(numOfAppliances.get(i).value);
                    }
                startActivity(new Intent(ApplianceConsumptionActivity.this,PowerForecastActivity.class));
                finish();
            }

        });

        FirebaseDatabase.getInstance().getReference("appliances").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()){
                    if(snap.child("number_of_devices").getValue(int.class)>0) {
                        input.add(snap.getKey());
                        numOfAppliances.add(new Appliance(snap.getKey(), 0));
                    }

                }
                ListAdapter listAdapter = new ListAdapter(ApplianceConsumptionActivity.this,R.layout.list_view,input);
                mListView.setAdapter(listAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                num.setTag(new Integer(position));
                num.setOnClickListener(null);

                name.setText(p);
                num.setText(numOfAppliances.get(position).value+"");
                final ImageView imageView = (ImageView) v.findViewById(R.id.ad_max_g_text_minus);
                final ImageView imageView2 = (ImageView) v.findViewById(R.id.ad_max_g_text_plus);
                if (numOfAppliances.get(position).value == 0)
                    imageView.setAlpha(0.12f);
                else imageView.setAlpha(1f);

                if(numOfAppliances.get(position).value == 24)
                    imageView2.setAlpha(0.12f);
                else imageView2.setAlpha(1f);

                v.findViewById(R.id.ad_max_g_text_minus).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int number = Integer.parseInt(num.getText().toString().trim());
                        if(number==24) {
                            imageView2.setAlpha(1f);
                            num.setText("23");
                            numOfAppliances.get(pos).value = number - 1;
                        }
                        if (number == 1) {
                            imageView.setAlpha(0.12f);
                            num.setText("0");
                            numOfAppliances.get(pos).value = number - 1;
                        } else if (number > 1) {
                            num.setText(number - 1 + "");
                            numOfAppliances.get(pos).value = number - 1;
                        }


                        notifyDataSetChanged();
                    }
                });
                v.findViewById(R.id.ad_max_g_text_plus).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int number = Integer.parseInt(num.getText().toString().trim());
                        if (number == 0) {
                            imageView.setAlpha(1f);
                        }
                        if(number<24) {
                            num.setText(number + 1 + "");
                            imageView2.setAlpha(0.12f);
                            numOfAppliances.get(pos).value = number + 1;
                        }
                        notifyDataSetChanged();
                    }
                });
            }
            return v;
        }

    }

}
