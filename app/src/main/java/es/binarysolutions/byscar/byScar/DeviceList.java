package es.binarysolutions.byscar.byScar;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import es.binarysolutions.byscar.Classes.Utils;
import es.binarysolutions.byscar.R;

/**
 * DeviceList
 *
 * ACTIVITY THAT SHOWS A LIST OF BLUETOOTH PAIRED DEVICES
 *
 */
public class DeviceList extends AppCompatActivity {

    private SharedPreferences preferences;
    private BluetoothAdapter btAdapter = null;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ListView list_of_devices;
    private TextView txvConnecting;

    /**
     * onCreate method
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //RECOVER PREFERENCES
        preferences=getSharedPreferences(byScar.BYSCAR_PREFERENCES,MODE_PRIVATE);
        Utils.changeLocale(getApplicationContext(),preferences.getString(byScar.BYSCAR_LANGUAGE,"ca")); //CHANGE LOCALE

        setContentView(R.layout.activity_device_list);
        setTitle(getString(R.string.deviceListTitle));

        //CHARGE LANDSCAPE OR PORTRAIT MODE
        if(preferences.getBoolean(byScar.BYSCAR_BUTTONS_MODE,true)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        //CHECK ADAPTER
        checkBTState();

        //INITIALIZA ARRAY ADAPTER FOR PAIRED DEVICES
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.list_devices);

        //RECOVER TEXTVIEW THAT SHOWS CONNECTING...
        txvConnecting=(TextView)findViewById(R.id.txvConnecting);

        //RECOVER LISTVIEW AND ASSIGN ADAPTER AND ONCLICK FUNCTION
        list_of_devices = (ListView) findViewById(R.id.lvDevices);
        list_of_devices.setAdapter(mPairedDevicesArrayAdapter);
        list_of_devices.setOnItemClickListener(mDeviceClickListener);

        //GET THE LOCAL BLUETOOTH ADAPTER
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //GET A SET OF CURRENTLY PAIRED DEVICES AND APPEND TO 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        //ADD PREVIOUSLY PAIRED DEVICES TO THE ARRAY
        if (pairedDevices.size() > 0) {
            findViewById(R.id.txvDevices).setVisibility(View.VISIBLE);//MAKE TITLE VIEWABLE
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }

    }

    /**
     * onActivityResult method
     *
     * we uses this method to compare the result from activate bluetooth intent
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //CHECK RESULT OF BLUETOOTH INTENT
        switch (requestCode){
            case 1:
                //IF BLUETOOTH ISN'T ENABLED, ASK TO USER IF IT WANT TO CONNECT OR CLOSE APPLICATION
                if(!btAdapter.isEnabled()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(this.getResources().getString(R.string.no_has_activat_el_bluetooth));
                    builder.setMessage(this.getResources().getString(R.string.el_bluetooth_es_necessari));
                    builder.setCancelable(false);
                    builder.setPositiveButton(this.getResources().getString(R.string.Activar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, 1);
                        }
                    });
                    builder.setNegativeButton(this.getResources().getString(R.string.NoActivar), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //IF THE USER CLOSE APPLICATION WE RETURN -1 AND FINISH THIS ACTIVITY
                            setResult(-1);
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    if (dialog != null){
                        dialog.show();
                    }
                }
                else{
                    Intent intent=getIntent();
                    startActivity(intent);
                    finish();
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * onConfigurationChanged methodç
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /*-------------------------------------------------*/
    /*-----------------PRIVATE METHODS-----------------*/
    /*-------------------------------------------------*/

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {
        //Get bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        //If device doesn't have bluetooth we show a message and return -1 to close application
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), this.getResources().getString(R.string.el_dispositiu_no_soporta_bluetooth), Toast.LENGTH_SHORT).show();
            setResult(-1);
        } else {
            //If bluetooth adapter is already activated we continue
            if (btAdapter.isEnabled()) {}
            //Else we needed so we asked user that it want to do
            else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    // Set up on-click listener for the list (nicked this - unsure)
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            txvConnecting.setVisibility(View.VISIBLE);//MAKE TEXTVIEW CONNECTING MESSAGE VISIBLE

            // Get the device MAC address, which is the last 17 chars in the View,
            // and device name, which is the first 5 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            String name=(info.substring(0,5)).trim();
            //If we want to connect to hc-06 bluetooth module we can
            // but if isn't hc-06 bluetooth module we show a message
            if(name.equalsIgnoreCase("HC-06")) {
                //Create preferences editor
                SharedPreferences.Editor editor=preferences.edit();

                //Put MAC address in shared preferences
                editor.putString(byScar.EXTRA_DEVICE_ADDRESS,address.trim());
                //PUT BYSCAR_PREFERENCES_NO_THREAD IN FALSE MODE
                editor.putBoolean(byScar.BYSCAR_PREFERENCES_NO_THREAD,true);
                editor.commit();

                //Set result of activity and close this activity
                Intent i = getIntent();
                setResult(1, i);
                finish();
            }
            else{
                txvConnecting.setVisibility(View.INVISIBLE);//MAKE TEXTVIEW CONNECTING MESSAGE INVISIBLE
                //Show a message
                Toast.makeText(v.getContext(),getResources().getString(R.string.no_es_el_dispositiu),Toast.LENGTH_SHORT).show();
            }
        }
    };
}
