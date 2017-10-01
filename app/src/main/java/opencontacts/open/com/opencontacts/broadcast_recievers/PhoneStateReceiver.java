package opencontacts.open.com.opencontacts.broadcast_recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.orm.Contact;
import opencontacts.open.com.opencontacts.utils.ContactsDBHelper;

/**
 * Created by sultanm on 7/30/17.
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    private static View drawOverIncomingCallLayout = null;
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Contact callingContact = ContactsDBHelper.getContact(incomingNumber);
            if(callingContact == null)
                drawContactID(context, new Contact("Unknown", ""));
            else
                drawContactID(context, callingContact);
        }
        else if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
            removeCallerIdDrawing(context);
        }
        else if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
            removeCallerIdDrawing(context);
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
