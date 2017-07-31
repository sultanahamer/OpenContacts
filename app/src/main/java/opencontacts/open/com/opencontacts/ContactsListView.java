package opencontacts.open.com.opencontacts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.DomainUtils;

/**
 * Created by sultanm on 3/25/17.
 */

public class ContactsListView extends ListView {
    List <Contact> contacts;
    ContactsListView thisClass = this;
    public static final String ACTION_UPDATED = "action_updated";
    Context context;
    Contact selectedContact = null;
    ArrayAdapter<Contact> adapter;

    public ContactsListView(final Context context) {
        super(context);
        this.context = context;
        setTextFilterEnabled(true);
        List<Contact> contacts = DomainUtils.getAllContacts();

        final OnClickListener callContact = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact contact = (Contact) ((View)v.getParent()).getTag();
                AndroidUtils.call(contact.getPhoneNumber(), context);
            }
        };
        final OnClickListener messageContact = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact contact = (Contact) ((View)v.getParent()).getTag();
                AndroidUtils.message(contact.getPhoneNumber(), context);
            }
        };
        final OnClickListener editContact = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Contact contact = (Contact) v.getTag();
                selectedContact = contact;
                AndroidUtils.showContactDetails(contact, context);
            }
        };

        adapter = new ArrayAdapter<Contact>(context, R.layout.contact, contacts){
            private LayoutInflater layoutInflater = LayoutInflater.from(context);
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
                convertView.setOnClickListener(editContact);
                return convertView;
            }
        };
        sortContacts();
        this.setAdapter(adapter);
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
        adapter.add(DomainUtils.getContact(newContactId));
        sortContacts();
        adapter.notifyDataSetChanged();
    }

    public void update() {
        CharSequence textFilter = null;
        if(this.hasTextFilter())
            textFilter = this.getTextFilter();
        if(selectedContact == null)
            return;
        int position = adapter.getPosition(selectedContact);
        if(position == -1){
            selectedContact = null;
            return;
        }
        updateContactViewAt(position, selectedContact.getId());
        this.clearTextFilter();
        if(textFilter != null)
            this.setFilterText(textFilter.toString());
        selectedContact = null;
    }
}
