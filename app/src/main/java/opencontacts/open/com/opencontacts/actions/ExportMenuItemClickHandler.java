package opencontacts.open.com.opencontacts.actions;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;
import opencontacts.open.com.opencontacts.data.datastore.DomainUtils;

/**
 * Created by sultanm on 1/21/18.
 */

public class ExportMenuItemClickHandler implements MenuItem.OnMenuItemClickListener{
    private Context context;

    public ExportMenuItemClickHandler(Context context) {
        this.context = context;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        new AlertDialog.Builder(context)
                .setMessage("Do you want to export?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, R.string.exporting_contacts_started, Toast.LENGTH_SHORT).show();
                        new AsyncTask<Void, Void, Boolean>() {
                            @Override
                            protected Boolean doInBackground(Void... params) {
                                try {
                                    DomainUtils.exportAllContacts(context);
                                } catch (IOException e) {
                                    return false;
                                }
                                return true;
                            }

                            @Override
                            protected void onPostExecute(Boolean success) {
                                if (Boolean.FALSE.equals(success))
                                    AndroidUtils.showAlert(context, "Failed", "Failed exporting contacts");
                                else
                                    Toast.makeText(context, R.string.exporting_contacts_complete, Toast.LENGTH_LONG).show();

                            }
                        }.execute();
                    }
                }).setNegativeButton("No", null).show();
        return true;
    }
}
