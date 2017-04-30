package com.vmal.solarify;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PowerForecastActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener {


    Button currentDay;
    TextView Day1;
    TextView Day2;
    TextView Day3;
    TextView Day4;
    TextView Day5;
    TextView Day6;
    String d1;
    String d2;
    String d3;
    String d4;
    String d5;
    String d6;
    Double[] week;
    String[] acData;
    String tilt;
    RelativeLayout avgConsumption;
    int area;
    double efficiency;
    int[] sunTime;
    int totalconsumption;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private String lat;
    private String lon;
    String season;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_forecast);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        LocationManager service = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to
        // go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (provider == null) {

        } else {

            Location location = locationManager.getLastKnownLocation(provider);

            // Initialize the location fields
            if (location != null) {

                onLocationChanged(location);
            }
        }

        currentDay = (Button) findViewById(R.id.circlebutton);
        Day1 = (TextView) findViewById(R.id.day1);
        Day2 = (TextView) findViewById(R.id.day2);
        Day3 = (TextView) findViewById(R.id.day3);
        Day4 = (TextView) findViewById(R.id.day4);
        Day5 = (TextView) findViewById(R.id.day5);
        Day6 = (TextView) findViewById(R.id.day6);
        week = new Double[7];
        sunTime = new int[7];
        avgConsumption = (RelativeLayout) findViewById(R.id.avg_consumption);

        final TextView consumption = (TextView) findViewById(R.id.consumption_text);
        FirebaseDatabase.getInstance().getReference("appliances").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()){
                            if(snap.child("number_of_devices").getValue(int.class)>0) {
                                totalconsumption += snap.child("number_of_devices").getValue(int.class)
                                        *snap.child("number_of_hours").getValue(int.class)*snap.child("power").getValue(int.class)/1000;
                            }

                        }
                        consumption.setText(totalconsumption+" kWh");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        getPowerData();

        avgConsumption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(PowerForecastActivity.this,  ApplianceListActivity.class);
//                startActivity(i);
            }
        });


    }


    void getPowerData() {

        String BASE_URL = "https://developer.nrel.gov/api/pvwatts/v5.json?api_key=KhjqE5Ln7Ri9ckKXIej5onPadY3FDvCzlusgqPYB";


        String lati = "30";
        String lon = "29";
        if(this.lat2!=-1){
            lati = (int)this.lat2+"";
            lon = (int)lng2+"";
            Log.d("PowerForecastActivity",this.lat2+"");
            Log.d("PowerForecastActivity",this.lng2+"");
            Log.d("PowerForecastActivity","gotlocation :D");
        }
        String systemCapacity = "4";
        String azimuth = "180";

        tilt();
        String arrayType = "1"; //fixed
        String moduleType = "0"; //fixed
        String losses = "0"; //fixed
        String dataset = "intl";
        String timeFrame = "hourly";
        String radius = "900";

        final String JSONRequest = BASE_URL + "&lat=" + lati + "&lon=" + lon + "&system_capacity=" + systemCapacity + "&azimuth=" + azimuth
                + "&tilt=" + tilt + "&array_type=" + arrayType + "&module_type=" + moduleType + "&losses=" +
                losses + "&dataset=" + dataset + "&timeframe=" + timeFrame+"&radius="+radius;
        Log.d("PowerForecastActivity",JSONRequest);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(JSONRequest)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    String responseString = response.body().string();
                    Log.d("Response DATA:", response.body().string());
                    try {
                        JSONObject jsonData = new JSONObject(responseString);
                        JSONObject testData = new JSONObject(jsonData.getString("outputs"));
                        String testData2 = testData.getString("poa");
                        String acDataString = testData2.substring(1);
                        Log.d("testData", testData2);

                        acData = acDataString.split(",");
                        // Log.d("DataInStringArray", acData[13]);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                calcData(acData);

                            }
                        });


                        //TODO we should make an array that accepts the data coming from the JSON response
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();


    }

    void calcData(String[] data) {

        Arrays.fill(week, 0.0);
        Arrays.fill(sunTime, 0);
        int d = 0;
        Log.d("Array", data.toString());
        for (int i = 0; d < 7; i += 24) {
            for (int j = 0; j < 24; j++) {
                week[d] += Double.parseDouble(data[i + j]);
                if (Double.parseDouble(data[i + j]) != 0.0) {
                    sunTime[d]++;
                }
            }
            Log.d("weekDays", week[d].toString());
            Log.d("sunTime", sunTime[d] + "");
            d++;
        }
        viewData();

    }

    void viewData() {

        //  Calendar calendar = Calendar.getInstance();
        int day = new GregorianCalendar().get(Calendar.DAY_OF_WEEK);
        Log.d("Day", day + "");
        switch (day) {
            case 1:
                d1 = "Mon";
                d2 = "Tue";
                d3 = "Wed";
                d4 = "Thu";
                d5 = "Fri";
                d6 = "Sat";
                break;


            case 2:
                d1 = "Tue";
                d2 = "Wed";
                d3 = "Thu";
                d4 = "Fri";
                d5 = "Sat";
                d6 = "Sun";
                break;


            case 3:
                d1 = "Wed";
                d2 = "Thu";
                d3 = "Fri";
                d4 = "Sat";
                d5 = "Sun";
                d6 = "Mon";
                break;


            case 4:

                d1 = "Thu";
                d2 = "Fri";
                d3 = "Sat";
                d4 = "Sun";
                d5 = "Mon";
                d6 = "Tue";
                break;


            case 5:

                d1 = "Fri";
                d2 = "Sat";
                d3 = "Sun";
                d4 = "Mon";
                d5 = "Tue";
                d6 = "Wed";
                break;


            case 6:
                d1 = "Sat";
                d2 = "Sun";
                d3 = "Mon";
                d4 = "Tue";
                d5 = "Wed";
                d6 = "Thu";
                break;

            case 0:

                d1 = "Sun";
                d2 = "Mon";
                d3 = "Tue";
                d4 = "Wed";
                d5 = "Thu";
                d6 = "Fri";
                break;
        }

        // poaToProduction();

        currentDay.setText(poaToProduction(week[0]) + "kWh");
        Log.d("vero", week[1] + "");
        Log.d("vero", poaToProduction(week[1]) + "");
        Day1.setText(poaToProduction(week[1]) + "\nkWh\n" + d1);
        Day2.setText(poaToProduction(week[2]) + "\nkWh\n" + d2);
        Day3.setText(poaToProduction(week[3]) + "\nkWh\n" + d3);
        Day4.setText(poaToProduction(week[4]) + "\nkWh\n" + d4);
        Day5.setText(poaToProduction(week[5]) + "\nkWh\n" + d5);
        Day6.setText(poaToProduction(week[6]) + "\nkWh\n" + d6);
    }


    void tilt() {

         int month = new GregorianCalendar().get(Calendar.MONTH) +1;
       Log.d("month", month+"");

       if(lat2<0){
           if(month == 9 || month == 10 || month == 11){
               season = "spring";
           }
           else
               if(month == 12 || month == 1 || month == 2){
                   season = "summer";
               }else{
                   if(month>2 && month<6){
                       season = "fall";
                   }else{
                       if (month > 5 && month < 9) {
                           season = "winter";
                       }
               }
           }

           }
       else{ // bigger than zero

           if(month == 9 || month == 10 || month == 11){
               season = "fall";
           }else
           if(month == 12 || month == 1 || month == 2){
               season = "winter";
           }else{
               if(month>2 && month<6){
                   season = "spring";
               }else{
                   if(month>5 && month<9){
                       season = "summer";
                   }

               }
           }

       }
       
      double latit= lat2;
       if(season == "winter"){
           tilt = String.valueOf(Math.abs(latit)*0.9+29);
       }

       if (season== "summer"){
           tilt= String.valueOf(Math.abs(latit)*0.9-23.5);
       }
       if (season=="spring"|| season=="fall") {
           tilt = String.valueOf(Math.abs(latit) - 2.5);

       }
       Log.d("tilt", tilt);
    }

    int poaToProduction(double poa) {

        efficiency = 0.08;
        area = 10;
        int sunT = 10;

        Double poaa = (poa * area * sunT * efficiency) / 1000;

        return poaa.intValue();


    }

    double lat2 = -1;
    double lng2 = -1;
    LocationManager locationManager;
    private String provider = "";

    @Override
    public void onPause() {
        locationManager.removeUpdates(this);
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        if (provider != null) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        }
    }

    @Override
    public void onConnectionSuspended(int i) {


    }
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length == 1 &&
                permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }




        } else {
            if (provider != null) {
                Location location = locationManager.getLastKnownLocation(provider);
                onLocationChanged(location);
            }
            // Permission was denied. Display an error message.
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        if(lat2 == -1) {
            lat2 =(location.getLatitude());
            lng2 =(location.getLongitude());

        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
