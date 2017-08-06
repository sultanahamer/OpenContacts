package opencontacts.open.com.opencontacts;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.SparseIntArray;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.ContactsDBHelper;

import static android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE;

/**
 * Created by sultanm on 8/5/17.
 */

public class CallLogLoader {
    private SparseIntArray simsInfo = null;

    private void createSimsInfo(Context context) {
        simsInfo = new SparseIntArray();
        List<SubscriptionInfo> listOfSubscriptions = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            listOfSubscriptions = ((SubscriptionManager) context.getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE)).getActiveSubscriptionInfoList();
            for(int i=0; i<listOfSubscriptions.size(); i++){
                simsInfo.put(i, listOfSubscriptions.get(i).getSubscriptionId());
            }
        }
    }
    private String preferenceLastCallLogSavedDate = "preference_last_call_log_saved_date";

    public void loadCallLog(Context context) {
        if(simsInfo == null)
            createSimsInfo(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(context, R.string.grant_read_call_logs_permission, Toast.LENGTH_LONG).show();
            return;
        }

        CallLogEntry.saveInTx(getCallLogEntries(context));
    }

    private List<CallLogEntry> getCallLogEntries(Context context){
        Cursor c;
        String num, date, duration, callType, subscriptionId;
        ArrayList<CallLogEntry> callLogEntries = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
            c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.PHONE_ACCOUNT_ID}, CallLog.Calls.DATE + " > ?", new String[]{getLastSavedCallLogDate(context)}, CallLog.Calls.DATE + " DESC");
            if(c.getCount() == 0)
                return callLogEntries;
            int columnIndexForNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
            int columnIndexForDuration = c.getColumnIndex(CallLog.Calls.DURATION);
            int columnIndexForDate = c.getColumnIndex(CallLog.Calls.DATE);
            int columnIndexForCallType = c.getColumnIndex(CallLog.Calls.TYPE);
            int columnIndexForSubscriptionId = c.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID);
            while(c.moveToNext()){
                num = c.getString(columnIndexForNumber);// for  number
                duration = c.getString(columnIndexForDuration);// for duration
                date = c.getString(columnIndexForDate);
                callType = c.getString(columnIndexForCallType);// for call type, Incoming or out going
                subscriptionId = c.getString(columnIndexForSubscriptionId);
                opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getContact(num);
                if(contact == null)
                    callLogEntries.add(new CallLogEntry(null, (long)-1, num, duration, callType, date, subscriptionId));
                else
                    callLogEntries.add(new CallLogEntry(contact.toString(), contact.getId(), num, duration, callType, date, subscriptionId));
            }
            c.moveToFirst();
            setLastSavedCallLogDate(c.getString(columnIndexForDate), context);
        }

        else {
            c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.TYPE, CallLog.Calls.DATE}, CallLog.Calls.DATE + " > ?", new String[]{getLastSavedCallLogDate(context)}, CallLog.Calls.DATE + " DESC");
            if(c.getCount() == 0)
                return callLogEntries;
            int columnIndexForNumber = c.getColumnIndex(CallLog.Calls.NUMBER);
            int columnIndexForDuration = c.getColumnIndex(CallLog.Calls.DURATION);
            int columnIndexForDate = c.getColumnIndex(CallLog.Calls.DATE);
            int columnIndexForCallType = c.getColumnIndex(CallLog.Calls.TYPE);
            int columnIndexForSubscriptionId = c.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID);
            while(c.moveToNext()){
                num = c.getString(columnIndexForNumber);// for  number
                duration = c.getString(columnIndexForDuration);// for duration
                date = c.getString(columnIndexForDate);
                callType = c.getString(columnIndexForCallType);// for call type, Incoming or out going
                subscriptionId = c.getString(columnIndexForSubscriptionId);

                opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getContact(num);
                if(contact == null)
                    callLogEntries.add(new CallLogEntry(null, (long)-1, num, duration, callType, date, subscriptionId));
                else
                    callLogEntries.add(new CallLogEntry(contact.toString(), contact.getId(), num, duration, callType, date, subscriptionId));
            }
            c.moveToFirst();
            setLastSavedCallLogDate(c.getString(columnIndexForDate), context);
        }


        return callLogEntries;
    }

    private String getLastSavedCallLogDate(Context context) {
        return AndroidUtils.getAppsSharedPreferences(context).getString(preferenceLastCallLogSavedDate, "0");
    }

    private void setLastSavedCallLogDate(String date, Context context) {
        AndroidUtils.getAppsSharedPreferences(context).edit().putString(preferenceLastCallLogSavedDate, date).apply();
    }


}
