package com.fyp_lubdub;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Splash extends AppCompatActivity {

    private TextView txt;
    RelativeLayout Rl;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        txt = findViewById(R.id.Name);

        final ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                txt,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f));
        scaleDown.setDuration(180);
        scaleDown.setInterpolator(new FastOutSlowInInterpolator());
        //scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatCount(3);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);



        final Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                scaleDown.start();
            }
        }, 500);


        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                scaleDown.start();
            }

        }, 2500);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Splash.this,Login.class);
                Splash.this.finish();
                startActivity(i);

            }
        }, 3500);

       /* Rl = findViewById(R.id.splash);
        Rl.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int availableHeight = Rl.getMeasuredHeight();
                if(availableHeight>0) {
                    Rl.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    SC(Rl);
                    //save height here and do whatever you want with it
                }
            }
        });*/

    }


}
