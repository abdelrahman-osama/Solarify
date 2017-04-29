package com.vmal.solarify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.okhttp.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PanelInquiry extends AppCompatActivity {

    private ToggleButton yesButton;
    private ToggleButton noButton;
    private FloatingActionButton mNext;

    private static final String TAG = "PanelInquiry";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_inquiry);

        getPowerData();

        mNext = (FloatingActionButton) findViewById(R.id.next_button);
        yesButton = (ToggleButton) findViewById(R.id.student);
        noButton = (ToggleButton) findViewById(R.id.host);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noButton.setChecked(false);
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yesButton.setChecked(false);
            }
        });





        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v==mNext)
                {
                    SharedPreferences sharedPref = getSharedPreferences("ApplicationModeFile", Context.MODE_PRIVATE);
                    if(yesButton.isChecked()){
                        noButton.setChecked(false);
//                        SharedPreferences.Editor editor = sharedPref.edit();
//                        editor.putString("application_mode","student_mode");
//                        editor.commit();
                    }else{ if(noButton.isChecked()){
                        yesButton.setChecked(true);
//                        SharedPreferences.Editor editor = sharedPref.edit();
//                        editor.putString("application_mode","list_mode");
//                        editor.commit();
                    } else{
                        Toast.makeText(PanelInquiry.this, "Please choose what you are looking for!", Toast.LENGTH_SHORT).show();
                        return;
                    }}
                    finish();
                    Intent i = new Intent(PanelInquiry.this,PowerForecastActivity.class);
                    startActivity(i);
                }
            }
        });


    }


    void getPowerData() {

        String BASE_URL = "https://developer.nrel.gov/api/pvwatts/v5.json?api_key=KhjqE5Ln7Ri9ckKXIej5onPadY3FDvCzlusgqPYB";

        String lat = "30";
        String lon = "30";
        String systemCapacity = "4";
        String azimuth = "180";
        String tilt = "40";
        String arrayType = "1";
        String moduleType = "1";
        String losses = "10";
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
                        String testData2 = testData.getString("ac");
                        Log.d("testData", testData2);

                        String[] acData = testData2.split(",");
                        Log.d("DataInStringArray", acData[13]);

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
}

