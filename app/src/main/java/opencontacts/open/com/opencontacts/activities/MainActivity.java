package opencontacts.open.com.opencontacts.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;

import java.util.List;

import opencontacts.open.com.opencontacts.CallLogListView;
import opencontacts.open.com.opencontacts.CallLogLoader;
import opencontacts.open.com.opencontacts.ContactsListView;
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.actions.ExportMenuItemClickHandler;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.DomainUtils;


public class MainActivity extends AppCompatActivity {
    public static final int CONTACTS_TAB_INDEX = 1;
    public static int REQUESTCODE_FOR_ADD_CONTACT = 1;
    public static int REQUESTCODE_FOR_SHOW_CONTACT_DETAILS = 2;
    public static final String INTENT_EXTRA_BOOLEAN_CONTACT_DELETED = "contact_deleted";
    public static final String INTENT_EXTRA_LONG_CONTACT_ID = "contact_id";
    private Toolbar toolbar;
    private ContactsListView contactsListView;
    private CallLogListView callLogListView;
    private CallLogLoader callLogLoader;
    private TabHost tabHost;
    private Menu menu;

    @Override
    protected void onResume() {
        super.onResume();
        if(callLogListView != null)
            refresh();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        callLogLoader = new CallLogLoader();
        setupTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.button_new).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent addContact = new Intent(MainActivity.this, EditContactActivity.class);
                addContact.putExtra(EditContactActivity.INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, true);
                startActivityForResult(addContact, REQUESTCODE_FOR_ADD_CONTACT);
                return false;
            }
        });
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabHost.setCurrentTab(CONTACTS_TAB_INDEX);
                searchView.requestFocus();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                contactsListView.clearTextFilter();
                return false;
            }
        });
        searchView.setInputType(InputType.TYPE_CLASS_PHONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactsListView.setFilterText(newText);
                return false;
            }
        });

        menu.findItem(R.id.action_export).setOnMenuItemClickListener(new ExportMenuItemClickHandler(this));
        return super.onCreateOptionsMenu(menu);
    }
    private void refresh(){
        new AsyncTask<Void, Void, List<CallLogEntry>>() {
            @Override
            protected List<CallLogEntry> doInBackground(Void... params) {
                return callLogLoader.loadCallLog(MainActivity.this);
            }

            @Override
            protected void onPostExecute(List<CallLogEntry> callLogEntries) {
                super.onPostExecute(callLogEntries);
                if(callLogEntries == null || callLogEntries.size() == 0)
                    return;
                callLogListView.addNewEntries(callLogEntries);
                DomainUtils.updateContactsAccessedDate(callLogEntries);
            }
        }.execute();

    }

    private void fillContactsTab() {
        if(contactsListView == null)
            contactsListView = new ContactsListView(this);
    }

    private void setupTabs() {
        tabHost = (TabHost)findViewById(R.id.tab_host);
        tabHost.setup();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("Call Log");
        spec.setContent(R.id.tab_call_log);
        spec.setIndicator("Call Log");
        tabHost.addTab(spec);

        //Tab 2
        spec = tabHost.newTabSpec("Contacts");
        spec.setContent(R.id.tab_contacts);
        spec.setIndicator("Contacts");
        tabHost.addTab(spec);

        //Tab 3
        spec = tabHost.newTabSpec("Dialer");
        spec.setContent(R.id.tab_dialer);
        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_dialpad_black_24dp));
        imageView.setPadding(10, 15, 10, 15);
        spec.setIndicator(imageView);
        tabHost.addTab(spec);

        linkDialerButtonsToHandlers();

        new AsyncTask<Void, String, Void>() {
            String callLogLoaded = "call log loaded";
            String contactsLoaded = "contacts loaded";

            @Override
            protected Void doInBackground(Void... params) {
                callLogLoader.loadCallLog(MainActivity.this);
                callLogListView = new CallLogListView(MainActivity.this);
                publishProgress(callLogLoaded);
                fillContactsTab();
                publishProgress(contactsLoaded);
                return null;
            }

            @Override
            protected void onProgressUpdate(String... progress) {
                super.onProgressUpdate(progress);
                if(callLogLoaded.equals(progress[0])){
                    LinearLayout call_logs_holder_layout  = (LinearLayout) findViewById(R.id.tab_call_log);
                    call_logs_holder_layout.addView(callLogListView);
                }

                if(contactsLoaded.equals(progress[0])) {
                    LinearLayout contacts_holder_layout  = (LinearLayout) findViewById(R.id.tab_contacts);
                    contacts_holder_layout.addView(contactsListView);
                }
            }
        }.execute();
    }

    private void linkDialerButtonsToHandlers() {
        final EditText editTextDialpadNumber = (EditText) findViewById(R.id.editText_dialpad_number);
        findViewById(R.id.button_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.call(editTextDialpadNumber.getText().toString(), getBaseContext());
            }
        });
        findViewById(R.id.button_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.message(editTextDialpadNumber.getText().toString(), getBaseContext());
            }
        });
        findViewById(R.id.button_add_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToAddContact = AndroidUtils.getIntentToAddContact(editTextDialpadNumber.getText().toString(), getBaseContext());
                startActivityForResult(intentToAddContact, MainActivity.REQUESTCODE_FOR_ADD_CONTACT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_CANCELED)
            return;
        long contactId = intent.getLongExtra(INTENT_EXTRA_LONG_CONTACT_ID, -1);
        if(requestCode == REQUESTCODE_FOR_ADD_CONTACT && resultCode == RESULT_OK)
            contactsListView.addNewContactInView(contactId);
        else if(requestCode == REQUESTCODE_FOR_SHOW_CONTACT_DETAILS && resultCode == RESULT_OK){
            if(intent.getBooleanExtra(INTENT_EXTRA_BOOLEAN_CONTACT_DELETED, false)){
                contactsListView.deleteContactInView(contactId);
            }
            else
                contactsListView.updateContactInView(contactId);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}