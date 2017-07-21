package opencontacts.open.com.opencontacts;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by sultanm on 3/24/17.
 */
@Table(name = "Contacts")
public class Contact extends Model implements Serializable{
    @Column(name = "FirstName")
    public String firstName;

    @Column(name = "LastName")
    public String lastName;

    @Column(name = "PhoneNumber")
    public String phoneNumber;

    @Column(name = "ExtraNumbers")
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