package opencontacts.open.com.opencontacts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.FormattedName;
import ezvcard.property.Telephone;
import opencontacts.open.com.opencontacts.domain.Contact;

public class ImportVcardActivity extends AppCompatActivity {
    private String TOTAL_NUMBER_OF_VCARDS = "total_vcards";
    private String NUMBER_OF_VCARDS_IMPORTED_UNTIL_NOW = "number_of_vcards_imported_until_now";
    private String FINAL_RESULT_OF_IMPORT = "final_result_of_import";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_vcard);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        VCardParser parser = new VCardParser();
        parser.execute(uri, this);
    }
    private class VCardParser  extends AsyncTask {
        Context context;
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Uri uri = (Uri) params[0];
                context = (Context) params[1];
                InputStream vcardInputStream = context.getContentResolver().openInputStream(uri);
                List<VCard> vCards = Ezvcard.parse(vcardInputStream).all();
                publishProgress(TOTAL_NUMBER_OF_VCARDS, vCards.size());
                int numberOfvCardsImported=0, numberOfCardsIgnored = 0;
                for(VCard vcard : vCards){
                    FormattedName name = vcard.getFormattedName();
                    if(name == null){
                        numberOfCardsIgnored++;
                        continue;
                    }
                    Contact contact = new Contact();
                    contact.firstName = name.getValue();
                    contact.lastName = "";
                    List<Telephone> telephoneNumbers = vcard.getTelephoneNumbers();
                    if(telephoneNumbers.size() == 1)
                        contact.phoneNumber = telephoneNumbers.get(0).getText();
                    else if(telephoneNumbers.size() > 1){
                        contact.phoneNumber = telephoneNumbers.get(0).getText();
                        contact.extraNumbers = new ArrayList<String>();
                        for(int i=1, total=telephoneNumbers.size(); i<total; i++){
                            contact.extraNumbers.add(telephoneNumbers.get(i).getText());
                        }
                    }
                    contact.save();
                    numberOfvCardsImported++;
                    publishProgress(NUMBER_OF_VCARDS_IMPORTED_UNTIL_NOW, numberOfvCardsImported);
                }
                publishProgress(FINAL_RESULT_OF_IMPORT, numberOfvCardsImported, numberOfCardsIgnored);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(context, R.string.error_while_parsing_vcard_file, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, R.string.error_while_parsing_vcard_file, Toast.LENGTH_LONG).show();
            }
            catch(Exception e){
                e.printStackTrace();
                Toast.makeText(context, R.string.unexpected_error_happened, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            Toast.makeText(context, "Imported Successfully", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
