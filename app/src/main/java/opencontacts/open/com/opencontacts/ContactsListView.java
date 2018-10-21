package opencontacts.open.com.opencontacts;

import android.content.Context;
import android.widget.ListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.interfaces.DataStoreChangeListener;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;

/**
 * Created by sultanm on 3/25/17.
 */

public class ContactsListView extends ListView implements DataStoreChangeListener<Contact>, ContactsListViewAdapter.ContactsListActionsListener {
    private final List <Contact> contacts;
    private Context context;
    private ContactsListViewAdapter adapter;


    public ContactsListView(final Context context) {
        super(context);
        this.context = context;
        setTextFilterEnabled(true);
        contacts = ContactsDataStore.getAllContacts();
        ContactsDataStore.addDataChangeListener(this);
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact1, Contact contact2) {
                return contact1.getName().compareToIgnoreCase(contact2.getName());
            }
        });
        adapter = new ContactsListViewAdapter(context, R.layout.contact, contacts);
        adapter.setContactsListActionsListener(this);

        this.setAdapter(adapter);
    }

    @Override
    public void onUpdate(final Contact contact) {
        this.post(new Runnable() {
            @Override
            public void run() {
                contacts.remove(contact);
                contacts.add(contact);

                adapter.remove(contact);
                adapter.add(contact);
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onRemove(final Contact contact) {
        this.post(new Runnable() {
            @Override
            public void run() {
                contacts.remove(contact);
                adapter.remove(contact);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onAdd(final Contact contact) {
        this.post(new Runnable() {
            @Override
            public void run() {
                contacts.add(contact);
                adapter.add(contact);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStoreRefreshed() {
    }

    public void onDestroy(){
        ContactsDataStore.removeDataChangeListener(this);
    }

    @Override
    public void onCallClicked(Contact contact) {
        AndroidUtils.call(contact.getPhoneNumber(), context);
    }

    @Override
    public void onMessageClicked(Contact contact) {
        AndroidUtils.message(contact.getPhoneNumber(), context);
    }

    @Override
    public void onShowDetails(Contact contact) {
        context.startActivity(AndroidUtils.getIntentToShowContactDetails(contact.getId(), context));
    }
}
