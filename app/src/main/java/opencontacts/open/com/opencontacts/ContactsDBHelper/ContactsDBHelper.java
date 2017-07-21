package opencontacts.open.com.opencontacts.ContactsDBHelper;

import com.activeandroid.query.Select;

import java.util.List;

import opencontacts.open.com.opencontacts.Contact;

/**
 * Created by sultanm on 7/17/17.
 */

public class ContactsDBHelper {
    public static Contact getContactWithId(Long id){
        return new Select()
                .from(Contact.class)
                .where("id = ?", id)
                .executeSingle();
    }

    public static List<Contact> getAllContacts(){
        return new Select()
                .from(Contact.class)
                .execute();
    }
}
