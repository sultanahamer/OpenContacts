package opencontacts.open.com.opencontacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.Serializable;

import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;

import static opencontacts.open.com.opencontacts.ContactsListView.OnClickListener;


public class MainActivity extends Activity implements TextWatcher {
    private int REQUESTCODE_FOR_ADD_CONTACT = 1;
    private int REQUESTCODE_FOR_UPDATE_CONTACT = 2;
    private int lastSelectedContactPosition = -1;
    private Toolbar toolbar;
    private EditText searchBar;
    private ImageButton stopSearch;
    ContactsListView contactsListView;
    private OnClickListener callContact = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Contact contact = contactsListView.getContactAt((Integer) ((View)v.getParent()).getTag());
            AndroidUtils.call(contact.getPhoneNumber(), getApplicationContext());
        }
    };
    private OnClickListener messageContact = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Contact contact = contactsListView.getContactAt((Integer) ((View)v.getParent()).getTag());
            AndroidUtils.message(contact.getPhoneNumber(), getApplicationContext());
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        searchBar = (EditText) findViewById(R.id.text_edit_search_box);
        stopSearch = (ImageButton) findViewById(R.id.image_button_stop_search);
        toolbar.setTitle(R.string.app_name);
        if(contactsListView == null)
            contactsListView = new ContactsListView(this, callContact, messageContact, editContact);
        rlayout.addView(contactsListView);
        searchBar.addTextChangedListener(this);
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
        Intent editContact = new Intent(getApplication(), ContactDetailsActivity.class);
        editContact.putExtra(EditContactActivity.INTENT_EXTRA_CONTACT_CONTACT_DETAILS, (Serializable) selectedContact);
        editContact.putExtra(EditContactActivity.INTENT_EXTRA_LONG_CONTACT_ID, selectedContact.getId());
        startActivity(editContact);
    }

    public void addContact(View view) {
        Intent addContact = new Intent(this, EditContactActivity.class);
        addContact.putExtra(EditContactActivity.INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, true);
        startActivityForResult(addContact, REQUESTCODE_FOR_ADD_CONTACT);
    }

    public void searchContact(View view) {
        toolbar.setTitle("");
        searchBar.setVisibility(View.VISIBLE);
        stopSearch.setVisibility(View.VISIBLE);
        searchBar.requestFocus();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().equals(""))
            contactsListView.clearTextFilter();
        contactsListView.setFilterText(s.toString());
    }

    public void stopSearch(View view) {
        contactsListView.clearTextFilter();
        searchBar.setVisibility(View.GONE);
        stopSearch.setVisibility(View.GONE);
        toolbar.setTitle(R.string.app_name);
    }
}
