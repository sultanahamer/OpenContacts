package opencontacts.open.com.opencontacts.orm;

import com.orm.SugarRecord;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sultanm on 3/24/17.
 */
public class Contact extends SugarRecord implements Serializable{
    public String firstName;

    public String lastName;

    public String lastAccessed;

    public Contact(){
        super();
    }

    public Contact(String firstName, String lastName){
        if(lastName == null)
            lastName = "";
        if(firstName == null)
            firstName = lastName;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String toString(){
        return firstName + " " + lastName;
    }

    public List<PhoneNumber> getAllPhoneNumbers(){
        return PhoneNumber.find(PhoneNumber.class,  "contact = ?", "" + this.getId());
    }

}