package opencontacts.open.com.opencontacts.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.ContactsDBHelper;


public class ContactDetailsActivity extends AppCompatActivity {
    private Contact contact;
    private Toolbar toolbar;
    private int REQUESTCODE_FOR_EDIT_CONTACT = 1;

    private View.OnClickListener callContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AndroidUtils.call(getSelectedMobileNumber((View)v.getParent()), ContactDetailsActivity.this);
        }
    };
    private View.OnClickListener messageContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AndroidUtils.message(getSelectedMobileNumber((View)v.getParent()), ContactDetailsActivity.this);
        }
    };

    private View.OnLongClickListener copyPhoneNumberToClipboard = new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View v) {
            AndroidUtils.copyToClipboard(getSelectedMobileNumber(v), ContactDetailsActivity.this);
            Toast.makeText(ContactDetailsActivity.this, R.string.copied_phonenumber_to_clipboard, Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    private String getSelectedMobileNumber(View v){
        int position = (Integer)v.getTag();
        return contact.getPhoneNumbers().get(position);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Intent intent = getIntent();
        contact = (Contact) intent.getSerializableExtra(EditContactActivity.INTENT_EXTRA_CONTACT_CONTACT_DETAILS);
        toolbar.setTitle(contact.getName());
        setSupportActionBar(toolbar);
        if(contact.getId() == -1){
            Toast.makeText(this, R.string.error_while_loading_contact, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        ((ImageButton)findViewById(R.id.image_button_delete_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ContactDetailsActivity.this)
                        .setMessage("Do you want to delete?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ContactsDBHelper.deleteContact(contact.getId());
                                Toast.makeText(ContactDetailsActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                Intent result = new Intent();
                                result.putExtra(MainActivity.INTENT_EXTRA_LONG_CONTACT_ID, contact.getId());
                                result.putExtra(MainActivity.INTENT_EXTRA_BOOLEAN_CONTACT_DELETED, true);
                                setResult(RESULT_OK, result);
                                finish();
                            }
                        })
                        .setNegativeButton("No", null).show();
            }
        });

        ((ImageButton)findViewById(R.id.image_button_edit_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editContact = new Intent(ContactDetailsActivity.this, EditContactActivity.class);
                editContact.putExtra(EditContactActivity.INTENT_EXTRA_CONTACT_CONTACT_DETAILS, contact);
                ContactDetailsActivity.this.startActivityForResult(editContact, REQUESTCODE_FOR_EDIT_CONTACT);
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
                convertView.setOnLongClickListener(copyPhoneNumberToClipboard);
                convertView.setTag(position);
                return convertView;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_CANCELED)
            return;
        if(requestCode == REQUESTCODE_FOR_EDIT_CONTACT && resultCode == RESULT_OK){
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void exportToContactsApp() {
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
