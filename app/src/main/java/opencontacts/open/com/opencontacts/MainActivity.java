package opencontacts.open.com.opencontacts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.Serializable;

import opencontacts.open.com.opencontacts.utils.AndroidUtils;

import static opencontacts.open.com.opencontacts.ContactsListView.*;


public class MainActivity extends Activity {
    private int REQUESTCODE_FOR_ADD_CONTACT = 1;
    private int REQUESTCODE_FOR_UPDATE_CONTACT = 2;
    private int lastSelectedContactPosition = -1;
    ContactsListView contactsListView;
    private OnClickListener callContact = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Contact contact = contactsListView.getContactAt((Integer) ((View)v.getParent()).getTag());
            AndroidUtils.call(contact.phoneNumber, getApplicationContext());
        }
    };
    private OnClickListener messageContact = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Contact contact = contactsListView.getContactAt((Integer) ((View)v.getParent()).getTag());
            AndroidUtils.message(contact.phoneNumber, getApplicationContext());
        }
    };
    OnClickListener editContact = new OnClickListener() {
        @Override
        public void onClick(View v) {
            lastSelectedContactPosition = (Integer) v.getTag();
            Contact contact = contactsListView.getContactAt(lastSelectedContactPosition);
            editContact(contact);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout rlayout  = (LinearLayout) findViewById(R.id.contacts_holder);
        if(contactsListView == null)
            contactsListView = new ContactsListView(this, callContact, messageContact, editContact);
        rlayout.addView(contactsListView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_CANCELED)
            return;
        long contactId = intent.getLongExtra(EditContactActivity.INTENT_EXTRA_LONG_CONTACT_ID, -1);
        if(requestCode == REQUESTCODE_FOR_ADD_CONTACT && resultCode == RESULT_OK)
            contactsListView.addNewContactInView(contactId);
        else if (requestCode == REQUESTCODE_FOR_UPDATE_CONTACT && resultCode == RESULT_OK)
            try {
                if(intent.getBooleanExtra(EditContactActivity.INTENT_EXTRA_BOOLEAN_CONTACT_DELETED, false))
                    contactsListView.deleteContactAt(lastSelectedContactPosition);
                else
                    contactsListView.updateContactViewAt(lastSelectedContactPosition, contactId);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.error_while_saving_contact, Toast.LENGTH_LONG).show();
            }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void editContact(Contact selectedContact){
        Intent editContact = new Intent((Context) getApplication(), ContactDetailsActivity.class);
        editContact.putExtra(EditContactActivity.INTENT_EXTRA_CONTACT_CONTACT_DETAILS, (Serializable) selectedContact);
        editContact.putExtra(EditContactActivity.INTENT_EXTRA_LONG_CONTACT_ID, selectedContact.getId());
        startActivity(editContact);
    }

    public void addContact(View view) {
        Intent addContact = new Intent(this, EditContactActivity.class);
        addContact.putExtra(EditContactActivity.INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, true);
        addContact.putExtra("position", -1);
        startActivityForResult(addContact, REQUESTCODE_FOR_ADD_CONTACT);
    }
}
