package opencontacts.open.com.opencontacts.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import opencontacts.open.com.opencontacts.CallLogListView;
import opencontacts.open.com.opencontacts.CallLogLoader;
import opencontacts.open.com.opencontacts.ContactsListView;
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.DomainUtils;


public class MainActivity extends Activity implements TextWatcher {
    public static int REQUESTCODE_FOR_ADD_CONTACT = 1;
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
        if(callLogListView != null)
            refresh();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);
        callLogLoader = new CallLogLoader();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        findViewById(R.id.button_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addContact = new Intent(MainActivity.this, EditContactActivity.class);
                addContact.putExtra(EditContactActivity.INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, true);
                startActivityForResult(addContact, REQUESTCODE_FOR_ADD_CONTACT);
            }
        });
        setupTabs();
    }

    private void refresh(){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                return callLogLoader.loadCallLog(MainActivity.this);
            }

            @Override
            protected void onPostExecute(Object callLogEntries) {
                super.onPostExecute(callLogEntries);
                List<CallLogEntry> listCallLogEntries = (List<CallLogEntry>) callLogEntries;
                if(listCallLogEntries == null || listCallLogEntries.size() == 0)
                    return;
                callLogListView.addNewEntries(listCallLogEntries);
                DomainUtils.updateContactsAccessedDate(listCallLogEntries);
            }
        }.execute(new Object());

    }

    private void fillContactsTab() {
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
                tabHost.setCurrentTab(1);
                toolbar.setTitle("");
                searchBar.setVisibility(View.VISIBLE);
                stopSearch.setVisibility(View.VISIBLE);
                AndroidUtils.showSoftKeyboard(searchBar, MainActivity.this);
            }
        });

        final ImageButton exportToVCardFileButton = (ImageButton) findViewById(R.id.image_button_export_to_vcard_file);
        exportToVCardFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Do you want to export?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, R.string.exporting_contacts_started, Toast.LENGTH_SHORT).show();
                            new AsyncTask() {
                                @Override
                                protected Object doInBackground(Object[] params) {
                                    try {
                                        DomainUtils.exportAllContacts(MainActivity.this);
                                    } catch (IOException e) {
                                        return false;
                                    }
                                    return true;
                                }

                                @Override
                                protected void onPostExecute(Object success) {
                                    if (Boolean.FALSE.equals(success))
                                        AndroidUtils.showAlert(MainActivity.this, "Failed", "Failed exporting contacts");
                                    else
                                        Toast.makeText(MainActivity.this, R.string.exporting_contacts_complete, Toast.LENGTH_LONG).show();

                                }
                            }.execute(new Object());
                        }
                    }).setNegativeButton("No", null).show();
            }
        });

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

        new AsyncTask() {
            String callLogLoaded = "call log loaded";
            String contactsLoaded = "contacts loaded";


            @Override
            protected Object doInBackground(Object[] params) {
                callLogLoader.loadCallLog(MainActivity.this);
                callLogListView = new CallLogListView(MainActivity.this);
                publishProgress(callLogLoaded);
                fillContactsTab();
                publishProgress(contactsLoaded);
                return null;
            }

            @Override
            protected void onProgressUpdate(Object[] values) {
                super.onProgressUpdate(values);
                if(values[0].toString().equals(callLogLoaded)){
                    LinearLayout call_logs_holder_layout  = (LinearLayout) findViewById(R.id.tab_call_log);
                    call_logs_holder_layout.addView(callLogListView);
                }

                if(values[0].toString().equals(contactsLoaded)) {
                    LinearLayout contacts_holder_layout  = (LinearLayout) findViewById(R.id.tab_contacts);
                    contacts_holder_layout.addView(contactsListView);
                    findViewById(R.id.button_search).setVisibility(View.VISIBLE);
                    findViewById(R.id.image_button_export_to_vcard_file).setVisibility(View.VISIBLE);
                }
            }
        }.execute(new Object());
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


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        contactsListView.setFilterText(s.toString());
    }

    public void stopSearch(View view) {
        contactsListView.clearTextFilter();
        searchBar.setText("");
        searchBar.setVisibility(View.GONE);
        stopSearch.setVisibility(View.GONE);
        toolbar.setTitle(R.string.app_name);
    }
}