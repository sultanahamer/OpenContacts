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
import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.actions.ExportMenuItemClickHandler;
import opencontacts.open.com.opencontacts.data.datastore.CallLogDataStore;
import opencontacts.open.com.opencontacts.fragments.CallLogFragment;
import opencontacts.open.com.opencontacts.fragments.ContactsFragment;
import opencontacts.open.com.opencontacts.fragments.DialerFragment;
import opencontacts.open.com.opencontacts.interfaces.SelectableTab;


public class MainActivity extends AppCompatActivity {
    public static final int CONTACTS_TAB_INDEX = 1;
    public static final String INTENT_EXTRA_LONG_CONTACT_ID = "contact_id";
    private Toolbar toolbar;
    private ContactsListView contactsListView;
    private CallLogListView callLogListView;
    private ViewPager viewPager;
    private SearchView searchView;

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
        setupTabs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contactsListView.onDestroy();
        callLogListView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        menu.findItem(R.id.button_new).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent addContact = new Intent(MainActivity.this, EditContactActivity.class);
                addContact.putExtra(EditContactActivity.INTENT_EXTRA_BOOLEAN_ADD_NEW_CONTACT, true);
                startActivity(addContact);
                return false;
            }
        });
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
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
    private void refresh() {
        new Thread(){
            @Override
            public void run() {
                CallLogDataStore.loadRecentCallLogEntries(MainActivity.this);
            }
        }.start();
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
        tabLayout.getTabAt(2).setIcon(R.drawable.dial_pad);
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
                CallLogDataStore.loadRecentCallLogEntries(MainActivity.this);
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

    public void collapseSearchView(){
        searchView.onActionViewCollapsed();
    }
}