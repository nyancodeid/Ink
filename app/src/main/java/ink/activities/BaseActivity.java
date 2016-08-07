package ink.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ink.utils.SharedHelper;

/**
 * Created by USER on 2016-07-24.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedHelper = new SharedHelper(this);
    }

    protected boolean isSocialAccount() {
        return sharedHelper.isSocialAccount();
    }
}
