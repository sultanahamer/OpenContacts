package opencontacts.open.com.opencontacts.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardWriter;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.StructuredName;
import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;

/**
 * Created by sultanm on 7/22/17.
 */

public class DomainUtils {

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

    public static Contact getACopyOf(Contact contact){
        return new Contact(contact.getId(), contact.getFirstName(), contact.getLastName(), new ArrayList<>(contact.getPhoneNumbers()));
    }

    public static List<Contact> filter(CharSequence constraint, List<Contact> contacts){
        if(constraint == null || constraint.length() == 0){
            return contacts;
        }

        ArrayList<Contact> filteredContacts = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.toString().toUpperCase().contains( constraint.toString().toUpperCase() )) {
                filteredContacts.add(c);
            }
        }
        Collections.sort(filteredContacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact1, Contact contact2) {
                String lastAccessedDate1 = contact1.getLastAccessed();
                String lastAccessedDate2 = contact2.getLastAccessed();
                if(lastAccessedDate1 == null && lastAccessedDate2 == null)
                    return 0;
                else if(lastAccessedDate1 == null)
                    return 1;
                else if (lastAccessedDate2 == null)
                    return -1;
                else
                    return lastAccessedDate2.compareTo(lastAccessedDate1);
            }
        });
        return filteredContacts;
    }
}
