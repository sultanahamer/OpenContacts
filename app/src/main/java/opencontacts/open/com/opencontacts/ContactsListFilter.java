package opencontacts.open.com.opencontacts;

import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.List;

import opencontacts.open.com.opencontacts.domain.Contact;
import opencontacts.open.com.opencontacts.utils.DomainUtils;

public class ContactsListFilter extends Filter{
    private final List<Contact> contacts;
    private ArrayAdapter<Contact> adapter;

    public ContactsListFilter(List<Contact> contacts, ArrayAdapter<Contact> adapter){
        this.contacts = contacts;
        this.adapter = adapter;
    }
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        List<Contact> filteredContacts = DomainUtils.filter(constraint, contacts);
        results.values = filteredContacts;
        results.count = filteredContacts.size();
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.clear();
        if (constraint == null || constraint.length() == 0)
            adapter.addAll(contacts);
        else
            adapter.addAll((List<Contact>) results.values);
        adapter.notifyDataSetChanged();
    }
}
