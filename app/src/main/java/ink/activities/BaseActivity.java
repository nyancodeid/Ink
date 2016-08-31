package ink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.ink.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.interfaces.AccountDeleteListener;
import ink.utils.SharedHelper;

/**
 * Created by USER on 2016-07-24.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Nullable
    @Bind(R.id.customizeToolbar)
    Toolbar toolbar;
    private AccountDeleteListener accountDeleteListener;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        sharedHelper = new SharedHelper(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        if (toolbar == null) {
            Log.d("Fasfasfasfsafas", "setContentView: " + "null");
        } else {
            Log.d("Fasfasfasfsafas", "setContentView: " + "not null");
        }
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
