package opencontacts.open.com.opencontacts.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import opencontacts.open.com.opencontacts.CallLogListView;
import opencontacts.open.com.opencontacts.interfaces.SelectableTab;

public class CallLogFragment extends Fragment implements SelectableTab {

    private LinearLayout linearLayout;
    private CallLogListView callLogListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        linearLayout = new LinearLayout(getContext());
        ProgressBar progressBar = new ProgressBar(getContext());
        progressBar.setIndeterminate(true);
        linearLayout.addView(progressBar);
        return linearLayout;
    }

    public void addCallLog(CallLogListView callLogListView){
        this.callLogListView = callLogListView;
        linearLayout.removeAllViews();
        linearLayout.addView(callLogListView);
    }
    @Override
    public void onSelect() {}

    @Override
    public void onUnSelect() {

    }
}
