package opencontacts.open.com.opencontacts;

import android.app.Activity;
import android.content.Context;
import android.widget.ListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;

/**
 * Created by sultanm on 3/25/17.
 */

public class ContactsListView extends ListView implements ContactsDataStore.ContactsDataChangeListener, ContactsListViewAdapter.ContactsListActionsListener {
    private final List <Contact> contacts;
    private Context activity;
    private ContactsListViewAdapter adapter;


    public ContactsListView(final Activity activity) {
        super(activity);
        this.activity = activity;
        setTextFilterEnabled(true);
        contacts = ContactsDataStore.getAllContacts();
        ContactsDataStore.addDataChangeListener(this);
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact1, Contact contact2) {
                return contact1.getName().compareToIgnoreCase(contact2.getName());
            }
        });

        adapter = new ContactsListViewAdapter(activity, R.layout.contact, contacts);
        adapter.setContactsListActionsListener(this);

        this.setAdapter(adapter);
    }

    @Override
    public void onUpdate(Contact contact) {
        contacts.remove(contact);
        adapter.remove(contact);

        adapter.add(contact);
        contacts.add(contact);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRemove(Contact contact) {
        adapter.remove(contact);
        contacts.remove(contact);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAdd(Contact contact) {
        adapter.add(contact);
        contacts.add(contact);
        adapter.notifyDataSetChanged();
    }

    public void onDestroy(){
        ContactsDataStore.removeDataChangeListener(this);
    }

    @Override
    public void onCallClicked(Contact contact) {
        AndroidUtils.call(contact.getPhoneNumber(), activity);
    }

    @Override
    public void onMessageClicked(Contact contact) {
        AndroidUtils.message(contact.getPhoneNumber(), activity);
    }

    @Override
    public void onShowDetails(Contact contact) {
        activity.startActivity(AndroidUtils.getIntentToShowContactDetails(contact, activity));
    }
}
