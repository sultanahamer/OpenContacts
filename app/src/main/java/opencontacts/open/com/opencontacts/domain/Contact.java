package opencontacts.open.com.opencontacts.domain;

import java.io.Serializable;
import java.util.List;

import opencontacts.open.com.opencontacts.utils.Common;

/**
 * Created by sultanm on 7/22/17.
 */

public class Contact implements Serializable{
    private final long id;
    private String firstName;
    private String lastName;
    private List<String> phoneNumbers;
    private String name;

    private String lastAccessed;

    public Contact(long id, String firstName, String lastName, List<String> phoneNumbers, String lastAccessed) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumbers = phoneNumbers;
        this.name = firstName + " " + lastName;
        this.lastAccessed = lastAccessed;
    }

    public Contact(Long id, String firstName, String lastName, List<String> phoneNumbers, String lastAccessed) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumbers = phoneNumbers;
        this.name = firstName + " " + lastName;
        this.lastAccessed = lastAccessed;
    }

    @Override
    public String toString() {
        StringBuffer searchStringBuffer = new StringBuffer();
        searchStringBuffer.append(name).append(' ');
        for(String phoneNumber : phoneNumbers)
            searchStringBuffer.append(phoneNumber).append(' ');
        searchStringBuffer.append(Common.getNumericKeyPadNumberForString(name));
        return searchStringBuffer.toString();
    }

    public String getLastAccessed() {
        return lastAccessed;
    }
    public void setLastAccessed(String lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
    public String getPhoneNumber(){
        return phoneNumbers.get(0);
    }
    public String getName(){
        return name;
    }
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void addPhoneNumber(String phoneNumber){
        phoneNumbers.add(phoneNumber);
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}
