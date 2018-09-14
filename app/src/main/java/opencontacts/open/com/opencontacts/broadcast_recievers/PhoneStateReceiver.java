package opencontacts.open.com.opencontacts.broadcast_recievers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.provider.CallLog;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Random;

import opencontacts.open.com.opencontacts.CallLogLoader;
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.activities.MainActivity;
import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.orm.Contact;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;

/**
 * Created by sultanm on 7/30/17.
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    private static View drawOverIncomingCallLayout = null;
    private static boolean isCallRecieved;
    private static Contact callingContact;
    private static String incomingNumber;
    public static String unknown = "Unknown";


    @Override
    public void onReceive(final Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
            isCallRecieved = false;
            incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            callingContact = ContactsDataStore.getContact(incomingNumber);
            if(callingContact == null)
                callingContact = new Contact(unknown, incomingNumber);
            drawContactID(context, callingContact);
        }
        else if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
            removeCallerIdDrawing(context);
            isCallRecieved = true;
        }
        else if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
            removeCallerIdDrawing(context);
            new Handler().postDelayed(new Runnable() {// give android some time to write call log
                @Override
                public void run() {
                    try{
                        if(isCallRecieved)
                            return;
                        CallLogEntry callLogEntry = new CallLogLoader().loadCallLog(context).get(0);
                        if(!callLogEntry.getCallType().equals(String.valueOf(CallLog.Calls.MISSED_TYPE)))
                            return;
                    }
                    catch (Exception e){}
                    PendingIntent pendingIntentToLaunchApp = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent pendingIntentToCall = PendingIntent.getActivity(context, 0, AndroidUtils.getCallIntent(incomingNumber, context), PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent pendingIntentToMessage = PendingIntent.getActivity(context, 0, AndroidUtils.getMessageIntent(incomingNumber), PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_phone_missed_black_24dp)
                                    .setContentTitle("Missed Call")
                                    .setTicker("missed call from " + callingContact.firstName + " " + callingContact.lastName)
                                    .setContentText(callingContact.firstName + " " + callingContact.lastName)
                                    .addAction(R.drawable.ic_call_black_24dp, "Call", pendingIntentToCall)
                                    .addAction(R.drawable.ic_chat_black_24dp, "Message", pendingIntentToMessage)
                                    .setContentIntent(pendingIntentToLaunchApp);
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(new Random().nextInt(), mBuilder.build());
                }
            }, 3000);
        }
    }

    private void drawContactID(Context context, Contact callingContact) {
        WindowManager windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        LayoutInflater layoutinflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        drawOverIncomingCallLayout = layoutinflater.inflate(R.layout.draw_over_incoming_call, null);
        TextView contactName = (TextView) drawOverIncomingCallLayout.findViewById(R.id.name_of_contact);
        contactName.setText(callingContact.toString());
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.x = 0;
        layoutParams.y = 0;
        windowManager.addView(drawOverIncomingCallLayout, layoutParams);
    }

    private void removeCallerIdDrawing(Context context) {
        if(drawOverIncomingCallLayout == null)
            return;
        WindowManager windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        windowManager.removeView(drawOverIncomingCallLayout);
        drawOverIncomingCallLayout = null;
    }
}
