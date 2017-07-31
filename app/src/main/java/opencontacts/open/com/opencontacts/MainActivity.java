package opencontacts.open.com.opencontacts;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.ArrayList;

import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.utils.ContactsDBHelper;

import static opencontacts.open.com.opencontacts.ContactsListView.OnClickListener;


public class MainActivity extends Activity implements TextWatcher {
    private String preferenceLastCallLogSavedDate = "preference_last_call_log_saved_date";
    private int REQUESTCODE_FOR_ADD_CONTACT = 1;
    private int REQUESTCODE_FOR_UPDATE_CONTACT = 2;
    private int lastSelectedContactPosition = -1;
    private Toolbar toolbar;
    private EditText searchBar;
    private ImageButton stopSearch;
    ContactsListView contactsListView;
    CallLogListView callLogListView;

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onresume Running");
        contactsListView.update();
        callLogListView.update();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);
        setupTabs();
    }

    private void fillCallLogTab() {
        LinearLayout call_logs_holder_layout  = (LinearLayout) findViewById(R.id.tab_call_log);
        loadCallLog();
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
        stopSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSearch(v);
            }
        });
        ImageButton searchButton = (ImageButton) findViewById(R.id.button_search);
        searchButton.setOnClickListener(new OnClickListener() {
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
        TabHost host = (TabHost)findViewById(R.id.tab_host);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Call Log");
        spec.setContent(R.id.tab_call_log);
        spec.setIndicator("Call Log");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Contacts");
        spec.setContent(R.id.tab_contacts);
        spec.setIndicator("Contacts");
        host.addTab(spec);

        fillContactsTab();
        fillCallLogTab();
    }

    private void loadCallLog() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, R.string.grant_read_call_logs_permission, Toast.LENGTH_LONG).show();
            return;
        }
        Cursor c = getContentResolver().query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.TYPE, CallLog.Calls.DATE}, CallLog.Calls.DATE + " > ?", new String[]{getLastSavedCallLogDate()}, CallLog.Calls.DATE + " DESC");
        String num, date, duration, callType;
        ArrayList<CallLogEntry> callLogEntries = new ArrayList<>();
        if(c.getCount() == 0)
            return;
        while(c.moveToNext()){
            num = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));// for  number
            duration = c.getString(c.getColumnIndex(CallLog.Calls.DURATION));// for duration
            date = c.getString(c.getColumnIndex(CallLog.Calls.DATE));// for duration
            callType = c.getString(c.getColumnIndex(CallLog.Calls.TYPE));// for call type, Incoming or out going
            opencontacts.open.com.opencontacts.orm.Contact contact = ContactsDBHelper.getContact(num);
            if(contact == null)
                callLogEntries.add(new CallLogEntry(null, (long)-1, num, duration, callType, date));
            else
                callLogEntries.add(new CallLogEntry(contact.toString(), contact.getId(), num, duration, callType, date));
        }
        CallLogEntry.saveInTx(callLogEntries);
        c.moveToFirst();
        setLastSavedCallLogDate(c.getString(c.getColumnIndex(CallLog.Calls.DATE)));
    }

    private String getLastSavedCallLogDate() {
        return AndroidUtils.getAppsSharedPreferences(this).getString(preferenceLastCallLogSavedDate, "0");
    }

    private void setLastSavedCallLogDate(String date) {
        AndroidUtils.getAppsSharedPreferences(this).edit().putString(preferenceLastCallLogSavedDate, date).apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_CANCELED)
            return;
        long contactId = intent.getLongExtra(EditContactActivity.INTENT_EXTRA_LONG_CONTACT_ID, -1);
        if(requestCode == REQUESTCODE_FOR_ADD_CONTACT && resultCode == RESULT_OK)
            contactsListView.addNewContactInView(contactId);
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
