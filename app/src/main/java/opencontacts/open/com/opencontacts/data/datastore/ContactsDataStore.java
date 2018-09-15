package opencontacts.open.com.opencontacts.data.datastore;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.interfaces.DataStoreChangeListener;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;

public class ContactsDataStore {
    private static List<Contact> contacts = new ArrayList<>(1);
    private static List<DataStoreChangeListener<Contact>> dataChangeListeners = new ArrayList<>(3);

    public static List<Contact> getAllContacts() {
        if (contacts.size() == 0) {
            contacts = ContactsDBHelper.getAllContactsFromDB();
        }
        return new ArrayList<>(contacts);
    }

    public static void addContact(String firstName, String lastName, List<String> phoneNumbers) {
        opencontacts.open.com.opencontacts.orm.Contact dbContact = new opencontacts.open.com.opencontacts.orm.Contact(firstName, lastName);
        dbContact.save();
        ContactsDBHelper.replacePhoneNumbersInDB(dbContact, phoneNumbers);
        Contact newContactWithDatabaseId = ContactsDBHelper.getContact(dbContact.getId());
        contacts.add(newContactWithDatabaseId);
        for (DataStoreChangeListener<Contact> contactsDataChangeListener : dataChangeListeners)
            contactsDataChangeListener.onAdd(newContactWithDatabaseId);
    }

    public static void removeContact(Contact contact) {
        if (contacts.remove(contact)) {
            ContactsDBHelper.deleteContactInDB(contact.getId());
            for (DataStoreChangeListener<Contact> contactsDataChangeListener : dataChangeListeners)
                contactsDataChangeListener.onRemove(contact);
        }
    }

    public static void updateContact(Contact contact) {
        int indexOfContact = contacts.indexOf(contact);
        if (indexOfContact == -1)
            return;
        ContactsDBHelper.updateContactInDB(contact);
        Contact updatedContact = ContactsDBHelper.getContact(contact.getId());
        contacts.remove(indexOfContact);
        contacts.add(indexOfContact, updatedContact);
        for (DataStoreChangeListener<Contact> contactsDataChangeListener : dataChangeListeners)
            contactsDataChangeListener.onUpdate(updatedContact);
    }

    public static void addDataChangeListener(DataStoreChangeListener<Contact> changeListener) {
        dataChangeListeners.add(changeListener);
    }

    public static void removeDataChangeListener(DataStoreChangeListener<Contact> changeListener) {
        dataChangeListeners.remove(changeListener);
    }

    public static opencontacts.open.com.opencontacts.orm.Contact getContact(String phoneNumber) {
        return ContactsDBHelper.getContactFromDB(phoneNumber);
    }

    public static Contact getContactWithId(long contactId) {
        if (contactId == -1)
            return null;
        int indexOfContact = contacts.indexOf(new Contact(contactId));
        if (indexOfContact == -1)
            return null;
        return contacts.get(indexOfContact);
    }

    public static void updateLastAccessed(long contactId, String callTimeStamp) {
        opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getDBContactWithId(contactId);
        if (callTimeStamp.equals(contact.lastAccessed))
            return;
        contact.lastAccessed = callTimeStamp;
        contact.save();
    }

    public static void updateContactsAccessedDateAsync(final List<CallLogEntry> newCallLogEntries) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (CallLogEntry callLogEntry : newCallLogEntries) {
                    long contactId = callLogEntry.getContactId();
                    if (contactId != -1) {
                        contacts.indexOf(new Contact(contactId));
                        ContactsDataStore.updateLastAccessed(contactId, callLogEntry.getDate());
                    }
                }
                return null;
            }
        }.execute();
    }
}
