package com.vmal.solarify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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

    private ImageButton yesButton;
    private ImageButton noButton;
    private FloatingActionButton mNext;

    private static final String TAG = "PanelInquiry";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel_inquiry);



        mNext = (FloatingActionButton) findViewById(R.id.next_button);
        yesButton = (ImageButton) findViewById(R.id.student);
        noButton = (ImageButton) findViewById(R.id.host);

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
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();




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
                        Toast.makeText(PanelInquiry.this, "Please choose what you are looking for.", Toast.LENGTH_SHORT).show();
                        return;
                    }}
                    finish();
                    Intent i = new Intent(PanelInquiry.this,PowerForecastActivity.class);
                    startActivity(i);
                }
            }
        });


    }



}

