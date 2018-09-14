package opencontacts.open.com.opencontacts.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardWriter;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.StructuredName;
import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;

/**
 * Created by sultanm on 7/22/17.
 */

public class DomainUtils {

    public static void updateContactsAccessedDate(final List<CallLogEntry> newCallLogEntries){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for(CallLogEntry callLogEntry : newCallLogEntries){
                    long contactId = callLogEntry.getContactId();
                    if(contactId !=-1){
                        ContactsDataStore.updateLastAccessed(contactId, callLogEntry.getDate());
                    }
                }
                return null;
            }
        }.execute();
    }

    public static void exportAllContacts(Context context) throws IOException {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            AndroidUtils.showAlert(context, "Error", "Storage is not mounted");
            return;
        }
        File path = Environment.getExternalStorageDirectory();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy hh-mm-ss");
        File file = new File(path, "Contacts_" + simpleDateFormat.format(new Date()) + " .vcf");
        file.createNewFile();
        List<Contact> allContacts = ContactsDataStore.getAllContacts();
        VCardWriter vCardWriter = null;
        try{
            vCardWriter = new VCardWriter(new FileOutputStream(file), VCardVersion.V4_0);

            StructuredName structuredName = new StructuredName();

            for( Contact contact : allContacts){
                VCard vcard = new VCard();
                structuredName.setGiven(contact.getFirstName());
                structuredName.setFamily(contact.getLastName());
                vcard.setStructuredName(structuredName);
                for(String phoneNumber : contact.getPhoneNumbers())
                    vcard.addTelephoneNumber(phoneNumber, TelephoneType.CELL);
                vCardWriter.write(vcard);
            }
        }
        finally {
            if(vCardWriter != null)
                vCardWriter.close();
        }

    }
}
