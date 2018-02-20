package es.binarysolutions.byscar.Threads;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.OutputStream;

import es.binarysolutions.byscar.Classes.Utils;
import es.binarysolutions.byscar.R;

/**
 * Created by domy9 on 29/05/2016.
 */

/**
 * THREAD TO SEND DATA TO ARDUINO CAR
 */
public class ConnexionBluetooth extends AsyncTask<Void,Void,Void>{
    //DECLARE OUTPUTSTREAM TO SEND DATA TO CAR
    private final OutputStream mmOutStream;
    //DECLARE CONTEXT TO LOCK VIEWS
    private Context ctx;
    //DECLARE ARRAY OF VIEWS
    private View[] views;
    //DECLARE TOGGLEBUTTON (ON/OFF)
    private ToggleButton toggleButton;

    /**
     * CONSTRUCTOR FOR THREAD
     *
     * RECOVER OUTPUTSTREAM FROM BLUETOOTH SOCKET
     *
     * @param ctx
     * @param socket
     * @param views
     * @param toggleButton
     */
    public ConnexionBluetooth(Context ctx, BluetoothSocket socket, View[] views, ToggleButton toggleButton){
        //RECOVER OBJECTS THAT WE SEND IN CONSTRUCTOR
        this.ctx=ctx;
        this.views=views;
        this.toggleButton=toggleButton;

        //CREATE NEW OUTPUTSTREAM
        OutputStream tmpOut = null;
        try {
            //GETTING OUTPUTSTREAM FROM BLUETOOTH SOCKET
            tmpOut = socket.getOutputStream();
        } catch (Exception e) {
            Log.e("ERROR","ERRORR AL RECUPERAR L'OUTPUT STREAM DEL SOCKET");
            e.printStackTrace();
        }

        //ASSIGN IN GLOBAL OUTPUTSTREAM AN OUTPUTSTREAM THAT WE RECOVER FROM BLUETOOTH SOCKET
        mmOutStream = tmpOut;
    }

    /**
     * DOINBACKGROUND
     *
     * WE DON'T USE THIS METHOD
     *
     * @param params
     * @return
     */
    @Override
    protected Void doInBackground(Void... params) {
        //WE DON'T USE THAT METHOD
        return null;
    }

    /**
     * WRITE METHOD
     *
     * WE USE THIS METHOD TO SEND DATA TO ARDUINO CAR
     *
     * @param input
     */
    public void write(String input) {
        //CONVERTS ENTERED STRING INTO BYTES
        byte[] msgBuffer = input.getBytes();
        try {
            //WRITE BYTES OVER BLUETOOTH CONNECTION VIA OUTPUTSTREAM
            mmOutStream.write(msgBuffer);
        } catch (Exception e) {
            //IF IT CAN'T WRITE (OR SEND) DATA, SHOW A MESSAGE
            Toast.makeText(ctx, ctx.getResources().getString(R.string.ha_fallat_la_connexio), Toast.LENGTH_SHORT).show();
            //LOCK ARRAY OF VIEWS
            Utils.lockViews(views);
            //LOCK TOGGLE BUTTON (ON/OFF)
            toggleButton.setEnabled(false);
        }
    }
}
