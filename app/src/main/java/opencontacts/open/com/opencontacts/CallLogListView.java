package opencontacts.open.com.opencontacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import opencontacts.open.com.opencontacts.activities.MainActivity;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.Common;

import static opencontacts.open.com.opencontacts.data.datastore.DomainUtils.getContact;

/**
 * Created by sultanm on 7/31/17.
 */

public class CallLogListView extends ListView {
    List <CallLogEntry> callLogEntries;
    Context context;
    ArrayAdapter<CallLogEntry> adapter;
    public CallLogListView(final Context context) {
        super(context);
        this.context = context;

        callLogEntries = CallLogEntry.find(CallLogEntry.class, null, null, null, "date desc", "100");

        final OnClickListener callContact = new OnClickListener() {
            @Override
            public void onClick(View v) {
                CallLogEntry callLogEntry = (CallLogEntry) v.getTag();
                AndroidUtils.call(callLogEntry.getPhoneNumber(), context);
            }
        };
        final OnClickListener messageContact = new OnClickListener() {
            @Override
            public void onClick(View v) {
                CallLogEntry callLogEntry = (CallLogEntry) ((View)v.getParent()).getTag();
                AndroidUtils.message(callLogEntry.getPhoneNumber(), context);
            }
        };
        final OnClickListener addContact = new OnClickListener() {
            @Override
            public void onClick(View v) {
                CallLogEntry callLogEntry = (CallLogEntry) ((View)v.getParent()).getTag();
                Intent intentToAddContact = AndroidUtils.getIntentToAddContact(callLogEntry.getPhoneNumber(), context);
                ((Activity)context).startActivityForResult(intentToAddContact, MainActivity.REQUESTCODE_FOR_ADD_CONTACT);
            }
        };
        final OnClickListener showContactDetails = new OnClickListener() {
            @Override
            public void onClick(View v) {
                CallLogEntry callLogEntry = (CallLogEntry) ((View)v.getParent()).getTag();
                long contactId = callLogEntry.getContactId();
                if(contactId == -1)
                    return;
                Contact contact = getContact(contactId);
                if(contact == null)
                    return;
                Intent showContactDetails = AndroidUtils.getIntentToShowContactDetails(getContact(contactId), CallLogListView.this.context);
                ((Activity)(CallLogListView.this.context)).startActivityForResult(showContactDetails, MainActivity.REQUESTCODE_FOR_SHOW_CONTACT_DETAILS);
            }
        };

        final OnLongClickListener copyPhoneNumberToClipboard = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CallLogEntry callLogEntry = (CallLogEntry) v.getTag();
                AndroidUtils.copyToClipboard(callLogEntry.getPhoneNumber(), context);
                Toast.makeText(context, R.string.copied_phonenumber_to_clipboard, Toast.LENGTH_SHORT).show();
                return true;
            }
        };

        adapter = new ArrayAdapter<CallLogEntry>(CallLogListView.this.context, R.layout.call_log_entry, callLogEntries){
            private LayoutInflater layoutInflater = LayoutInflater.from(CallLogListView.this.context);
            @NonNull
            @Override
            public View getView(int position, View reusableView, ViewGroup parent) {
                CallLogEntry callLogEntry = getItem(position);
                if(reusableView == null)
                    reusableView = layoutInflater.inflate(R.layout.call_log_entry, parent, false);
                ((TextView) reusableView.findViewById(R.id.textview_full_name)).setText(callLogEntry.getName());
                ((TextView) reusableView.findViewById(R.id.textview_phone_number)).setText(callLogEntry.getPhoneNumber());
                ((ImageButton)reusableView.findViewById(R.id.button_message)).setOnClickListener(messageContact);
                if(callLogEntry.getCallType().equals(String.valueOf(CallLog.Calls.INCOMING_TYPE)))
                    ((ImageView)reusableView.findViewById(R.id.image_view_call_type)).setImageResource(R.drawable.ic_call_received_black_24dp);
                else if(callLogEntry.getCallType().equals(String.valueOf(CallLog.Calls.OUTGOING_TYPE)))
                    ((ImageView)reusableView.findViewById(R.id.image_view_call_type)).setImageResource(R.drawable.ic_call_made_black_24dp);
                else if(callLogEntry.getCallType().equals(String.valueOf(CallLog.Calls.MISSED_TYPE)))
                    ((ImageView)reusableView.findViewById(R.id.image_view_call_type)).setImageResource(R.drawable.ic_call_missed_outgoing_black_24dp);
                ((TextView)reusableView.findViewById(R.id.text_view_duration)).setText(Common.getDurationInMinsAndSecs(Integer.valueOf(callLogEntry.getDuration())));
                ((TextView)reusableView.findViewById(R.id.text_view_sim)).setText(String.valueOf(callLogEntry.getSimId()));
                String timeStampOfCall = new java.text.SimpleDateFormat("dd/MM  hh:mm a", Locale.getDefault()).format(new Date(Long.parseLong(callLogEntry.getDate())));
                ((TextView)reusableView.findViewById(R.id.text_view_timestamp)).setText(timeStampOfCall);
                View addButton = reusableView.findViewById(R.id.image_button_add_contact);
                View infoButton = reusableView.findViewById(R.id.button_info);

                if(callLogEntry.getContactId() == -1){
                    addButton.setOnClickListener(addContact);
                    addButton.setVisibility(View.VISIBLE);
                    infoButton.setVisibility(View.INVISIBLE);
                }
                else{
                    addButton.setVisibility(View.INVISIBLE);
                    infoButton.setVisibility(View.VISIBLE);
                    infoButton.setOnClickListener(showContactDetails);
                }
                reusableView.setTag(callLogEntry);
                reusableView.setOnClickListener(callContact);
                reusableView.setOnLongClickListener(copyPhoneNumberToClipboard);
                return reusableView;
            }
        };
        this.setAdapter(adapter);
    }

    public void addNewEntries(List<CallLogEntry> newCallLogEntries) {
        for(int i=0,totalEntries = newCallLogEntries.size(); i < totalEntries; i++){
            adapter.insert(newCallLogEntries.get(i), i);
        }
        adapter.notifyDataSetChanged();
    }
}
