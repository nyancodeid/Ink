package ink.va.utils;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by USER on 2016-12-23.
 */

public class FragmentDialog extends DialogFragment {


    private ResultListener resultListener;
    private String firstParagpaphContent;
    private String secondParagraphContent;
    private Drawable backgroundResource;

    @Bind(R.id.fragmentDialogRoot)
    RelativeLayout fragmentDialogRoot;

    @Bind(R.id.firstParagraphTV)
    TextView firstParagraphTV;

    @Bind(R.id.secondParagraphTV)
    TextView secondParagraphTV;

    static FragmentDialog newInstance() {
        FragmentDialog fragmentDialog = new FragmentDialog();
        return fragmentDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentDialogRoot.setBackground(backgroundResource);
        firstParagraphTV.setText(firstParagpaphContent);
        secondParagraphTV.setText(secondParagraphContent);
    }

    public void showDialog(FragmentManager fragmentManager, @Nullable ResultListener resultListener,
                           String firstParagraphContent,
                           String secondParagraphContent, Drawable backgroundResource) {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        this.resultListener = resultListener;
        this.firstParagpaphContent = firstParagraphContent;
        this.secondParagraphContent = secondParagraphContent;
        this.backgroundResource = backgroundResource;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = FragmentDialog.newInstance();
        newFragment.show(fragmentTransaction, "dialog");
    }

    public void hideDialog() {
        dismiss();
        if (resultListener != null) {
            resultListener.onDialogClosed();
        }
    }

    @OnClick(R.id.close)
    public void closeClicked() {
        hideDialog();
    }

    @OnClick(R.id.doIt)
    public void doItClicked() {
        hideDialog();
    }


    public interface ResultListener {
        void onResultSuccess();

        void onDialogClosed();
    }
}
