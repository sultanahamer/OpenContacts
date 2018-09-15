package opencontacts.open.com.opencontacts.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        editText_firstName = (EditText) findViewById(R.id.editFirstName);
        editText_lastName = (EditText) findViewById(R.id.editLastName);
        editText_mobileNumber = (EditText) findViewById(R.id.editPhoneNumber);

        Intent intent = getIntent();
        if(intent.getBooleanExtra(INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, false)) {
            addingNewContact = true;
            editText_mobileNumber.setText(intent.getStringExtra(INTENT_EXTRA_STRING_PHONE_NUMBER));
            toolbar.setTitle("New Contact");
        }
        else{
            contact = (Contact) intent.getSerializableExtra(INTENT_EXTRA_CONTACT_CONTACT_DETAILS);
            if(contact.getId() == -1){
                Toast.makeText(this, R.string.error_while_loading_contact, Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
            toolbar.setTitle(contact.getFirstName());
            fillFieldsFromContactDetails();
        }
        setSupportActionBar(toolbar);
        AndroidUtils.setBackButtonInToolBar(toolbar, this);
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

        if(addingNewContact)
            ContactsDataStore.addContact(firstName, lastName, getPhoneNumbersFromView());
        else
            ContactsDataStore.updateContact(new Contact(contact.getId(), firstName, lastName, getPhoneNumbersFromView()));
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private List<String> getPhoneNumbersFromView() {
        LinearLayout phoneNumbersContainer = (LinearLayout) findViewById(R.id.phonenumbers);
        int numberOfPhoneNumbers = phoneNumbersContainer.getChildCount();
        String extraPhoneNumber;
        ArrayList<String> phoneNumbers = new ArrayList(numberOfPhoneNumbers);
        for(int i=0; i<numberOfPhoneNumbers; i++){
            extraPhoneNumber = String.valueOf(((EditText) phoneNumbersContainer.getChildAt(i)).getText());
            if("".equals(extraPhoneNumber))
                continue;
            phoneNumbers.add(extraPhoneNumber);
        }
        return phoneNumbers;
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
