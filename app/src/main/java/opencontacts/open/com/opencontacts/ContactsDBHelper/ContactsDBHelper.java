package opencontacts.open.com.opencontacts.ContactsDBHelper;

import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;

/**
 * Created by sultanm on 7/17/17.
 */

public class ContactsDBHelper {
    public static Contact getContactWithId(Long id){
        return Contact.findById(Contact.class, id);
    }

    public static List<Contact> getAllContacts(){
        return Contact.listAll(Contact.class, "first_Name");
    }
}
