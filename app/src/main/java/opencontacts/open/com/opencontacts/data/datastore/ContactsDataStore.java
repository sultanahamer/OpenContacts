package opencontacts.open.com.opencontacts.data.datastore;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;

public class ContactsDataStore {
    private static List<Contact> contacts;
    private static List<ContactsDataChangeListener> dataChangeListeners = new ArrayList<>(3);

    public static List<Contact> getAllContacts(){
        if(contacts == null){
            contacts = DomainUtils.getAllContacts();
        }
        return new ArrayList<>(contacts);
    }

    public static void addContact(String firstName, String lastName, List<String> phoneNumbers){
        opencontacts.open.com.opencontacts.orm.Contact dbContact = new opencontacts.open.com.opencontacts.orm.Contact(firstName, lastName);
        dbContact.save();
        ContactsDBHelper.replacePhoneNumbers(dbContact, phoneNumbers);
        Contact newContactWithDatabaseId = DomainUtils.getContact(dbContact.getId());
        contacts.add(newContactWithDatabaseId);
        for(ContactsDataChangeListener contactsDataChangeListener : dataChangeListeners)
            contactsDataChangeListener.onAdd(newContactWithDatabaseId);
    }

    public static void removeContact(Contact contact){
        if(contacts.remove(contact)){
            ContactsDBHelper.deleteContact(contact.getId());
            for(ContactsDataChangeListener contactsDataChangeListener : dataChangeListeners)
                contactsDataChangeListener.onRemove(contact);
        }
    }

    public static void updateContact(Contact contact){
        int indexOfContact = contacts.indexOf(contact);
        if(indexOfContact == -1)
            return;
        opencontacts.open.com.opencontacts.orm.Contact dbContact = ContactsDBHelper.getContactWithId(contact.getId());
        dbContact.firstName = contact.getFirstName();
        dbContact.lastName = contact.getLastName();
        dbContact.save();
        ContactsDBHelper.replacePhoneNumbers(dbContact, contact.getPhoneNumbers());
        Contact updatedContact = DomainUtils.getContact(contact.getId());
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

    public interface ContactsDataChangeListener {
        void onUpdate(Contact contact);
        void onRemove(Contact contact);
        void onAdd(Contact contact);
    }
}

