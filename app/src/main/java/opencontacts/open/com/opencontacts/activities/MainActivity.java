package opencontacts.open.com.opencontacts.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.CallLogListView;
import opencontacts.open.com.opencontacts.ContactsListView;
import opencontacts.open.com.opencontacts.CallLogLoader;
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.actions.ExportMenuItemClickHandler;
import opencontacts.open.com.opencontacts.fragments.CallLogFragment;
import opencontacts.open.com.opencontacts.fragments.ContactsFragment;
import opencontacts.open.com.opencontacts.fragments.DialerFragment;
import opencontacts.open.com.opencontacts.interfaces.SelectableTab;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;
import opencontacts.open.com.opencontacts.utils.DomainUtils;


public class MainActivity extends AppCompatActivity {
    public static final int CONTACTS_TAB_INDEX = 1;
    public static final String DIALER = "Dialer";
    public static int REQUESTCODE_FOR_ADD_CONTACT = 1;
    public static int REQUESTCODE_FOR_SHOW_CONTACT_DETAILS = 2;
    public static final String INTENT_EXTRA_BOOLEAN_CONTACT_DELETED = "contact_deleted";
    public static final String INTENT_EXTRA_LONG_CONTACT_ID = "contact_id";
    private Toolbar toolbar;
    private ContactsListView contactsListView;
    private CallLogListView callLogListView;
    private CallLogLoader callLogLoader;
    private ViewPager viewPager;
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
                viewPager.setCurrentItem(CONTACTS_TAB_INDEX);
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

    private void setupTabs() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        final List<SelectableTab> tabs= new ArrayList<>();
        tabs.add(new CallLogFragment());
        tabs.add(new ContactsFragment());
        tabs.add(new DialerFragment());
        final String[] tabTitles = new String[]{"Call Log", "Contacts", ""};

        FragmentPagerAdapter fragmentStatePagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return tabTitles[position];
            }

            @Override
            public Fragment getItem(int position) {
                return (Fragment) tabs.get(position);
            }
        };
        viewPager.setAdapter(fragmentStatePagerAdapter);
        viewPager.setOffscreenPageLimit(3); //crazy shit with viewPager in case used with tablayout

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_dialpad_black_24dp);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabs.get(tab.getPosition()).onSelect();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tabs.get(tab.getPosition()).onUnSelect();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        new AsyncTask<Void, String, Void>() {
            String callLogLoaded = "call log loaded";
            String contactsLoaded = "contacts loaded";

            @Override
            protected Void doInBackground(Void... params) {
                callLogLoader.loadCallLog(MainActivity.this);
                callLogListView = new CallLogListView(MainActivity.this);
                publishProgress(callLogLoaded);
                if(contactsListView == null)
                    contactsListView = new ContactsListView(MainActivity.this);
                publishProgress(contactsLoaded);
                return null;
            }

            @Override
            protected void onProgressUpdate(String... progress) {
                super.onProgressUpdate(progress);
                if(callLogLoaded.equals(progress[0])){
                    ((CallLogFragment)tabs.get(0)).addCallLog(callLogListView);
                }

                if(contactsLoaded.equals(progress[0])) {
                    ((ContactsFragment)tabs.get(1)).addContactsList(contactsListView);
                }
            }
        }.execute();
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