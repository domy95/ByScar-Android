package es.binarysolutions.byscar.byScar;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Locale;

import es.binarysolutions.byscar.Classes.Utils;
import es.binarysolutions.byscar.R;

public class Configuration extends AppCompatActivity {

    public static final int LLENGUATGES=150;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Switch switchMode;
    Spinner spinnerIdioma;
    boolean primer_inici=true;
    private boolean canvi_idioma;
    private String idiomaSigla;
    private String idioma;
    private int positionCurrentLanguage;
    private String arrayIdiomes[];
    private String arrayIdiomesSigles[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences=getSharedPreferences(byScar.BYSCAR_PREFERENCES,MODE_PRIVATE);
        Utils.changeLocale(getApplicationContext(),preferences.getString(byScar.BYSCAR_LANGUAGE,"ca")); //CHANGE LOCALE

        setContentView(R.layout.activity_configuration);
        setTitle(getString(R.string.action_settings));


        //INITIALIZE OBJECTS AND VARIABLES
        canvi_idioma=false;
        arrayIdiomes=new String[]{"Català","English","Español","Euskal","French","Galego","Português"};
        arrayIdiomesSigles=new String[]{"ca","en","es","eu","fr","gl","pt"};

        //CHARGE LANDSCAPE OR PORTRAIT MODE
        if(preferences.getBoolean(byScar.BYSCAR_BUTTONS_MODE,true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        //RECOVER ACTUAL LANGUAGE
        Locale locale=getResources().getConfiguration().locale;
        int i=0;
        boolean trobat=false;
        while(i<arrayIdiomes.length && !trobat){
            if(locale.toString().substring(0,2).equalsIgnoreCase(arrayIdiomesSigles[i])){
                idiomaSigla=arrayIdiomesSigles[i];
                idioma=arrayIdiomes[i];
                positionCurrentLanguage=i;
                trobat=true;
            }
            else i++;
        }

        spinnerIdioma=(Spinner)findViewById(R.id.spinnerIdiomes);
        switchMode=(Switch)findViewById(R.id.switchConfiguration);

        //SETUP SPINNER
        setupSpinnerLanguage();

        editor=preferences.edit();
        switchMode.setChecked(preferences.getBoolean(byScar.BYSCAR_BUTTONS_MODE,true));

        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editor.putBoolean(byScar.BYSCAR_BUTTONS_MODE,true);
                }
                else{
                    editor.putBoolean(byScar.BYSCAR_BUTTONS_MODE,false);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_configuration, menu);

        //RECOVER ITEM OF MENU AND ADDED A LISTENER FOR ON CLICK EVENT
        MenuItem item_desa=menu.findItem(R.id.desa);
        item_desa.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(canvi_idioma){
                    editor.putString(byScar.BYSCAR_LANGUAGE,idiomaSigla.toLowerCase());
                    Utils.changeLocale(getApplicationContext(),idiomaSigla);
                    setResult(1);
                }
                else setResult(-1);
                editor.commit();
                //CLOSE ACTIVITY
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void setupSpinnerLanguage(){
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayIdiomes);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIdioma.setAdapter(adaptador);
        try {
            spinnerIdioma.setSelection(positionCurrentLanguage);
        }catch (Exception e){
            e.printStackTrace();
        }

        spinnerIdioma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(primer_inici) primer_inici=false;
                else{
                    if(idiomaSigla!=arrayIdiomesSigles[position].toLowerCase()){
                        canvi_idioma=true;
                        idiomaSigla=arrayIdiomesSigles[position].toLowerCase();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
