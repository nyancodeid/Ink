package ink.va.utils;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by USER on 2016-12-23.
 */

public class FragmentDialog extends DialogFragment {


    public static final int DIALOG_TYPE_INCOGNITO = 0;
    public static final int DIALOG_TYPE_HIDE_PROFILE = 1;

    public static final String DIALOG_TYPE_KEY = "dialog_type";
    private int type;


    static FragmentDialog newInstance(int type) {
        FragmentDialog fragmentDialog = new FragmentDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(DIALOG_TYPE_KEY, type);
        fragmentDialog.setArguments(bundle);
        return fragmentDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if (getArguments() != null) {
            type = getArguments().getInt(DIALOG_TYPE_KEY);
        }
        switch (type) {
            case DIALOG_TYPE_HIDE_PROFILE:
                view = inflater.inflate(R.layout.fragment_dialog_hide_profile, container, false);
                break;
            case DIALOG_TYPE_INCOGNITO:
                view = inflater.inflate(R.layout.fragment_dialog_incognito, container, false);
                break;
            default:
                view = inflater.inflate(R.layout.fragment_dialog_hide_profile, container, false);
        }
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void showDialog(@DialogTypes int type) {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = FragmentDialog.newInstance(type);
        newFragment.show(ft, "dialog");
    }

    public void hideDialog() {
        dismiss();
    }

    @OnClick(R.id.close)
    public void closeClicked() {
        hideDialog();
    }

    @IntDef({DIALOG_TYPE_INCOGNITO, DIALOG_TYPE_HIDE_PROFILE})
    private @interface DialogTypes {

    }
}
