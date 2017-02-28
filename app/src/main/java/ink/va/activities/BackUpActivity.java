package ink.va.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.BackUpManagerCallback;
import ink.va.managers.BackupManager;
import ink.va.utils.PermissionsChecker;

public class BackUpActivity extends BaseActivity implements BackUpManagerCallback {


    private BackupManager backUpManager;

    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.backUpMessages)
    RadioButton backUpMessages;
    @BindView(R.id.restoreMessages)
    RadioButton restoreMessages;
    @BindView(R.id.progressHintTV)
    TextView progressHintTV;
    @BindView(R.id.proceedBackupRestore)
    Button proceedBackupRestore;
    private boolean anythingSelected;
    private boolean restoreChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        backUpManager = new BackupManager(this);
        backUpManager.setOnBackUpManagerCallback(this);
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
            proceedBackUp();
        }
    }

    private void proceedBackUp() {
        if (!PermissionsChecker.isStoragePermissionGranted(this)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }
        progress.setVisibility(View.VISIBLE);
        if (restoreChecked) {
            backUpManager.restoreMessages();
        } else {
            backUpManager.backUpMessages();
        }
        changeButtonState(false);
        progressHintTV.setVisibility(View.VISIBLE);
        progressHintTV.setTextColor(ContextCompat.getColor(this, R.color.red));
        progressHintTV.setText(getString(R.string.backupCaution));
    }

    private void changeButtonState(boolean active) {
        if (active) {
            proceedBackupRestore.setEnabled(true);
            proceedBackupRestore.setAlpha(1);
        } else {
            proceedBackupRestore.setEnabled(false);
            proceedBackupRestore.setAlpha((float) 0.5);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackUpFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeButtonState(true);
                progressHintTV.setTextColor(ContextCompat.getColor(BackUpActivity.this, R.color.darkGreen));
                progressHintTV.setText(getString(R.string.backUpMade));
            }
        });

    }

    @Override
    public void onBackUpError(final String friendlyErrorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progress.setProgress(0, true);
                } else {
                    progress.setProgress(0);
                }
                changeButtonState(true);
                progressHintTV.setTextColor(ContextCompat.getColor(BackUpActivity.this, R.color.red));
                progressHintTV.setText(getString(R.string.backUpError, friendlyErrorMessage));
            }
        });

    }

    @Override
    public void onRestoreFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeButtonState(true);
                progressHintTV.setTextColor(ContextCompat.getColor(BackUpActivity.this, R.color.darkGreen));
                progressHintTV.setText(getString(R.string.restoreMade));
            }
        });

    }

    @Override
    public void onRestoreError(final String friendlyErrorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progress.setProgress(0, true);
                } else {
                    progress.setProgress(0);
                }
                changeButtonState(true);
                progressHintTV.setTextColor(ContextCompat.getColor(BackUpActivity.this, R.color.red));
                progressHintTV.setText(getString(R.string.restoreError, friendlyErrorMessage));
            }
        });
    }

    @Override
    public void onBackUpProgress(final double percentCompleted) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progress.setProgress((int) percentCompleted, true);
                } else {
                    progress.setProgress((int) percentCompleted);
                }
            }
        });
    }

    @Override
    public void onRestoreProgress(final double percentCompleted) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progress.setProgress((int) percentCompleted, true);
                } else {
                    progress.setProgress((int) percentCompleted);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsChecker.isStoragePermissionGranted(this)) {
            proceedBackUp();
        }
    }

}
