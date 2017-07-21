package opencontacts.open.com.opencontacts.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import opencontacts.open.com.opencontacts.Contact;

/**
 * Created by sultanm on 7/17/17.
 */

public class AndroidUtils {
    public static void call(String number, Context context){
        Uri numberUri = Uri.parse("tel:" + number);
        Intent callIntent = new Intent(Intent.ACTION_DIAL, numberUri);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);
    }

    public static void message(String number, Context context){
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
