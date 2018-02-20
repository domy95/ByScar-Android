package es.binarysolutions.byscar.CustomViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by domy9 on 27/05/2016.
 */

/**
 * TEXTVIEWSPLASH
 *
 * TEXTVIEW WITH AN ESPECIFIED FONT
 *
 */
public class textViewSplash extends TextView {

    public textViewSplash(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/irregular.ttf"));
    }
}
