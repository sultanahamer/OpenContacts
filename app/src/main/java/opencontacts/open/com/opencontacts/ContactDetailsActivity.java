package opencontacts.open.com.opencontacts;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.ContactsDBHelper;


public class ContactDetailsActivity extends Activity {
    private Contact contact;
    private long contactId;
    private Toolbar toolbar;

    private View.OnClickListener callContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AndroidUtils.call(getSelectedMobileNumber(v), getApplicationContext());
        }
    };
    private View.OnClickListener messageContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AndroidUtils.message(getSelectedMobileNumber(v), getApplicationContext());
        }
    };
    private String getSelectedMobileNumber(View v){
        int position = (Integer)((View)v.getParent()).getTag();
        return contact.getPhoneNumbers().get(position);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Intent intent = getIntent();
        contact = (Contact) intent.getSerializableExtra(EditContactActivity.INTENT_EXTRA_CONTACT_CONTACT_DETAILS);
        contactId = intent.getLongExtra(EditContactActivity.INTENT_EXTRA_LONG_CONTACT_ID, -1);
        toolbar.setTitle(contact.getName());
        if(contactId == -1){
            Toast.makeText(this, R.string.error_while_loading_contact, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        ((ImageButton)findViewById(R.id.image_button_delete_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactsDBHelper.deleteContact(contactId);
                setResult(RESULT_OK);
                finish();
            }
        });

        ((ImageButton)findViewById(R.id.image_button_edit_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AndroidUtils.showContactDetails(contact, getApplicationContext());
//                setResult(RESULT_OK);
//                finish();
            }
        });

        ((ImageButton)findViewById(R.id.image_button_export_to_contacts_app)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportToContactsApp();
            }
        });

        ListView listView = (ListView) findViewById(R.id.listview_contact_details);
        final List<String> mobileNumbers = contact.getPhoneNumbers();
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.contact_details_row, mobileNumbers){
            private LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = layoutInflater.inflate(R.layout.contact_details_row, parent, false);
                ((TextView) convertView.findViewById(R.id.textview_phone_number)).setText(mobileNumbers.get(position));
                convertView.findViewById(R.id.button_call).setOnClickListener(callContact);
                convertView.findViewById(R.id.button_message).setOnClickListener(messageContact);
                convertView.setTag(position);
                return convertView;
            }
        });
    }

    public void exportToContactsApp() {
        Intent exportToContactsAppIntent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);

        ArrayList<ContentValues> data = new ArrayList<ContentValues>();
        for(String phoneNumber : contact.getPhoneNumbers()){
            ContentValues row = new ContentValues();
            row.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            row.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
            data.add(row);
        }
        exportToContactsAppIntent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data)
                .putExtra(ContactsContract.Intents.Insert.NAME, contact.getName());
        startActivity(exportToContactsAppIntent);
    }
}
