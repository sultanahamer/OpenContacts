package opencontacts.open.com.opencontacts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;

public class ContactsListViewAdapter extends ArrayAdapter<Contact>{
    private final List<Contact> contacts;
    private ContactsListActionsListener contactsListActionsListener;
    private LayoutInflater layoutInflater;

    private final View.OnClickListener callContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(contactsListActionsListener == null)
                return;
            Contact contact = (Contact) v.getTag();
            contactsListActionsListener.onCallClicked(contact);
        }
    };
    private final View.OnClickListener messageContact = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(contactsListActionsListener == null)
                return;
            Contact contact = (Contact) ((View)v.getParent()).getTag();
            contactsListActionsListener.onMessageClicked(contact);
        }
    };
    private final View.OnClickListener showContactDetails = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(contactsListActionsListener == null)
                return;
            Contact contact = (Contact) ((View)v.getParent()).getTag();
            contactsListActionsListener.onShowDetails(contact);
        }
    };

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = getItem(position);
        if(convertView == null)
            convertView = layoutInflater.inflate(R.layout.contact, parent, false);
        ((TextView) convertView.findViewById(R.id.textview_full_name)).setText(contact.getName());
        ((TextView) convertView.findViewById(R.id.textview_phone_number)).setText(contact.getPhoneNumber());
        convertView.findViewById(R.id.button_info).setOnClickListener(showContactDetails);
        convertView.findViewById(R.id.button_message).setOnClickListener(messageContact);
        convertView.setTag(contact);
        convertView.setOnClickListener(callContact);
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected synchronized FilterResults  performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if(constraint == null || constraint.length() == 0){
                    results.values = contacts;
                    results.count = contacts.size();
                    return results;
                }

                ArrayList<Contact> filteredContacts = new ArrayList<>();
                for (Contact c : contacts) {
                    if (c.toString().toUpperCase().contains( constraint.toString().toUpperCase() )) {
                        filteredContacts.add(c);
                    }
                }
                Collections.sort(filteredContacts, new Comparator<Contact>() {
                    @Override
                    public int compare(Contact contact1, Contact contact2) {
                        String lastAccessedDate1 = contact1.getLastAccessed();
                        String lastAccessedDate2 = contact2.getLastAccessed();
                        if(lastAccessedDate1 == null && lastAccessedDate2 == null)
                            return 0;
                        else if(lastAccessedDate1 == null)
                            return 1;
                        else if (lastAccessedDate2 == null)
                            return -1;
                        else
                            return lastAccessedDate2.compareTo(lastAccessedDate1);
                    }
                });
                results.values = filteredContacts;
                results.count = filteredContacts.size();
                return results;
            }

            @Override
            protected synchronized void publishResults(CharSequence constraint, FilterResults results) {
                clear();
                if (constraint == null || constraint.length() == 0)
                    addAllContactsToAdapter(contacts);
                else
                    addAllContactsToAdapter((List<Contact>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    private synchronized void addAllContactsToAdapter(List<Contact> contactsList){
        for(Contact contact : contactsList)
            add(contact);
    }

    ContactsListViewAdapter(@NonNull Context context, int resource, @NonNull List<Contact> contacts) {
        super(context, resource, new ArrayList<Contact>(contacts));
        this.contacts = contacts;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setContactsListActionsListener(ContactsListActionsListener contactsListActionsListener){
        this.contactsListActionsListener = contactsListActionsListener;
    }

    interface ContactsListActionsListener {
        void onCallClicked(Contact contact);
        void onMessageClicked(Contact contact);
        void onShowDetails(Contact contact);
    }
}
