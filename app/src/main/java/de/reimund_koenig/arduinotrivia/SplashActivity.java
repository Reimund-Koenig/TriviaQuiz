package de.reimund_koenig.arduinotrivia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


/**
 * Created by Reimund König
 * Mobile Anwendungen Master Abschlussprojekt
 * Mat.Nr.: 863751
 * Date: 30.01.2015.
 * Last Change: 10.02.2015
 *
 */

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Create the Image Views
        final ImageView splash1 = (ImageView)findViewById(R.id.splash1);
        final ImageView splash2 = (ImageView)findViewById(R.id.splash2);
        final ImageView splash3 = (ImageView)findViewById(R.id.splash3);
        final ImageView splash4 = (ImageView)findViewById(R.id.splash4);

        //Set some Image Views invisible
        splash1.setVisibility(View.INVISIBLE);
        splash2.setVisibility(View.INVISIBLE);
        splash3.setVisibility(View.INVISIBLE);

        //Create the Animations
        final Animation animationFadeInSplash1 = AnimationUtils.loadAnimation(this, R.anim.fadein);
        final Animation animationFadeInSplash2 = AnimationUtils.loadAnimation(this, R.anim.fadein);
        final Animation animationFadeInSplash3 = AnimationUtils.loadAnimation(this, R.anim.fadein);
        final Animation animationRotateSplash4 = AnimationUtils.loadAnimation(this, R.anim.rotate);

        //Create Animation Listener
        animationFadeInSplash1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splash1.setVisibility(View.VISIBLE);
                splash2.startAnimation(animationFadeInSplash2);
            }
        });
        animationFadeInSplash2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splash2.setVisibility(View.VISIBLE);
                splash3.startAnimation(animationFadeInSplash3);
            }
        });
        animationFadeInSplash3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splash3.setVisibility(View.VISIBLE);
            }
        });

        //Start Animations
        splash1.startAnimation(animationFadeInSplash1);
        splash4.startAnimation(animationRotateSplash4);

        //Open Menu Screen after 4 seconds
        long splashTimer = 4000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish(); // Close this activity
            }
        }, splashTimer);
    }

}

//*****************************************************************************************
//End of File
//*****************************************************************************************