package opencontacts.open.com.opencontacts;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Telephone;

public class ImportVcardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_vcard);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        try {
            InputStream vcardInputStream = getContentResolver().openInputStream(uri);
            List<VCard> vCards = Ezvcard.parse(vcardInputStream).all();
            System.out.println(vCards.size());
            int index=0;
            for(VCard vcard : vCards){
                index++;
                if(vcard == null)
                    System.out.println("found null at index "+ index);
                System.out.println(vcard.getFormattedName());
                for(Telephone telephoneNumber : vcard.getTelephoneNumbers()){
                    System.out.println(telephoneNumber.getText());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_while_parsing_vcard_file, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_while_parsing_vcard_file, Toast.LENGTH_LONG).show();
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, R.string.unexpected_error_happened, Toast.LENGTH_LONG).show();
        }
    }
}
