package es.binarysolutions.byscar.byScar;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import es.binarysolutions.byscar.Classes.Utils;
import es.binarysolutions.byscar.R;
import es.binarysolutions.byscar.Splash.SplashScreen;
import es.binarysolutions.byscar.Threads.ConnexionBluetooth;

public class byScar extends AppCompatActivity implements SensorEventListener {

    //Static variables
    public static String BYSCAR_PREFERENCES="byscar_preferences";
    public static String BYSCAR_PREFERENCES_NO_THREAD="provar_connexio";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String BYSCAR_BUTTONS_MODE="byscar_buttons_mode";
    public static String BYSCAR_LANGUAGE="language";

    //Declare views
    ToggleButton OnOff;
    ImageButton btnExit;
    ImageButton btnRight;
    ImageButton btnLeft;
    ImageButton btnForward;
    ImageButton btnBackward;
    ImageView btnConnect;
    ImageView ivStop;
    //Declare views in landscape mode
    ImageView ivGas, ivLogo, ivFre;

    //Declare variables
    boolean on_off;
    private String previous_state;
    View[] views;
    // String for MAC address
    private static String address = null;

    //Declare objects
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    //private ConnectedThread mConnectedThread;
    private ConnexionBluetooth mConnectedThread;
    // SPP UUID service, we used to connect with bluetooth module
    private static final UUID BTMODULEUUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); //Bluetooth UUID
    private SharedPreferences preferences;

    //Declare objects
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean connectat=false;

    //Object rotate animation
    RotateAnimation animation;
    float pos=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        previous_state="c";

        //Initializa on_off in a false position
        on_off=false;
        connectat=false;

        //Recover preferences
        preferences=getSharedPreferences(BYSCAR_PREFERENCES,MODE_PRIVATE);
        Utils.changeLocale(getApplicationContext(),preferences.getString(byScar.BYSCAR_LANGUAGE,"ca"));
        setContentView(R.layout.activity_by_scar);

        //CHARGE LANDSCAPE OR PORTRAIT MODE
        if(preferences.getBoolean(byScar.BYSCAR_BUTTONS_MODE,true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            oncreatePortrait();
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            try {
                oncreateLandscape();
            }catch (Exception e){
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean(byScar.BYSCAR_BUTTONS_MODE,true);
                editor.commit();
            }
        }

    }

    //onResume Method
    @Override
    protected void onResume() {
        super.onResume();

        //Recover preferences
        preferences=getSharedPreferences(BYSCAR_PREFERENCES,MODE_PRIVATE);

        //CHECK MODE
        if(preferences.getBoolean(byScar.BYSCAR_BUTTONS_MODE,true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            onResumeLandscape();
        }

        //GET MAC ADDRESS FROM DEVICE SELECTED IN DEVICE LIST ACTIVITY VIA SHARED PREFERENECES
        address = preferences.getString(EXTRA_DEVICE_ADDRESS,null);
        //IF WE DON'T SELECTED THE CORRECT DEVICE WE GO TO ELSE....
        if(preferences.getBoolean(BYSCAR_PREFERENCES_NO_THREAD,false)) {
            //PUT BYSCAR_PREFERENCES_NO_THREAD IN FALSE MODE
            SharedPreferences.Editor editor=preferences.edit();
            editor.putBoolean(BYSCAR_PREFERENCES_NO_THREAD,false);
            editor.commit();

            try {
                //CREATE BLUETOOTH DEVICE AND SET THE MAC ADRESS
                BluetoothDevice device = btAdapter.getRemoteDevice(address);

                //TRY TO CREATE A BLUETOOTH SOCKET BY DEVICE
                try {
                    btSocket = createBluetoothSocket(device);
                } catch (Exception e) {
                    //SHOW MESSAGE TO ADVERTISE THAT SOCKET IT CAN'T CREATE
                    Toast.makeText(this.getApplicationContext(), this.getResources().getString(R.string.la_cracio_del_socket_ha_fallat), Toast.LENGTH_SHORT).show();

                }

                //ESTABLISH BLUETOOTH SOCKET CONNECTION
                try {
                    btSocket.connect();
                    Toast.makeText(this.getApplicationContext(), this.getResources().getString(R.string.connexio_establerta), Toast.LENGTH_SHORT).show();

                    //ENABLE TOGGLEBUTTON
                    OnOff.setEnabled(true);
                    OnOff.setChecked(false);
                } catch (Exception e) {
                    //IF WE CAN'T ESTABLISH THE BLUETOOTH SOCKET CONNECTION WE CLOSE THE SOCKET
                    try {
                        btSocket.close();
                    } catch (Exception e2) {
                        Log.e("ERROR", "EL SOCKET JA ESTAVA TANCAT O NO S'HAVIA POGUT CREAR");
                        e2.printStackTrace();
                    }
                    Log.e("ERROR", "NO HEM POGUT CONNECTARNOS AL SOCKET");
                    e.printStackTrace();
                }
                //CREATE NEW CONNEXIONBLUETOOTH THREAD TO ENABLE SEND DATA MODE
                mConnectedThread=new ConnexionBluetooth(getApplicationContext(),btSocket,views,OnOff);

                //WE SEND A CHARACTER, IF IT BREAKS WE KNOW THAT WE AREN'T CONNECTED
                //IF IT BREAKS, WE'LL SEE A MESSAGE SAYING THAT WE AREN'T CONNECTED
                mConnectedThread.write("Z");
            } catch (Exception exception) {
                exception.printStackTrace();
                connectat=false;
            }
        }

    }

    //onPause Method
    @Override
    protected void onPause() {
        super.onPause();
        try
        {
            if(connectat) mConnectedThread.write("c");
            //WHE CAN'T LEAVE BLUETOOTH SOCKETS OPEN WHEN LEAVING ACTIVITY
            btSocket.close();
            //UNREGISTER THE LISTENER FOR SENSOR MANAGER
            mSensorManager.unregisterListener(this);
        } catch (Exception e2) {
            Log.e("ERROR","ERROR AL TANCAR EL SOCKET BLUETOOTH");
            e2.printStackTrace();
        }
    }

    //Method onBackPressed from AppCompatActivity
    @Override
    public void onBackPressed() {
        try
        {
            if(connectat) mConnectedThread.write("c");
            //WHE CAN'T LEAVE BLUETOOTH SOCKETS OPEN WHEN LEAVING ACTIVITY
            btSocket.close();
        } catch (Exception e2) {
            Log.e("ERROR","ERROR AL TANCAR EL SOCKET BLUETOOTH");
            e2.printStackTrace();
        }
        //WE SHOW A DIALOG TO USER FOR NOTIFY THAT IT IS LEAVING THE APPLICATION
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getResources().getString(R.string.SortirByScar));
        builder.setMessage(this.getResources().getString(R.string.SegurSortirByScar));
        builder.setPositiveButton(this.getResources().getString(R.string.sortir), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //IF USER IS AGREE, WHE CLOSE APPLICATION
                finish();
            }
        });

        builder.setNegativeButton(this.getResources().getString(R.string.cancelar), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //IF USER I DISAGREE, WHE CLOSE DIALOG
                dialog.cancel();
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean(BYSCAR_PREFERENCES_NO_THREAD,false);
                OnOff.setEnabled(false);
                connectat=false;
            }
        });

        builder.create().show();
    }

    //Method onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //WHE CHECK THE RESULT OF BLUETOOTH INTENT
        switch (requestCode){
            case 5:
                //IF THE RESULT IS -1, IT MEANS THAT USER DIDN'T WANT TO TURN ON BLUETOOTH. WE CLOS APPLICATION
                if(resultCode==-1) {
                    finish();
                }
                break;
            case Configuration.LLENGUATGES:
                if(resultCode==1){
                    Intent intent=new Intent(this,SplashScreen.class);
                    this.finish();
                    startActivity(intent);
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //Method to assign a menu into activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //WE INFLATE THE MENU INTO ACTIVITY APPLICATION BAR
        getMenuInflater().inflate(R.menu.menu_strartup, menu);

        //RECOVER ITEMS OF THAT MENU AND WE ADD THEM A CLICK LISTENTER
        MenuItem item_about=menu.findItem(R.id.action_about);
        MenuItem item_setting=menu.findItem(R.id.action_setting);
        item_about.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //IF WE ARE CONNECTED WE CHANGE STATE IN STOP MODE
                if(connectat) mConnectedThread.write("c");
                //CHANGE VALUES IN DISCONNECTED MODE AND OPEN ABOUT ACTIVITY
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean(BYSCAR_PREFERENCES_NO_THREAD,false);
                OnOff.setEnabled(false);
                connectat=false;

                Intent intent=new Intent(byScar.this,About.class);
                startActivity(intent);
                return false;
            }
        });

        item_setting.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //IF WE ARE CONNECTED WE CHANGE STATE IN STOP MODE
                if(connectat) mConnectedThread.write("c");
                //CHANGE VALUES IN DISCONNECTED MODE AND OPEN CONFIGURATION ACTIVITY
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean(BYSCAR_PREFERENCES_NO_THREAD,false);
                OnOff.setEnabled(false);
                connectat=false;

                Intent intent=new Intent(byScar.this,Configuration.class);
                startActivityForResult(intent,Configuration.LLENGUATGES);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    //onConfigurationChanged method
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //METHODS FOR IMPLEMENTS IN LANDSCAPE MODE
    @Override
    public void onSensorChanged(SensorEvent event) {
        try{
            //CHECK MODE AND WE ARE CONNECTED
            if(!preferences.getBoolean(byScar.BYSCAR_BUTTONS_MODE,true)&&connectat) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                if (Math.abs(x) > Math.abs(y)) {
                    if (x < 0 && connectat) {
                        //RIGHT
                        mConnectedThread.write("a");//FORWARD

                        animation=new RotateAnimation(pos, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        animation.setInterpolator(new LinearInterpolator());
                        animation.setDuration(500);
                        animation.setFillEnabled(true);
                        animation.setFillAfter(true);
                        ivLogo.startAnimation(animation);
                        pos=0;

                    }
                    if (x > 0 && connectat) {
                        //LEFT
                        mConnectedThread.write("e");//BACKWARD

                        animation=new RotateAnimation(pos, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        animation.setInterpolator(new LinearInterpolator());
                        animation.setDuration(500);
                        animation.setFillEnabled(true);
                        animation.setFillAfter(true);
                        ivLogo.startAnimation(animation);
                        pos=0;

                    }
                } else {
                    if (y < 0 && connectat) {
                        //TOP
                        mConnectedThread.write("b"); //LEFT

                        animation=new RotateAnimation(pos, -45.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        animation.setInterpolator(new LinearInterpolator());
                        if(pos>0)animation.setDuration(800);
                        if(pos<=0) animation.setDuration(500);
                        animation.setFillEnabled(true);
                        animation.setFillAfter(true);
                        ivLogo.startAnimation(animation);
                        pos=-45;

                    }
                    if (y > 0 && connectat) {
                        //BOTTOM
                        mConnectedThread.write("d"); //RIGHT

                        animation=new RotateAnimation(pos, 45.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        animation.setInterpolator(new LinearInterpolator());
                        if(pos<0) animation.setDuration(800);
                        if(pos>=0) animation.setDuration(500);
                        animation.setFillEnabled(true);
                        animation.setFillAfter(true);
                        ivLogo.startAnimation(animation);
                        pos=45;

                    }
                }
                if (x > (-2) && x < (2) && y > (-2) && y < (2) && connectat) {
                    //CENTER
                    mConnectedThread.write("c");

                    animation=new RotateAnimation(pos, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.setDuration(500);
                    animation.setFillEnabled(true);
                    animation.setFillAfter(true);
                    ivLogo.startAnimation(animation);
                    pos=0;

                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            Utils.lockViews(views);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*-------------------------------------------------*/
    /*-----------------PRIVATE METHODS-----------------*/
    /*-------------------------------------------------*/

    //Creates secure outgoing connecetion with BT device using UUID
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    //Method to recover the views from portrait mode
    private void recoverViewsPortrait(){
        OnOff=(ToggleButton)findViewById(R.id.tbOnOff);
        btnExit=(ImageButton)findViewById(R.id.btnExit);
        btnRight=(ImageButton)findViewById(R.id.btnDreta);
        btnLeft=(ImageButton)findViewById(R.id.btnEsquerra);
        btnForward=(ImageButton)findViewById(R.id.btnEndavant);
        btnBackward=(ImageButton)findViewById(R.id.btnEnrera);
        btnConnect=(ImageView)findViewById(R.id.btnConnect);
        ivStop=(ImageView)findViewById(R.id.ivStop);
    }

    //Method to recover the views from landscape mode
    private void recoverViewsLandscape(){
        OnOff=(ToggleButton)findViewById(R.id.tbOnOff);
        btnExit=(ImageButton)findViewById(R.id.btnExit);
        ivLogo=(ImageView)findViewById(R.id.ivStop);
        btnConnect=(ImageView)findViewById(R.id.btnConnect);
    }

    //Method to setup all click listeners from portrait mode
    private void setupListenersPortrait(){

        //Listener for click in toogle button
        OnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                on_off=!on_off;
                if(on_off){
                    Utils.unlockViews(views);
                    mConnectedThread.write("f");
                }
                else{
                    Utils.lockViews(views);
                    mConnectedThread.write("c");
                }
                Log.e("POSICIÓ",String.valueOf(on_off));
                Log.e("SETUP_LISTENERS","ON/OFF!!");
            }
        });

        //Listener for click in imagebutton
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("SETUP_LISTENERS","EXIT!!");
                if(connectat) mConnectedThread.write("c");
                onBackPressed();
            }
        });

        //Listener for touch in imagebutton
        btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==(MotionEvent.ACTION_DOWN)){
                    mConnectedThread.write("d");
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    mConnectedThread.write(previous_state);
                }
                return false;
            }
        });

        //Listener for touch in imagebutton
        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==(MotionEvent.ACTION_DOWN)){
                    mConnectedThread.write("b");
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    mConnectedThread.write(previous_state);
                }
                return false;
            }
        });

        //Listener for touch in imagebutton
        btnForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==(MotionEvent.ACTION_DOWN)){
                    previous_state="a";
                    mConnectedThread.write("a");
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    previous_state="c";
                    mConnectedThread.write("c");
                }
                return false;
            }
        });

        //Listener for touch in imagebutton
        btnBackward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==(MotionEvent.ACTION_DOWN)){
                    previous_state="e";
                    mConnectedThread.write("e");
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    previous_state="c";
                    mConnectedThread.write("c");
                }
                return false;
            }
        });

        //Listener for click in imageview
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("SETUP_LISTENERS","CONNECT!!");
                //IF WE ARE CONNECTED WE CHANGE STATE IN STOP MODE
                if(connectat) mConnectedThread.write("c");
                //WE START THE NEXT ACTIVITY TO START CONNECTION WITH BLUETOOTH MODULE
                Intent intent=new Intent(byScar.this,DeviceList.class);
                startActivityForResult(intent, 5);
            }
        });

        //Listener for touch in imageview
        ivStop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==(MotionEvent.ACTION_DOWN)){
                    previous_state="c";
                    mConnectedThread.write("c");
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    previous_state="c";
                    mConnectedThread.write("c");
                }
                return false;
            }
        });
    }

    //Method to setup all click listeners from landscape mode
    private void setupListenersLandscape(){
        //Listener for click in toogle button
        OnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                on_off=!on_off;
                if(on_off){
                    Utils.unlockViews(views);
                    mConnectedThread.write("f");
                    connectat=true;
                }
                else{
                    connectat=false;
                    Utils.lockViews(views);
                    mConnectedThread.write("c");
                }
                Log.e("POSICIÓ",String.valueOf(on_off));
                Log.e("SETUP_LISTENERS","ON/OFF!!");
            }
        });

        //Listener for click in imagebutton
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("SETUP_LISTENERS","EXIT!!");
                //IF WE ARE CONNECTED WE CHANGE STATE IN STOP MODE
                if(connectat) mConnectedThread.write("c");
                onBackPressed();
            }
        });

        //Listener for click in imageview
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("SETUP_LISTENERS","CONNECT!!");
                //IF WE ARE CONNECTED WE CHANGE STATE IN STOP MODE
                if(connectat) mConnectedThread.write("c");
                //WE START THE NEXT ACTIVITY TO START CONNECTION WITH BLUETOOTH MODULE
                Intent intent=new Intent(byScar.this,DeviceList.class);
                startActivityForResult(intent, 5);
            }
        });

        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation botzina= AnimationUtils.loadAnimation(getApplicationContext(),R.animator.horn);
                ivLogo.startAnimation(botzina);
                mConnectedThread.write("f");
            }
        });
    }

    //Method onCreate in portrait mode
    private void oncreatePortrait(){
        //WE RECOVER VIEWS
        recoverViewsPortrait();

        //LOCK TOGGLEBUTTON
        OnOff.setEnabled(false);

        //ARRAY OF VIEWS IN PORTRAIT MODE
        views=new View[]{
                ivStop,
                btnBackward,
                btnForward,
                btnLeft,
                btnRight
        };
        //LOCK ALL OF VIEWS
        Utils.lockViews(views);

        //GET BLUETOOTH ADAPTER
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //SETUP CLICK LISTENER
        setupListenersPortrait();
    }

    //Method onCreate in landscape mode
    private void oncreateLandscape(){
        //WE RECOVER VIEWS
        recoverViewsLandscape();

        //LOCK TOGGLEBUTTON
        OnOff.setEnabled(false);

        //ARRAY OF VIEWS IN PORTRAIT MODE
        views=new View[]{
                ivLogo
        };
        //LOCK ALL OF VIEWS
        ivLogo.setEnabled(false);

        //GET BLUETOOTH ADAPTER
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //GET SENSOR MANAGER FOR ACCELEROMETER
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //SETUP CLICK LISTENERS
        setupListenersLandscape();
    }

    private void onResumeLandscape(){
        //GET SENSOR MANAGER FOR ACCELEROMETER
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

}
