package opencontacts.open.com.opencontacts.data.datastore;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.orm.Contact;
import opencontacts.open.com.opencontacts.orm.PhoneNumber;

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

    public static void deleteContact(Long contactId){
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

    public static void updatePhoneNumber(Contact dbContact, String oldPhoneNumber, String newPhoneNumber) {
        List<PhoneNumber> dbPhoneNumbers = dbContact.getAllPhoneNumbers();
        for(PhoneNumber dbPhoneNumber : dbPhoneNumbers){
            if(dbPhoneNumber.getPhoneNumber().equals(oldPhoneNumber)){
                dbPhoneNumber.setPhoneNumber(newPhoneNumber);
                dbPhoneNumber.save();
                break;
            }
        }
    }

    public static void saveAll(List<opencontacts.open.com.opencontacts.domain.Contact> domainContacts) {
        List<Contact> dbContacts = new ArrayList<>();
        List<PhoneNumber> dbPhoneNumbers = new ArrayList<>();
        Contact tempDBContact;
        PhoneNumber tempDBPhoneNumber;
        for(opencontacts.open.com.opencontacts.domain.Contact domainContact : domainContacts){
            tempDBContact = new Contact(domainContact.getFirstName(), domainContact.getLastName());
            dbContacts.add(tempDBContact);
            for(String phoneNumber : domainContact.getPhoneNumbers()){
                tempDBPhoneNumber = new PhoneNumber(phoneNumber, tempDBContact);
                dbPhoneNumbers.add(tempDBPhoneNumber);
            }
        }
        Contact.saveInTx(dbContacts);
        PhoneNumber.saveInTx(dbPhoneNumbers);
    }

    public static Contact getContact(String incomingNumber) {
        if(incomingNumber.length() < 8)
            return null; //TODO: find a better logic to determine how to compare mobile number excluding code.
        String phoneNumberToSearch = incomingNumber.length() > 10 ? incomingNumber.substring(incomingNumber.length() - 10) : incomingNumber;
        List<PhoneNumber> phoneNumbers = PhoneNumber.find(PhoneNumber.class, "phone_Number like ?", "%" + phoneNumberToSearch);
        if(phoneNumbers.size() == 0)
            return null;
        return phoneNumbers.get(0).getContact();
    }

    public static void replacePhoneNumbers(Contact dbContact, List<String> phoneNumbers) {
        List<PhoneNumber> dbPhoneNumbers = dbContact.getAllPhoneNumbers();
        for(String phoneNumber : phoneNumbers){
            new PhoneNumber(phoneNumber, dbContact).save();
        }
        PhoneNumber.deleteInTx(dbPhoneNumbers);
    }
}