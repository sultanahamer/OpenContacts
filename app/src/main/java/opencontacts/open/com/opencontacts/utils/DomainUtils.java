package opencontacts.open.com.opencontacts.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardWriter;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.StructuredName;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.orm.PhoneNumber;

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
                        opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getContactWithId(contactId);
                        contact.lastAccessed = callLogEntry.getDate();
                        contact.save();
                    }
                }
                return null;
            }
        }.execute();
    }

    public static Contact createNewDomainContact(PhoneNumber dbPhoneNumber){
        opencontacts.open.com.opencontacts.orm.Contact dbContact = dbPhoneNumber.getContact();
        ArrayList<String> phoneNumbers = new ArrayList<String>(3);
        phoneNumbers.add(dbPhoneNumber.getPhoneNumber());
        return new Contact(dbContact.getId(), dbContact.firstName, dbContact.lastName, phoneNumbers, dbContact.lastAccessed);
    }

    public static List<Contact> getAllContacts(){
        List<PhoneNumber> dbPhoneNumbers = PhoneNumber.listAll(PhoneNumber.class);
        HashMap<Long, Contact> contactsMap= new HashMap<Long, Contact>();
        Contact tempContact;
        for(PhoneNumber dbPhoneNumber: dbPhoneNumbers){
            tempContact = contactsMap.get(dbPhoneNumber.getContact().getId());
            if(tempContact == null)
                tempContact = createNewDomainContact(dbPhoneNumber);
            else
                tempContact.getPhoneNumbers().add(dbPhoneNumber.getPhoneNumber());
            contactsMap.put(tempContact.getId(), tempContact);
        }
        return new ArrayList<Contact>(contactsMap.values());
    }

    public static Contact createNewDomainContact(opencontacts.open.com.opencontacts.orm.Contact contact){
        List<PhoneNumber> dbPhoneNumbers = contact.getAllPhoneNumbers();
        List<String> phoneNumbers = new ArrayList<String>(3);
        for(PhoneNumber dbPhoneNumber : dbPhoneNumbers){
            phoneNumbers.add(dbPhoneNumber.getPhoneNumber());
        }
        return new Contact(contact.getId(), contact.firstName, contact.lastName, phoneNumbers, contact.lastAccessed);
    }
    public static Contact getContact(long id){
        if(id == -1)
            return null;
        opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getContactWithId(id);
        if(contact == null)
            return null;
        return createNewDomainContact(contact);
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
        List<Contact> allContacts = getAllContacts();
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
