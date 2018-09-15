package opencontacts.open.com.opencontacts.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import opencontacts.open.com.opencontacts.R;
import opencontacts.open.com.opencontacts.interfaces.SelectableTab;
import opencontacts.open.com.opencontacts.utils.AndroidUtils;

import static opencontacts.open.com.opencontacts.activities.MainActivity.REQUESTCODE_FOR_ADD_CONTACT;

public class DialerFragment extends Fragment implements SelectableTab {
    private Context context;
    private View view;
    private EditText dialPadEditText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("creating damn yolo");
        View inflatedView = inflater.inflate(R.layout.dialer, container, false);
        return inflatedView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.context = getContext();
        this.view = view;
        linkDialerButtonsToHandlers();
    }

    private void linkDialerButtonsToHandlers() {
        dialPadEditText = (EditText) view.findViewById(R.id.editText_dialpad_number);
        view.findViewById(R.id.button_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.call(dialPadEditText.getText().toString(), context);
            }
        });

        view.findViewById(R.id.button_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.message(dialPadEditText.getText().toString(), context);
            }
        });

        view.findViewById(R.id.button_add_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToAddContact = AndroidUtils.getIntentToAddContact(dialPadEditText.getText().toString(), context);
                startActivity(intentToAddContact);
            }
        });
    }


    @Override
    public void onSelect() {
        EditText editText = dialPadEditText == null ? (EditText) view.findViewById(R.id.editText_dialpad_number) : dialPadEditText;
        AndroidUtils.showSoftKeyboard(editText, context);
    }

    @Override
    public void onUnSelect() {
        EditText editText = dialPadEditText == null ? (EditText) view.findViewById(R.id.editText_dialpad_number) : dialPadEditText;
        AndroidUtils.hideSoftKeyboard(editText, context);
    }
}
