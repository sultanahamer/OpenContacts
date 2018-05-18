package opencontacts.open.com.opencontacts.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.ContactsDBHelper;

import static android.view.ViewGroup.LayoutParams.*;

public class EditContactActivity extends AppCompatActivity {
    Contact contact = null;
    public static final String INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT = "add_new_contact";
    public static final String INTENT_EXTRA_CONTACT_CONTACT_DETAILS = "contact_details";
    public static final String INTENT_EXTRA_STRING_PHONE_NUMBER = "phone_number";
    EditText editText_firstName;
    EditText editText_lastName;
    EditText editText_mobileNumber;
    private boolean addingNewContact = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        editText_firstName = (EditText) findViewById(R.id.editFirstName);
        editText_lastName = (EditText) findViewById(R.id.editLastName);
        editText_mobileNumber = (EditText) findViewById(R.id.editPhoneNumber);

        Intent intent = getIntent();
        if(intent.getBooleanExtra(INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, false)) {
            addingNewContact = true;
            editText_mobileNumber.setText(intent.getStringExtra(INTENT_EXTRA_STRING_PHONE_NUMBER));
            myToolbar.setTitle("New Contact");
        }
        else{
            contact = (Contact) intent.getSerializableExtra(INTENT_EXTRA_CONTACT_CONTACT_DETAILS);
            if(contact.getId() == -1){
                Toast.makeText(this, R.string.error_while_loading_contact, Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
            myToolbar.setTitle(contact.getFirstName());
            fillFieldsFromContactDetails();
        }
        setSupportActionBar(myToolbar);
    }

    private void fillFieldsFromContactDetails() {
        editText_firstName.setText(contact.getFirstName());
        editText_lastName.setText(contact.getLastName());
        editText_mobileNumber.setText(contact.getPhoneNumber());
        List<String> phoneNumbers = contact.getPhoneNumbers();
        if(phoneNumbers.size() > 1)
            for(int i = 1, totalNumbers = phoneNumbers.size(); i < totalNumbers; i++){
                addOneMorePhoneNumberView(null).setText(phoneNumbers.get(i));
            }
    }

    public void saveContact(View view) {
        String firstName = String.valueOf(editText_firstName.getText());
        String lastName = String.valueOf(editText_lastName.getText());
        String phoneNumber = String.valueOf(editText_mobileNumber.getText());
        if("".equals(firstName) && "".equals(lastName)){
            editText_firstName.setError("Required FirstName or LastName");
            return;
        }
        if("".equals(phoneNumber)){
            editText_mobileNumber.setError("Required");
            return;
        }
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
        savePhoneNumbers(dbContact);
        return dbContact;
    }

    @NonNull
    private opencontacts.open.com.opencontacts.orm.Contact addNewContact(String firstName, String lastName, String phoneNumber) {
        opencontacts.open.com.opencontacts.orm.Contact dbContact;
        dbContact = new opencontacts.open.com.opencontacts.orm.Contact(firstName, lastName);
        dbContact.save();
        savePhoneNumbers(dbContact);
        return dbContact;
    }

    private void savePhoneNumbers(opencontacts.open.com.opencontacts.orm.Contact dbContact) {
        LinearLayout phoneNumbersContainer = (LinearLayout) findViewById(R.id.phonenumbers);
        int numberOfPhoneNumbers = phoneNumbersContainer.getChildCount();
        String extraPhoneNumber;
        if(numberOfPhoneNumbers > 1){
            ArrayList<String> phoneNumbers = new ArrayList(numberOfPhoneNumbers);
            for(int i=0; i<numberOfPhoneNumbers; i++){
                extraPhoneNumber = String.valueOf(((EditText) phoneNumbersContainer.getChildAt(i)).getText());
                if("".equals(extraPhoneNumber))
                    continue;
                else
                    phoneNumbers.add(extraPhoneNumber);
            }
            ContactsDBHelper.replacePhoneNumbers(dbContact, phoneNumbers);
        }
        else
            ContactsDBHelper.replacePhoneNumbers(dbContact, Arrays.asList(String.valueOf(editText_mobileNumber.getText())));
    }

    public EditText addOneMorePhoneNumberView(View view){
        LinearLayout phoneNumbers_linearLayout = (LinearLayout) findViewById(R.id.phonenumbers);
        EditText oneMorePhoneNumberField = new EditText(this);
        oneMorePhoneNumberField.setLayoutParams(new ActionBar.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        oneMorePhoneNumberField.setInputType(InputType.TYPE_CLASS_PHONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            oneMorePhoneNumberField.setBackground((findViewById(R.id.editPhoneNumber)).getBackground());
        }
        else
            oneMorePhoneNumberField.setBackgroundDrawable((findViewById(R.id.editPhoneNumber)).getBackground());
        oneMorePhoneNumberField.setHint("Phone Number");
        phoneNumbers_linearLayout.addView(oneMorePhoneNumberField);
        return oneMorePhoneNumberField;
    }
}
