package opencontacts.open.com.opencontacts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.ContactsListFilter;
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.data.datastore.ContactsDataStore;
import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.DomainUtils;

import static opencontacts.open.com.opencontacts.activities.EditContactActivity.INTENT_EXTRA_STRING_PHONE_NUMBER;

public class AddToContactActivity extends AppCompatActivity {

    private ListView contactsListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String phoneNumber = getIntent().getStringExtra(INTENT_EXTRA_STRING_PHONE_NUMBER);
        if(phoneNumber == null)
            finish();

        setContentView(R.layout.activity_add_to_contact);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(R.string.add_to_contact);
        contactsListView = new ListView(this);
        contactsListView.setTextFilterEnabled(true);
        final List<Contact> contacts = ContactsDataStore.getAllContacts();
        final ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(this, android.R.layout.simple_list_item_1, android.R.id.text1, new ArrayList<>(contacts)) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new ContactsListFilter(contacts, this);
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView = super.getView(position, convertView, parent);
                ((TextView)(convertView.findViewById(android.R.id.text1))).setText(getItem(position).getName());
                return convertView;
            }
        };
        contactsListView.setAdapter(adapter);
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact tempContact = DomainUtils.getACopyOf(adapter.getItem(position));
                tempContact.getPhoneNumbers().add(phoneNumber);
                Intent editContact = new Intent(AddToContactActivity.this, EditContactActivity.class);
                editContact.putExtra(EditContactActivity.INTENT_EXTRA_CONTACT_CONTACT_DETAILS, tempContact);
                AddToContactActivity.this.startActivity(editContact);
            }
        });
        ((LinearLayout)findViewById(R.id.parent_linear_layout)).addView(contactsListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchView searchView = new SearchView(this);
        bindSearchViewToContacts(searchView);
        menu.add(R.string.search)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setActionView(searchView);
        return true;
    }

    private void bindSearchViewToContacts(SearchView searchView) {
        searchView.setInputType(InputType.TYPE_CLASS_PHONE);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                contactsListView.clearTextFilter();
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactsListView.setFilterText(newText);
                return true;
            }
        });

    }
}
