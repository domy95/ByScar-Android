package es.binarysolutions.byscar.byScar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import es.binarysolutions.byscar.Classes.Utils;
import es.binarysolutions.byscar.R;

public class About extends AppCompatActivity {

    private TextView txvWeb;
    private TextView txvTel;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //RECOVER PREFERENCES
        preferences=getSharedPreferences(byScar.BYSCAR_PREFERENCES,MODE_PRIVATE);
        Utils.changeLocale(getApplicationContext(),preferences.getString(byScar.BYSCAR_LANGUAGE,"ca")); //CHANGE LOCALE

        setContentView(R.layout.activity_about);
        setTitle(Html.fromHtml(getString(R.string.action_about)));

        //CHARGE LANDSCAPE OR PORTRAIT MODE
        if(preferences.getBoolean(byScar.BYSCAR_BUTTONS_MODE,true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        recoverViews();
        setupListeners();

    }

    private void recoverViews(){
        txvTel=(TextView)findViewById(R.id.tv_telefon);
        txvWeb=(TextView)findViewById(R.id.tv_web);
    }

    private void setupListeners(){
        txvTel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:000000000")); //Phone number
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        });
        txvWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://domain.com"; //Website
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }
}
