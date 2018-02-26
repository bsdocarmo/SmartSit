package com.habitus.smartsit;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by brunodocarmo on 25/02/18.
 */

public class Utilities {

    public static void toast(final Context context, final String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
