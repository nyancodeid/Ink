package ink.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ink.interfaces.AccountDeleteListener;
import ink.utils.SharedHelper;

/**
 * Created by USER on 2016-07-24.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private AccountDeleteListener accountDeleteListener;
    private SharedHelper sharedHelper;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedHelper = new SharedHelper(this);
        decorView = getWindow().getDecorView();


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
}
