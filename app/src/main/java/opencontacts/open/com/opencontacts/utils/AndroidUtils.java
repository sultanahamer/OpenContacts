package opencontacts.open.com.opencontacts.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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

    public static float dpToPixels(int dp) {
        return Resources.getSystem().getDisplayMetrics().density * dp;
    }

    public static void showSoftKeyboard(View view, Context context) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void call(String number, Context context) {
        Intent callIntent = getCallIntent(number, context);
        context.startActivity(callIntent);
    }

    @NonNull
    public static Intent getCallIntent(String number, Context context) {
        Uri numberUri = Uri.parse("tel:" + number);
        Intent callIntent;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            callIntent = new Intent(Intent.ACTION_DIAL, numberUri);
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        else{
            callIntent = new Intent(Intent.ACTION_CALL, numberUri);
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return callIntent;
    }

    public static void message(String number, Context context){
        context.startActivity(getMessageIntent(number));
    }

    public static Intent getMessageIntent(String number) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static Intent getIntentToShowContactDetails(Contact selectedContact, Context context){
     return new Intent(context, ContactDetailsActivity.class)
                    .putExtra(EditContactActivity.INTENT_EXTRA_CONTACT_CONTACT_DETAILS, (Serializable) selectedContact)
                    .putExtra(MainActivity.INTENT_EXTRA_LONG_CONTACT_ID, selectedContact.getId());
    }
    public static SharedPreferences getAppsSharedPreferences(Context context){
        return context.getSharedPreferences(context.getString(R.string.app_name), context.MODE_PRIVATE);
    }

    public static Intent getIntentToAddContact(String phoneNumber, Context context){
        return new Intent(context, EditContactActivity.class)
            .putExtra(EditContactActivity.INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, true)
            .putExtra(EditContactActivity.INTENT_EXTRA_STRING_PHONE_NUMBER, phoneNumber);
    }

    public static void showAlert(Context context, String title, String message){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Okay", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void copyToClipboard(String text, Context context) {

        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
                    .getSystemService(context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                    .getSystemService(context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText(null, text);
            clipboard.setPrimaryClip(clip);
        }

    }
}
