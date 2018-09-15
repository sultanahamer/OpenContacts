package opencontacts.open.com.opencontacts.data.datastore;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import opencontacts.open.com.opencontacts.interfaces.DataStoreChangeListener;
import opencontacts.open.com.opencontacts.orm.CallLogEntry;

public class CallLogDataStore {
    private static CallLogDBHelper callLogDBHelper = new CallLogDBHelper();
    private static List<CallLogEntry> callLogEntries = new ArrayList<>(1);
    private static List<DataStoreChangeListener<CallLogEntry>> dataChangeListeners = new ArrayList<>(3);

    public static List<CallLogEntry> loadRecentCallLogEntries(Context context) {
        final List<CallLogEntry> recentCallLogEntries = callLogDBHelper.loadRecentCallLogEntriesIntoDB(context);
        if(recentCallLogEntries.size() == 0)
            return recentCallLogEntries;
        ContactsDataStore.updateContactsAccessedDateAsync(recentCallLogEntries);
        updateStoreAsync(recentCallLogEntries);
        return recentCallLogEntries;
    }

    private static void updateStoreAsync(final List<CallLogEntry> recentCallLogEntries) {
        new Thread() {
            @Override
            public void run() {
                if(recentCallLogEntries.size() > 1){
                    refreshStore();
                }
                else if(recentCallLogEntries.size() == 1 && callLogEntries.size() > 0){
                    CallLogEntry callLogEntry = recentCallLogEntries.get(0);
                    callLogEntries.add(0, callLogEntry);
                    for(DataStoreChangeListener<CallLogEntry> dataStoreChangeListener: dataChangeListeners){
                        dataStoreChangeListener.onUpdate(callLogEntry);
                    }
                }
            }
        }.start();
    }

    private static void refreshStore() {
        if(callLogEntries.size() == 0)
            return;
        callLogEntries = CallLogDBHelper.getRecent100CallLogEntriesFromDB();
        for(DataStoreChangeListener<CallLogEntry> dataStoreChangeListener: dataChangeListeners){
            dataStoreChangeListener.onStoreRefreshed();
        }
    }

    public static List<CallLogEntry> getRecent100CallLogEntries(){
        if(callLogEntries.size() > 0)
            return callLogEntries;
        callLogEntries = CallLogDBHelper.getRecent100CallLogEntriesFromDB();
        return callLogEntries;
    }

    public static void addDataChangeListener(DataStoreChangeListener<CallLogEntry> changeListener) {
        dataChangeListeners.add(changeListener);
    }

    public static void removeDataChangeListener(DataStoreChangeListener<CallLogEntry> changeListener) {
        dataChangeListeners.remove(changeListener);
    }
}