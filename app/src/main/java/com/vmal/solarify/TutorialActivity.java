package com.vmal.solarify;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;

public class TutorialActivity extends AppCompatActivity {

    int pos = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);


        FloatingActionButton mNext = (FloatingActionButton) findViewById(R.id.next_button);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (pos){
                    case 0:findViewById(R.id.tutorial1).startAnimation(AnimationUtils.
                            loadAnimation(TutorialActivity.this,R.anim.right_to_left));
                        findViewById(R.id.tutorial2).setVisibility(View.VISIBLE);
                        findViewById(R.id.tutorial1).setVisibility(View.GONE);
                        pos +=1;
                        break;
                    case 1:findViewById(R.id.tutorial2).startAnimation(AnimationUtils.
                            loadAnimation(TutorialActivity.this,R.anim.right_to_left));
                        findViewById(R.id.tutorial3).setVisibility(View.VISIBLE);
                        findViewById(R.id.tutorial2).setVisibility(View.GONE);

                        pos +=1;break;
                    case 2:findViewById(R.id.tutorial3).startAnimation(AnimationUtils.
                            loadAnimation(TutorialActivity.this,R.anim.right_to_left));
                        startActivity(new Intent(TutorialActivity.this,ApplianceListActivity.class));
                        finish();
                        pos +=1;break;
                }
            }
        });

        FloatingActionButton mBack = (FloatingActionButton) findViewById(R.id.back_button);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (pos){
                    case 0:onBackPressed();
                        break;
                    case 1:
                        findViewById(R.id.tutorial1).setVisibility(View.VISIBLE);
                        findViewById(R.id.tutorial2).setVisibility(View.GONE);

                        pos-=1;break;
                    case 2:
                        findViewById(R.id.tutorial2).setVisibility(View.VISIBLE);
                        findViewById(R.id.tutorial3).setVisibility(View.GONE);
                        pos -=1;break;
                }
            }
        });
    }
}
