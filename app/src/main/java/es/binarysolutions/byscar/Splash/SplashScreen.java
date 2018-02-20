package es.binarysolutions.byscar.Splash;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import java.util.Timer;
import java.util.TimerTask;

import es.binarysolutions.byscar.R;
import es.binarysolutions.byscar.byScar.byScar;

/**
 * SplashScreen
 *
 * SPLASHTASK ACTIVITY
 */
public class SplashScreen extends AppCompatActivity {

    //SET DURATION OF SPLASHSCREEN
    private static final long SPLASH_SCREEN_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SET PORTRAIT ORIENTATION
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //HIDE TITLE BAR
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //SET CONTENT VIEW
        setContentView(R.layout.activity_splash_screen);

        //MAKE INVISIBLE APPLICATION BAR
        android.support.v7.app.ActionBar ab=getSupportActionBar();
        ab.hide();

        //CREATE TASK (OR THREAD)
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                //DECLARE INTENT WITH NEXT ACTIVITY
                Intent intent = new Intent().setClass(SplashScreen.this, byScar.class);
                //START NEXT ACTIVITY
                startActivity(intent);

                // CLOSE THE ACTIVITY SO THE USER WON'T ABLE TO GO BACK THIS ACTIVITY PRESSING BACK BUTTON
                finish();
            }
        };

        //SIMULATE A LONG LOADING PROCESS ON APPLICATION STARTUP
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);

    }
}
