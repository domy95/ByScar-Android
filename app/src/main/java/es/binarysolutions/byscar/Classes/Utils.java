package es.binarysolutions.byscar.Classes;

import android.content.Context;
import android.view.View;

import java.util.Locale;

/**
 * Created by domy9 on 20/05/2016.
 */
public class Utils {
    /**
     * lockViews
     *
     * METHOD TO DISABLE SOME VIEWS FROM AN ARRAY
     *
     * @param views
     */
    public static void lockViews(View[] views){
        for(int i=0;i<views.length;i++){
            views[i].setEnabled(false);
        }
    }

    /**
     * unlockViews
     *
     * METHOD TO ENABLE SOME VIEWS FROM AN ARRAY
     *
     * @param views
     */
    public static void unlockViews(View[] views){
        for(int i=0;i<views.length;i++){
            views[i].setEnabled(true);
        }
    }

    /**
     * changeLocale
     *
     * METHOD TO CHANGE APPLICATION'S LOCALE
     *
     * @param ctx
     * @param idioma
     */
    public static void changeLocale(Context ctx, String idioma){
        try {
            android.content.res.Configuration configuration= new android.content.res.Configuration();
            configuration.locale=new Locale(idioma);
            ctx.getResources().updateConfiguration(configuration,null);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
