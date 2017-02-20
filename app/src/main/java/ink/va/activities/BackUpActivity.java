package ink.va.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.BackUpManagerCallback;
import ink.va.managers.BackUpManager;

public class BackUpActivity extends BaseActivity implements BackUpManagerCallback {


    private BackUpManager backUpManager;

    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.backUpMessages)
    RadioButton backUpMessages;
    @BindView(R.id.restoreMessages)
    RadioButton restoreMessages;
    @BindView(R.id.progressHintTV)
    TextView progressHintTV;
    private boolean anythingSelected;
    private boolean restoreChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        backUpManager = new BackUpManager(this);
        restoreMessages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    anythingSelected = true;
                    restoreChecked = true;
                    backUpMessages.setChecked(false);
                }
            }
        });
        backUpMessages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    restoreMessages.setChecked(false);
                    anythingSelected = true;
                    restoreChecked = false;
                }
            }
        });
    }

    @OnClick(R.id.proceedBackupRestore)
    public void proceedClicked() {
        if (!anythingSelected) {
            Snackbar.make(progress, getString(R.string.choseBackUpOption), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        } else {
            if (restoreChecked) {
                Toast.makeText(this, "restore checked", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "back up checked", Toast.LENGTH_SHORT).show();
            }
        }
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
