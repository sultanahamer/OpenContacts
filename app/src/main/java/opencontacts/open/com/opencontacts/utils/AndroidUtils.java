package opencontacts.open.com.opencontacts.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.Serializable;

import opencontacts.open.com.opencontacts.activities.ContactDetailsActivity;
import opencontacts.open.com.opencontacts.activities.EditContactActivity;
import opencontacts.open.com.opencontacts.activities.MainActivity;
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.domain.Contact;

/**
 * Created by sultanm on 7/17/17.
 */

public class AndroidUtils {
    public static void showSoftKeyboard(View view, Context context) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
    public static void call(String number, Context context){
        Uri numberUri = Uri.parse("tel:" + number);
        Intent callIntent = new Intent(Intent.ACTION_DIAL, numberUri);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);
    }

    public static void message(String number, Context context){
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static Intent getIntentToShowContactDetails(Contact selectedContact, Context context){
     return new Intent(context, ContactDetailsActivity.class)
                    .putExtra(EditContactActivity.INTENT_EXTRA_CONTACT_CONTACT_DETAILS, (Serializable) selectedContact)
                    .putExtra(MainActivity.INTENT_EXTRA_LONG_CONTACT_ID, selectedContact.getId());
    }
    public static SharedPreferences getAppsSharedPreferences(Context context){
        return context.getSharedPreferences(context.getString(R.string.app_name), context.MODE_PRIVATE);
    }
}
