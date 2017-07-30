package opencontacts.open.com.opencontacts.orm;

import com.orm.SugarRecord;

/**
 * Created by sultanm on 7/31/17.
 */

public class CallLogEntry extends SugarRecord {
    String name;
    long contactId;
    String phoneNumber;
    String duration;
    String callType;
    String date; //in milliseconds from epoch type long;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public CallLogEntry(){
        super();
    }

    public CallLogEntry(String name, long contactId, String phoneNumber, String duration, String callType, String date) {
        this.name = name;
        this.contactId = contactId;
        this.phoneNumber = phoneNumber;
        this.duration = duration;
        this.callType = callType;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }
}
