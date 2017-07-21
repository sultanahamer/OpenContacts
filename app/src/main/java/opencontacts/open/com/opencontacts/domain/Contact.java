package opencontacts.open.com.opencontacts.domain;

import com.orm.SugarRecord;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by sultanm on 3/24/17.
 */
public class Contact extends SugarRecord implements Serializable{
    public String firstName;

    public String lastName;

    public String phoneNumber;

    public ArrayList<String> extraNumbers;

    public Contact(){
        super();
    }

    public Contact(String firstName, String lastName, String phoneNumber){
        setAll(firstName, lastName, phoneNumber);
    }
    public void setAll(String firstName, String lastName, String mobileNumber){
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = mobileNumber;
    }
    public String toString(){
        return firstName + " " + lastName + ":" + getId();
    }
}