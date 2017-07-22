package opencontacts.open.com.opencontacts.orm;

import com.orm.SugarRecord;

/**
 * Created by sultanm on 7/22/17.
 */

public class PhoneNumber extends SugarRecord{
    String phoneNumber;
    Contact contact;

    public PhoneNumber(){

    }
    public PhoneNumber(String mobileNumber, Contact contact) {
        this.phoneNumber = mobileNumber;
        this.contact = contact;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

}
