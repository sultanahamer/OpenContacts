package opencontacts.open.com.opencontacts;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.ContactsDBHelper.ContactsDBHelper;

public class EditContactActivity extends AppCompatActivity {
    Contact contactFromDB;
    public static final String INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT = "add_new_contact";
    public static final String INTENT_EXTRA_CONTACT_CONTACT_DETAILS = "contact_details";
    public static final String INTENT_EXTRA_LONG_CONTACT_ID = "contact_id";
    public static final String INTENT_EXTRA_BOOLEAN_CONTACT_DELETED = "contact_deleted";
    TextView firstName;
    TextView lastName;
    TextView mobileNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        RelativeLayout rlayout  = (RelativeLayout) findViewById(R.id.relative_layout_contact_details);
        firstName = ((TextView)findViewById(R.id.editFirstName));
        lastName = ((TextView)findViewById(R.id.editLastName));
        mobileNumber = ((TextView)findViewById(R.id.editPhoneNumber));
        Intent intent = getIntent();
        Contact contact;
        if(intent.getBooleanExtra(INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, false)) {
            contact = contactFromDB = new Contact();
        }
        else{
            contact = (Contact) intent.getSerializableExtra(INTENT_EXTRA_CONTACT_CONTACT_DETAILS);
            long contactId = intent.getLongExtra(INTENT_EXTRA_LONG_CONTACT_ID, -1);
            if(contactId == -1){
                Toast.makeText(this, R.string.error_while_loading_contact, Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }
            contactFromDB = ContactsDBHelper.getContactWithId(contactId);
        }
        firstName.setText(contact.firstName);
        lastName.setText(contact.lastName);
        mobileNumber.setText(contact.phoneNumber);
    }
    public void delete(View view) {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to delete?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contactFromDB.delete();
                        Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        Intent result = new Intent();
                        result.putExtra(INTENT_EXTRA_BOOLEAN_CONTACT_DELETED, true);
                        setResult(RESULT_OK, result);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).show();

    }
    public void saveContact(View view) {
        contactFromDB.firstName = String.valueOf(firstName.getText());
        contactFromDB.lastName = String.valueOf(lastName.getText());
        contactFromDB.phoneNumber = String.valueOf(mobileNumber.getText());
        contactFromDB.save();
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        Intent result = new Intent();
        result.putExtra(INTENT_EXTRA_LONG_CONTACT_ID, contactFromDB.getId());
        setResult(RESULT_OK, result);
        finish();
    }
}
