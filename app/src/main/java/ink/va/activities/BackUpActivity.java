package ink.va.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.interfaces.BackUpManagerCallback;
import ink.va.managers.BackUpManager;

public class BackUpActivity extends BaseActivity implements BackUpManagerCallback {


    private BackUpManager backUpManager;

    @BindView(R.id.progress)
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        backUpManager = new BackUpManager(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackUpFinished() {

    }

    @Override
    public void onBackUpError(String friendlyErrorMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress(0, true);
        } else {
            progress.setProgress(0);
        }
    }

    @Override
    public void onRestoreFinished() {

    }

    @Override
    public void onRestoreError(String friendlyErrorMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress(0, true);
        } else {
            progress.setProgress(0);
        }
    }

    @Override
    public void onBackUpProgress(double percentCompleted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress((int) percentCompleted, true);
        } else {
            progress.setProgress((int) percentCompleted);
        }
    }

    @Override
    public void onRestoreProgress(double percentCompleted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress((int) percentCompleted, true);
        } else {
            progress.setProgress((int) percentCompleted);
        }
    }
}
