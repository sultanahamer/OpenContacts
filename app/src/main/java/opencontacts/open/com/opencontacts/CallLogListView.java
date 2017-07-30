package opencontacts.open.com.opencontacts;

import android.content.Context;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.utils.Common;

/**
 * Created by sultanm on 7/31/17.
 */

public class CallLogListView extends ListView {
    List <CallLogEntry> callLogEntries;
    CallLogListView thisClass = this;
    Context context;
    ArrayAdapter<CallLogEntry> adapter;
    public CallLogListView(final Context context, final View.OnClickListener callContact, final View.OnClickListener messageContact, final View.OnClickListener editContact) {
        super(context);
        this.context = context;
        setTextFilterEnabled(true);
        callLogEntries = CallLogEntry.find(CallLogEntry.class, null, null, null, "date desc", "100");
        adapter = new ArrayAdapter<CallLogEntry>(context, R.layout.call_log_entry, callLogEntries){
            private LayoutInflater layoutInflater = LayoutInflater.from(context);
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                CallLogEntry callLogEntry = getItem(position);
                if(convertView == null)
                    convertView = layoutInflater.inflate(R.layout.call_log_entry, parent, false);
                ((TextView) convertView.findViewById(R.id.textview_full_name)).setText(callLogEntry.getName());
                ((TextView) convertView.findViewById(R.id.textview_phone_number)).setText(callLogEntry.getPhoneNumber());
                ((ImageButton)convertView.findViewById(R.id.button_call)).setOnClickListener(callContact);
                ((ImageButton)convertView.findViewById(R.id.button_message)).setOnClickListener(messageContact);
                if(callLogEntry.getCallType().equals(String.valueOf(CallLog.Calls.INCOMING_TYPE)))
                    ((ImageView)convertView.findViewById(R.id.image_button_call_type)).setImageResource(android.R.drawable.sym_call_incoming);
                else if(callLogEntry.getCallType().equals(String.valueOf(CallLog.Calls.OUTGOING_TYPE)))
                    ((ImageView)convertView.findViewById(R.id.image_button_call_type)).setImageResource(android.R.drawable.sym_call_outgoing);
                else if(callLogEntry.getCallType().equals(String.valueOf(CallLog.Calls.MISSED_TYPE)))
                    ((ImageView)convertView.findViewById(R.id.image_button_call_type)).setImageResource(android.R.drawable.sym_call_missed);
                ((TextView)convertView.findViewById(R.id.duration_of_call)).setText(Common.getDurationInMinsAndSecs(Integer.valueOf(callLogEntry.getDuration())));
                convertView.setTag(position);
                convertView.setOnClickListener(editContact);
                return convertView;
            }
        };
        this.setAdapter(adapter);
    }
}
