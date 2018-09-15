package opencontacts.open.com.opencontacts.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;


public class ContactDetailsActivity extends AppCompatActivity {
    private long contactId;
    private Contact contact;
    private Toolbar toolbar;

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
        return v.getTag().toString();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AndroidUtils.setBackButtonInToolBar(toolbar, this);
        Intent intent = getIntent();
        contactId = intent.getLongExtra(MainActivity.INTENT_EXTRA_LONG_CONTACT_ID, -1);
        if(contactId == -1)
            showInvalidContactErrorAndExit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        contact = ContactsDataStore.getContactWithId(contactId);
        if(contact == null)
            showInvalidContactErrorAndExit();
        setUpUI();
    }

    private void showInvalidContactErrorAndExit() {
        Toast.makeText(this, R.string.error_while_loading_contact, Toast.LENGTH_LONG).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void setUpUI() {
        toolbar.setTitle(contact.getName());
        ListView listView = (ListView) findViewById(R.id.listview_phone_numbers);
        final List<String> mobileNumbers = contact.getPhoneNumbers();
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.contact_details_row, mobileNumbers){
            private LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = layoutInflater.inflate(R.layout.contact_details_row, parent, false);
                String mobileNumber = mobileNumbers.get(position);
                ((TextView) convertView.findViewById(R.id.textview_phone_number)).setText(mobileNumber);
                convertView.findViewById(R.id.button_call).setOnClickListener(callContact);
                convertView.findViewById(R.id.button_message).setOnClickListener(messageContact);
                convertView.setOnLongClickListener(copyPhoneNumberToClipboard);
                convertView.setTag(mobileNumber);
                return convertView;
            }
        });
    }

    private void exportToContactsApp() {
        Intent exportToContactsAppIntent = AndroidUtils.getIntentToExportContactToNativeContactsApp(contact);
        startActivity(exportToContactsAppIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.contact_details_menu, menu);
        menu.findItem(R.id.image_button_export_to_contacts_app).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                exportToContactsApp();
                return true;
            }
        });
        menu.findItem(R.id.image_button_edit_contact).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent editContact = new Intent(ContactDetailsActivity.this, EditContactActivity.class);
                editContact.putExtra(EditContactActivity.INTENT_EXTRA_CONTACT_CONTACT_DETAILS, contact);
                ContactDetailsActivity.this.startActivity(editContact);
                return true;
            }
        });
        menu.findItem(R.id.image_button_delete_contact).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(ContactDetailsActivity.this)
                        .setMessage("Do you want to delete?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ContactsDataStore.removeContact(contact);
                                Toast.makeText(ContactDetailsActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton("No", null).show();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}