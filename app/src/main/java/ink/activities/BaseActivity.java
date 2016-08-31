package ink.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.ink.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.interfaces.AccountDeleteListener;
import ink.utils.SharedHelper;

/**
 * Created by USER on 2016-07-24.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private AccountDeleteListener accountDeleteListener;
    private SharedHelper sharedHelper;
    private View decorView;

    @Nullable
    @Bind(R.id.customizeToolbar)
    Toolbar customizeToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        sharedHelper = new SharedHelper(this);
        decorView = getWindow().getDecorView();

        ButterKnife.bind(decorView);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
        }
        Toolbar customizeToolbar = (Toolbar) decorView.findViewById(R.id.customizeToolbar);
        if (customizeToolbar != null) {
            Toast.makeText(BaseActivity.this, "not null", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(BaseActivity.this, "null", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        Toolbar customizeToolbar = (Toolbar) parent.findViewById(R.id.customizeToolbar);
        if (customizeToolbar != null) {
            Toast.makeText(BaseActivity.this, "not null", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(BaseActivity.this, "null", Toast.LENGTH_SHORT).show();
        }
        return super.onCreateView(parent, name, context, attrs);
    }

    protected void setOnAccountDeleteListener(AccountDeleteListener accountDeleteListener) {
        this.accountDeleteListener = accountDeleteListener;
    }

    protected void fireAccountDeleteListener() {
        if (accountDeleteListener != null) {
            accountDeleteListener.onAccountDeleted();
        }
    }


    protected boolean isSocialAccountRegistered() {
        return sharedHelper.isRegistered();
    }

    protected boolean isSocialAccount() {
        return sharedHelper.isSocialAccount();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
