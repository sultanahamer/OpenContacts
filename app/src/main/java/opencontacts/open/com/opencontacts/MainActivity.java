package opencontacts.open.com.opencontacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import java.util.List;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;


public class MainActivity extends Activity implements TextWatcher {
    private int REQUESTCODE_FOR_ADD_CONTACT = 1;
    public static int REQUESTCODE_FOR_SHOW_CONTACT_DETAILS = 2;
    public static final String INTENT_EXTRA_BOOLEAN_CONTACT_DELETED = "contact_deleted";
    public static final String INTENT_EXTRA_LONG_CONTACT_ID = "contact_id";
    private Toolbar toolbar;
    private EditText searchBar;
    private ImageButton stopSearch;
    private ContactsListView contactsListView;
    private CallLogListView callLogListView;
    private CallLogLoader callLogLoader;
    private TabHost tabHost;

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callLogLoader = new CallLogLoader();
        setContentView(R.layout.activity_tabbed);
        setupTabs();
    }

    private void refresh(){
        List<CallLogEntry> newCallLogEntries = callLogLoader.loadCallLog(MainActivity.this);
        if(newCallLogEntries == null)
            return;
        callLogListView.addNewEntries(newCallLogEntries);
    }
    private void fillCallLogTab() {
        LinearLayout call_logs_holder_layout  = (LinearLayout) findViewById(R.id.tab_call_log);
        new CallLogLoader().loadCallLog(this);
        callLogListView = new CallLogListView(this);
        call_logs_holder_layout.addView(callLogListView);
    }

    private void fillContactsTab() {
        LinearLayout contacts_holder_layout  = (LinearLayout) findViewById(R.id.contacts_holder);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        searchBar = (EditText) findViewById(R.id.text_edit_search_box);
        searchBar.addTextChangedListener(this);
        stopSearch = (ImageButton) findViewById(R.id.image_button_stop_search);
        stopSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSearch(v);
            }
        });
        ImageButton searchButton = (ImageButton) findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchContact(v);
            }
        });

        if(contactsListView == null)
            contactsListView = new ContactsListView(this);
        contacts_holder_layout.addView(contactsListView);
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

        fillContactsTab();
        fillCallLogTab();
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

    public void addContact(View view) {
        Intent addContact = new Intent(this, EditContactActivity.class);
        addContact.putExtra(EditContactActivity.INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, true);
        startActivityForResult(addContact, REQUESTCODE_FOR_ADD_CONTACT);
    }

    public void searchContact(View view) {
        toolbar.setTitle("");
        searchBar.setVisibility(View.VISIBLE);
        stopSearch.setVisibility(View.VISIBLE);
        searchBar.requestFocus();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().equals(""))
            contactsListView.clearTextFilter();
        contactsListView.setFilterText(s.toString());
    }

    public void stopSearch(View view) {
        contactsListView.clearTextFilter();
        searchBar.setVisibility(View.GONE);
        stopSearch.setVisibility(View.GONE);
        toolbar.setTitle(R.string.app_name);
    }
}
