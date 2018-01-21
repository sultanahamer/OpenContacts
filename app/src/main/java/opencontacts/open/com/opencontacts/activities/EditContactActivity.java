package opencontacts.open.com.opencontacts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.orm.PhoneNumber;
import opencontacts.open.com.opencontacts.utils.ContactsDBHelper;

public class EditContactActivity extends AppCompatActivity {
    Contact contact = null;
    public static final String INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT = "add_new_contact";
    public static final String INTENT_EXTRA_CONTACT_CONTACT_DETAILS = "contact_details";
    public static final String INTENT_EXTRA_STRING_PHONE_NUMBER = "phone_number";
    TextView textView_firstName;
    TextView textView_lastName;
    TextView textView_mobileNumber;
    private boolean addingNewContact = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        RelativeLayout rlayout  = (RelativeLayout) findViewById(R.id.relative_layout_contact_details);
        textView_firstName = ((TextView)findViewById(R.id.editFirstName));
        textView_lastName = ((TextView)findViewById(R.id.editLastName));
        textView_mobileNumber = ((TextView)findViewById(R.id.editPhoneNumber));

        Intent intent = getIntent();
        if(intent.getBooleanExtra(INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, false)) {
            addingNewContact = true;
            textView_mobileNumber.setText(intent.getStringExtra(INTENT_EXTRA_STRING_PHONE_NUMBER));
        }
        else{
            contact = (Contact) intent.getSerializableExtra(INTENT_EXTRA_CONTACT_CONTACT_DETAILS);
            if(contact.getId() == -1){
                Toast.makeText(this, R.string.error_while_loading_contact, Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
            textView_firstName.setText(contact.getFirstName());
            textView_lastName.setText(contact.getLastName());
            textView_mobileNumber.setText(contact.getPhoneNumber());
        }

    }
    public void saveContact(View view) {
        String firstName = String.valueOf(textView_firstName.getText());
        String lastName = String.valueOf(textView_lastName.getText());
        String phoneNumber = String.valueOf(textView_mobileNumber.getText());
        opencontacts.open.com.opencontacts.orm.Contact dbContact;
        if(addingNewContact)
            dbContact = addNewContact(firstName, lastName, phoneNumber);
        else
            dbContact = updateExistingContact(firstName, lastName, phoneNumber);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        Intent result = new Intent();
        result.putExtra(MainActivity.INTENT_EXTRA_LONG_CONTACT_ID, dbContact.getId());
        setResult(RESULT_OK, result);
        finish();
    }

    @NonNull
    private opencontacts.open.com.opencontacts.orm.Contact updateExistingContact(String firstName, String lastName, String phoneNumber) {
        opencontacts.open.com.opencontacts.orm.Contact dbContact;
        dbContact = ContactsDBHelper.getContactWithId(contact.getId());
        dbContact.firstName = firstName;
        dbContact.lastName = lastName;
        dbContact.save();
        if(!contact.getPhoneNumber().equals(phoneNumber)){
            ContactsDBHelper.updatePhoneNumber(dbContact, contact.getPhoneNumber(), phoneNumber);
        }
        return dbContact;
    }

    @NonNull
    private opencontacts.open.com.opencontacts.orm.Contact addNewContact(String firstName, String lastName, String phoneNumber) {
        opencontacts.open.com.opencontacts.orm.Contact dbContact;
        dbContact = new opencontacts.open.com.opencontacts.orm.Contact(firstName, lastName);
        dbContact.save();
        new PhoneNumber(phoneNumber, dbContact).save();
        return dbContact;
    }
}
