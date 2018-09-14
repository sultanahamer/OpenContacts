package opencontacts.open.com.opencontacts.data.datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.orm.Contact;
import opencontacts.open.com.opencontacts.orm.PhoneNumber;

/**
 * Created by sultanm on 7/17/17.
 */

class ContactsDBHelper {
    static Contact getDBContactWithId(Long id){
        return Contact.findById(Contact.class, id);
    }

    static void deleteContactInDB(Long contactId){
        Contact dbContact = Contact.findById(Contact.class, contactId);
        if(dbContact == null)
            return;
        List<PhoneNumber> dbPhoneNumbers = dbContact.getAllPhoneNumbers();
        for(PhoneNumber dbPhoneNumber : dbPhoneNumbers)
            dbPhoneNumber.delete();
        List<CallLogEntry> callLogEntries = CallLogEntry.getCallLogEntriesFor(contactId);
        for(CallLogEntry callLogEntry : callLogEntries){
            callLogEntry.setId((long) -1);
            callLogEntry.save();
        }
        dbContact.delete();
    }

    static Contact getContactFromDB(String phoneNumber) {
        if(phoneNumber.length() < 8)
            return null; //TODO: find a better logic to determine how to compare mobile number excluding code.
        String phoneNumberToSearch = phoneNumber.length() > 10 ? phoneNumber.substring(phoneNumber.length() - 10) : phoneNumber;
        List<PhoneNumber> phoneNumbers = PhoneNumber.find(PhoneNumber.class, "phone_Number like ?", "%" + phoneNumberToSearch);
        if(phoneNumbers.size() == 0)
            return null;
        return phoneNumbers.get(0).getContact();
    }

    static void replacePhoneNumbersInDB(Contact dbContact, List<String> phoneNumbers) {
        List<PhoneNumber> dbPhoneNumbers = dbContact.getAllPhoneNumbers();
        for(String phoneNumber : phoneNumbers){
            new PhoneNumber(phoneNumber, dbContact).save();
        }
        PhoneNumber.deleteInTx(dbPhoneNumbers);
    }

    static void updateContactInDB(opencontacts.open.com.opencontacts.domain.Contact contact){
        opencontacts.open.com.opencontacts.orm.Contact dbContact = ContactsDBHelper.getDBContactWithId(contact.getId());
        dbContact.firstName = contact.getFirstName();
        dbContact.lastName = contact.getLastName();
        dbContact.save();
        replacePhoneNumbersInDB(dbContact, contact.getPhoneNumbers());
    }

    private static opencontacts.open.com.opencontacts.domain.Contact createNewDomainContact(PhoneNumber dbPhoneNumber){
        opencontacts.open.com.opencontacts.orm.Contact dbContact = dbPhoneNumber.getContact();
        ArrayList<String> phoneNumbers = new ArrayList<>(3);
        phoneNumbers.add(dbPhoneNumber.getPhoneNumber());
        return new opencontacts.open.com.opencontacts.domain.Contact(dbContact.getId(), dbContact.firstName, dbContact.lastName, phoneNumbers, dbContact.lastAccessed);
    }

    static List<opencontacts.open.com.opencontacts.domain.Contact> getAllContactsFromDB(){
        List<PhoneNumber> dbPhoneNumbers = PhoneNumber.listAll(PhoneNumber.class);
        HashMap<Long, opencontacts.open.com.opencontacts.domain.Contact> contactsMap= new HashMap<Long, opencontacts.open.com.opencontacts.domain.Contact>();
        opencontacts.open.com.opencontacts.domain.Contact tempContact;
        for(PhoneNumber dbPhoneNumber: dbPhoneNumbers){
            tempContact = contactsMap.get(dbPhoneNumber.getContact().getId());
            if(tempContact == null)
                tempContact = createNewDomainContact(dbPhoneNumber);
            else
                tempContact.getPhoneNumbers().add(dbPhoneNumber.getPhoneNumber());
            contactsMap.put(tempContact.getId(), tempContact);
        }
        return new ArrayList<>(contactsMap.values());
    }

    private static opencontacts.open.com.opencontacts.domain.Contact createNewDomainContact(opencontacts.open.com.opencontacts.orm.Contact contact){
        List<PhoneNumber> dbPhoneNumbers = contact.getAllPhoneNumbers();
        List<String> phoneNumbers = new ArrayList<String>(3);
        for(PhoneNumber dbPhoneNumber : dbPhoneNumbers){
            phoneNumbers.add(dbPhoneNumber.getPhoneNumber());
        }
        return new opencontacts.open.com.opencontacts.domain.Contact(contact.getId(), contact.firstName, contact.lastName, phoneNumbers, contact.lastAccessed);
    }

    static opencontacts.open.com.opencontacts.domain.Contact getContact(long id){
        if(id == -1)
            return null;
        opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getDBContactWithId(id);
        if(contact == null)
            return null;
        return createNewDomainContact(contact);
    }
}
