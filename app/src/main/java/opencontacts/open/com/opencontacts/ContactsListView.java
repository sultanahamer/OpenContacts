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

import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.ContactsDBHelper.ContactsDBHelper;

/**
 * Created by sultanm on 3/25/17.
 */

public class ContactsListView extends ListView {
    List <Contact> contacts;
    ContactsListView thisClass = this;
    public static final String ACTION_UPDATED = "action_updated";
    Context context;
    ArrayAdapter<Contact> adapter;

    public ContactsListView(final Context context, final OnClickListener callContact, final OnClickListener messageContact, final OnClickListener editContact) {
        super(context);
        this.context = context;
        this.contacts = ContactsDBHelper.getAllContacts();
        adapter = new ArrayAdapter<Contact>(context, R.layout.contact, this.contacts){
            private LayoutInflater layoutInflater = LayoutInflater.from(context);
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Contact contact = contacts.get(position);
                if(convertView == null)
                    convertView = layoutInflater.inflate(R.layout.contact, parent, false);
                ((TextView) convertView.findViewById(R.id.textview_full_name)).setText(contact.firstName + " " + contact.lastName);
                ((TextView) convertView.findViewById(R.id.textview_phone_number)).setText(contact.phoneNumber);
                ((ImageButton)convertView.findViewById(R.id.button_call)).setOnClickListener(callContact);
                ((ImageButton)convertView.findViewById(R.id.button_message)).setOnClickListener(messageContact);
                convertView.setTag(position);
                convertView.setOnClickListener(editContact);
                return convertView;
            }
        };
        this.setAdapter(adapter);
    }

    public void updateContactViewAt(int position, long contactId) throws Exception {
        Contact oldContactInView = adapter.getItem(position);
        if(oldContactInView == null)
            throw new Exception("Invalid contact position to be updated");
        else{
            adapter.remove(adapter.getItem(position));
            adapter.insert(ContactsDBHelper.getContactWithId(contactId), position);
        }
        adapter.notifyDataSetChanged();
    }

    public void addNewContactInView(long newContactId) {
        adapter.add(ContactsDBHelper.getContactWithId(newContactId));
        adapter.notifyDataSetChanged();
    }

    public Contact getContactAt(int position){
        return adapter.getItem(position);
    }

    public void deleteContactAt(int lastSelectedContactPosition) {
        adapter.remove(adapter.getItem(lastSelectedContactPosition));
        adapter.notifyDataSetChanged();
    }
}
