package opencontacts.open.com.opencontacts.utils;

import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.orm.PhoneNumber;

/**
 * Created by sultanm on 7/22/17.
 */

public class DomainUtils {
    public static Contact createNewDomainContact(PhoneNumber dbPhoneNumber){
        opencontacts.open.com.opencontacts.orm.Contact dbContact = dbPhoneNumber.getContact();
        ArrayList<String> phoneNumbers = new ArrayList<String>(3);
        phoneNumbers.add(dbPhoneNumber.getPhoneNumber());
        return new Contact(dbContact.getId(), dbContact.firstName, dbContact.lastName, phoneNumbers);
    }

    public static List<Contact> getAllContacts(){
        List<PhoneNumber> dbPhoneNumbers = PhoneNumber.listAll(PhoneNumber.class);
        HashMap<Long, Contact> contactsMap= new HashMap<Long, Contact>();
        Contact tempContact;
        for(PhoneNumber dbPhoneNumber: dbPhoneNumbers){
            tempContact = contactsMap.get(dbPhoneNumber.getContact().getId());
            if(tempContact == null){
                tempContact = createNewDomainContact(dbPhoneNumber);
            }
            else
                tempContact.getPhoneNumbers().add(dbPhoneNumber.getPhoneNumber());
            contactsMap.put(tempContact.getId(), tempContact);
        }
        return new ArrayList<Contact>(contactsMap.values());
    }

    public static Contact createNewDomainContact(opencontacts.open.com.opencontacts.orm.Contact contact){
        List<PhoneNumber> dbPhoneNumbers = contact.getAllPhoneNumbers();
        List<String> phoneNumbers = new ArrayList<String>();
        for(PhoneNumber dbPhoneNumber : dbPhoneNumbers){
            phoneNumbers.add(dbPhoneNumber.getPhoneNumber());
        }
        return new Contact(contact.getId(), contact.firstName, contact.lastName, phoneNumbers);
    }
    public static Contact getContact(long id){
        opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getContactWithId(id);
        return createNewDomainContact(contact);
    }
}
