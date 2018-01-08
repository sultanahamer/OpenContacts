package opencontacts.open.com.opencontacts;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import opencontacts.open.com.opencontacts.activities.MainActivity;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.DomainUtils;

/**
 * Created by sultanm on 3/25/17.
 */

public class ContactsListView extends ListView {
    final List <Contact> contacts;
    ContactsListView thisClass = this;
    public static final String ACTION_UPDATED = "action_updated";
    Context activity;
    Contact selectedContact = null;
    ArrayAdapter<Contact> adapter;
    Object mLock = new Object();

    public ContactsListView(final Activity activity) {
        super(activity);
        this.activity = activity;
        setTextFilterEnabled(true);
        contacts = DomainUtils.getAllContacts();

        final OnClickListener callContact = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact contact = (Contact) ((View)v.getParent()).getTag();
                AndroidUtils.call(contact.getPhoneNumber(), ContactsListView.this.activity);
            }
        };
        final OnClickListener messageContact = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact contact = (Contact) ((View)v.getParent()).getTag();
                AndroidUtils.message(contact.getPhoneNumber(), ContactsListView.this.activity);
            }
        };
        final OnClickListener showContactDetails = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact contact = (Contact) v.getTag();
                selectedContact = contact;
                activity.startActivityForResult(AndroidUtils.getIntentToShowContactDetails(contact, ContactsListView.this.activity), MainActivity.REQUESTCODE_FOR_SHOW_CONTACT_DETAILS);
            }
        };

        adapter = new ArrayAdapter<Contact>(ContactsListView.this.activity, R.layout.contact, new ArrayList<Contact>(contacts)){
            private LayoutInflater layoutInflater = LayoutInflater.from(ContactsListView.this.activity);
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Contact contact = getItem(position);
                if(convertView == null)
                    convertView = layoutInflater.inflate(R.layout.contact, parent, false);
                ((TextView) convertView.findViewById(R.id.textview_full_name)).setText(contact.getName());
                ((TextView) convertView.findViewById(R.id.textview_phone_number)).setText(contact.getPhoneNumber());
                ((ImageButton)convertView.findViewById(R.id.button_call)).setOnClickListener(callContact);
                ((ImageButton)convertView.findViewById(R.id.button_message)).setOnClickListener(messageContact);
                convertView.setTag(contact);
                convertView.setOnClickListener(showContactDetails);
                return convertView;
            }

            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected synchronized FilterResults  performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        System.out.println(constraint + ", "+ contacts.size());
                        if(constraint == null || constraint.length() == 0){
                            results.values = contacts;
                            results.count = contacts.size();
                        }
                        else {
                            ArrayList<Contact> filteredContacts = new ArrayList<>();
                            filteredContacts.addAll(contacts);
                            for (Contact c : contacts) {
                                System.out.println(c.toString());
                                if (!c.toString().toUpperCase().contains( constraint.toString().toUpperCase() )) {
                                    filteredContacts.remove(c);
                                }
                            }
                            results.values = filteredContacts;
                            results.count = filteredContacts.size();
                        }

                        return results;
                    }

                    @Override
                    protected synchronized void publishResults(CharSequence constraint, FilterResults results) {
                        adapter.clear();
                        if (constraint == null || constraint.length() == 0) {
                            addAllContactsToAdapter(contacts);
                        }
                        else
                            addAllContactsToAdapter((List<Contact>) results.values);
                        notifyDataSetChanged();
                    }
                };
            }
        };
        sortContacts();
        this.setAdapter(adapter);
    }
    private synchronized void addAllContactsToAdapter(List<Contact> contactsList){
        for(Contact contact : contactsList)
            adapter.add(contact);
    }
    private void sortContacts() {
        adapter.sort(new Comparator<Contact>() {
            @Override
            public int compare(Contact contact1, Contact contact2) {
                return contact1.getName().compareToIgnoreCase(contact2.getName());
            }
        });
    }

    public void updateContactViewAt(int position, long contactId) {
        Contact oldContactInView = adapter.getItem(position);
        if(oldContactInView == null)
            return;
        else{
            adapter.remove(adapter.getItem(position));
            Contact updatedContact = DomainUtils.getContact(contactId);
            if(updatedContact == null){
                adapter.notifyDataSetChanged();
                return;
            }
            adapter.insert(updatedContact, position);
            adapter.notifyDataSetChanged();
        }
    }

    public void addNewContactInView(long newContactId) {
        Contact newContact = DomainUtils.getContact(newContactId);
        adapter.add(newContact);
        contacts.add(newContact);
        sortContacts();
        adapter.notifyDataSetChanged();
    }

    public  void deleteContactViewAt(int position){
        Contact contact = adapter.getItem(position);
        adapter.remove(contact);
        contacts.remove(contact);
        adapter.notifyDataSetChanged();
    }

    public void deleteContactInView(long contactId) {
        if(selectedContact == null)
            return;
        CharSequence textFilter = null;
        if(this.hasTextFilter())
            textFilter = this.getTextFilter();
        int position = adapter.getPosition(selectedContact);
        if(position == -1){
            selectedContact = null;
            return;
        }
        if(contactId == selectedContact.getId()){
            deleteContactViewAt(position);
            this.clearTextFilter();
            if(textFilter != null)
                this.setFilterText(textFilter.toString());
            selectedContact = null;
        }

    }

    public void updateContactInView(long contactId) {
        if(selectedContact == null)
            return;
        CharSequence textFilter = null;
        if(this.hasTextFilter())
            textFilter = this.getTextFilter();
        int position = adapter.getPosition(selectedContact);
        if(position == -1){
            selectedContact = null;
            return;
        }
        if(contactId == selectedContact.getId()){
            updateContactViewAt(position, contactId);
            this.clearTextFilter();
            if(textFilter != null)
                this.setFilterText(textFilter.toString());
            selectedContact = null;
        }
    }
}
