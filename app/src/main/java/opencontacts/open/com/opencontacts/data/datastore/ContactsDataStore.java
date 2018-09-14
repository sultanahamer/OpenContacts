package opencontacts.open.com.opencontacts.data.datastore;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;

public class ContactsDataStore {
    private static List<Contact> contacts = new ArrayList<>(1);
    private static List<ContactsDataChangeListener> dataChangeListeners = new ArrayList<>(3);

    public static List<Contact> getAllContacts(){
        if(contacts.size() == 0){
            contacts = ContactsDBHelper.getAllContactsFromDB();
        }
        return new ArrayList<>(contacts);
    }

    public static void addContact(String firstName, String lastName, List<String> phoneNumbers){
        opencontacts.open.com.opencontacts.orm.Contact dbContact = new opencontacts.open.com.opencontacts.orm.Contact(firstName, lastName);
        dbContact.save();
        ContactsDBHelper.replacePhoneNumbersInDB(dbContact, phoneNumbers);
        Contact newContactWithDatabaseId = ContactsDBHelper.getContact(dbContact.getId());
        contacts.add(newContactWithDatabaseId);
        for(ContactsDataChangeListener contactsDataChangeListener : dataChangeListeners)
            contactsDataChangeListener.onAdd(newContactWithDatabaseId);
    }

    public static void removeContact(Contact contact){
        if(contacts.remove(contact)){
            ContactsDBHelper.deleteContactInDB(contact.getId());
            for(ContactsDataChangeListener contactsDataChangeListener : dataChangeListeners)
                contactsDataChangeListener.onRemove(contact);
        }
    }

    public static void updateContact(Contact contact){
        int indexOfContact = contacts.indexOf(contact);
        if(indexOfContact == -1)
            return;
        ContactsDBHelper.updateContactInDB(contact);
        Contact updatedContact = ContactsDBHelper.getContact(contact.getId());
        contacts.remove(indexOfContact);
        contacts.add(indexOfContact, updatedContact);
        for(ContactsDataChangeListener contactsDataChangeListener : dataChangeListeners)
            contactsDataChangeListener.onUpdate(updatedContact);
    }

    public static void addDataChangeListener(ContactsDataChangeListener changeListener){
        dataChangeListeners.add(changeListener);
    }

    public static void removeDataChangeListener(ContactsDataChangeListener changeListener){
        dataChangeListeners.remove(changeListener);
    }

    public static opencontacts.open.com.opencontacts.orm.Contact getContact(String phoneNumber) {
        return ContactsDBHelper.getContactFromDB(phoneNumber);
    }

    public static Contact getContactWithId(long contactId){
        if(contactId == -1)
            return null;
        int indexOfContact = contacts.indexOf(new Contact(contactId));
        if(indexOfContact == -1)
            return null;
        return contacts.get(indexOfContact);
    }

    public static void updateLastAccessed(long contactId, String date) {
        opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getDBContactWithId(contactId);
        contact.lastAccessed = date;
        contact.save();
    }

    public interface ContactsDataChangeListener {
        void onUpdate(Contact contact);
        void onRemove(Contact contact);
        void onAdd(Contact contact);
    }
}

