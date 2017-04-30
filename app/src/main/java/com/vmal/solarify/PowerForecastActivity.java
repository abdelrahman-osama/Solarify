package com.vmal.solarify;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PowerForecastActivity extends AppCompatActivity {


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
    int area;
    double efficiency;
    int[] sunTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_forecast);


        currentDay = (Button) findViewById(R.id.circlebutton);
        Day1 = (TextView) findViewById(R.id.day1);
        Day2 = (TextView) findViewById(R.id.day2);
        Day3 = (TextView) findViewById(R.id.day3);
        Day4 = (TextView) findViewById(R.id.day4);
        Day5 = (TextView) findViewById(R.id.day5);
        Day6 = (TextView) findViewById(R.id.day6);
        week = new Double[7];
        sunTime = new int[7];
        getPowerData();


    }


    void getPowerData() {

        String BASE_URL = "https://developer.nrel.gov/api/pvwatts/v5.json?api_key=KhjqE5Ln7Ri9ckKXIej5onPadY3FDvCzlusgqPYB";

        String lat = "30";
        String lon = "30";
        String systemCapacity = "4";
        String azimuth = "180";
        tilt();
        String arrayType = "1"; //fixed
        String moduleType = "0"; //fixed
        String losses = "0"; //fixed
        String dataset = "intl";
        String timeFrame = "hourly";

        final String JSONRequest = BASE_URL + "&lat=" + lat + "&lon=" + lon + "&system_capacity=" + systemCapacity + "&azimuth=" + azimuth
                + "&tilt=" + tilt + "&array_type=" + arrayType + "&module_type=" + moduleType + "&losses=" + losses + "&dataset=" + dataset + "&timeframe=" + timeFrame;


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
    
   void calcData(String[] data){

       Arrays.fill(week, 0.0);
       Arrays.fill(sunTime, 0);
       int d = 0;
       Log.d("Array", data.toString());
       for (int i = 0;d<7;i+=24) {
           for (int j = 0; j < 24; j++) {
                   week[d] += Double.parseDouble(data[i+j]);
               if(Double.parseDouble(data[i+j])!=0.0){
                   sunTime[d]++;
               }
           }
           Log.d("weekDays", week[d].toString());
           Log.d("sunTime", sunTime[d]+"");
           d++;
       }
       viewData();
       
   }

   void viewData(){

     //  Calendar calendar = Calendar.getInstance();
       int day = new GregorianCalendar().get(Calendar.DAY_OF_WEEK);
        Log.d("Day", day+"");
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

       currentDay.setText(poaToProduction(week[0])+"kWh");
       Log.d("vero", week[1]+"");
       Log.d("vero", poaToProduction(week[1])+"");
       Day1.setText(poaToProduction(week[1])+"\nkWh\n"+d1);
       Day2.setText(poaToProduction(week[2])+"\nkWh\n"+d2);
       Day3.setText(poaToProduction(week[3])+"\nkWh\n"+d3);
       Day4.setText(poaToProduction(week[4])+"\nkWh\n"+d4);
       Day5.setText(poaToProduction(week[5])+"\nkWh\n"+d5);
       Day6.setText(poaToProduction(week[6])+"\nkWh\n"+d6);
   }


   void tilt(){

       int month = new GregorianCalendar().get(Calendar.MONTH);
       Log.d("month", month+1+"");




       tilt = "40";

   }

   int poaToProduction(double poa){

       efficiency=0.08;
       area=10;
       int sunT = 10;

       Double poaa = (poa*area*sunT*efficiency)/1000;

       return poaa.intValue();



   }

}
