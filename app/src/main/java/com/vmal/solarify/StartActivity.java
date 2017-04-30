package com.vmal.solarify;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    sleep(3000);  //Delay of 10 seconds
                } catch (Exception e) {

                } finally {

                    Intent i = new Intent(StartActivity.this,
                            MainActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.fadein , R.anim.fadeout);
                    finish();
                }
            }
        };
        welcomeThread.start();

    }
}
