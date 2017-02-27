package ink.va.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.utils.SharedHelper;

public class SecurityScreen extends BaseActivity {

    @BindView(R.id.activity_security_screen)
    View securityScreenRoot;
    @BindView(R.id.fingerPrintLayout)
    View fingerPrintLayout;
    private SharedHelper mSharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_screen);
        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);

        if (mSharedHelper.getFeedColor() != null) {
            securityScreenRoot.setBackgroundColor(Color.parseColor(mSharedHelper.getFeedColor()));
        }
    }
}
