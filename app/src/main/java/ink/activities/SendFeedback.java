package ink.activities;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.callbacks.GeneralCallback;
import ink.mail.GMailSender;
import ink.utils.Constants;
import ink.utils.Device;
import ink.utils.PermissionsChecker;
import ink.utils.Regex;
import ink.utils.UserDetails;

public class SendFeedback extends BaseActivity {
    private static final int ACCOUNTS_REQUEST_CODE = 1;
    private String mSenderEmail = "Anonymous";
    public String phoneManufacture = "No phone manufacture";
    public String phoneModel = "No phone model";
    public String processorsCount;
    @Bind(R.id.sendFeedbackButton)
    FloatingActionButton sendFeedbackButton;
    @Bind(R.id.feedbackInputField)
    EditText feedbackInputField;
    @Bind(R.id.actualEmail)
    TextView actualEmail;
    @Bind(R.id.emailWrapper)
    RelativeLayout emailWrapper;
    @Bind(R.id.useDeviceInfoCheckBox)
    android.support.v7.widget.AppCompatCheckBox useDeviceInfoCheckBox;
    private Snackbar progressSnack;
    private List<String> userAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_feedback);
        ButterKnife.bind(this);
        userAccounts = UserDetails.getUserAccountList(this);
        sendFeedbackButton.setEnabled(false);
        actualEmail.setText(mSenderEmail);
        progressSnack = Snackbar.make(sendFeedbackButton, getString(R.string.sendingFeedback), Snackbar.LENGTH_INDEFINITE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.feedbackTitle));
        }
        feedbackInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    sendFeedbackButton.setEnabled(false);
                } else {
                    sendFeedbackButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @OnClick(R.id.sendFeedbackButton)
    public void sendFeedbackButton() {
        disableTouches();
        progressSnack.show();
        sendFeedback(feedbackInputField.getText().toString().trim());
    }

    private void disableTouches() {
        sendFeedbackButton.setEnabled(false);
        feedbackInputField.setEnabled(false);
    }

    private void enableTouches() {
        sendFeedbackButton.setEnabled(true);
        feedbackInputField.setEnabled(true);
        feedbackInputField.setText("");
    }

    @OnClick(R.id.emailWrapper)
    public void emailWrapper() {
        if (PermissionsChecker.isAccountPermissionGranted(getApplicationContext())) {
            initAccounts();
        } else {
            ActivityCompat.requestPermissions(SendFeedback.this, new String[]{Manifest.permission.GET_ACCOUNTS}, ACCOUNTS_REQUEST_CODE);
        }
    }

    private void initAccounts() {
        System.gc();
        if (userAccounts.size() <= 0) {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SendFeedback.this);
            bottomSheetDialog.setContentView(R.layout.enter_email_view);
            final EditText userInputEmail = (EditText) bottomSheetDialog.findViewById(R.id.userInputEmail);
            Button doneInputEmail = (Button) bottomSheetDialog.findViewById(R.id.doneInputEmail);
            doneInputEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userInputEmail.getText().toString().trim().isEmpty()) {
                        userInputEmail.setError(getString(R.string.emalEmpty));
                    } else {
                        String inputEmail = userInputEmail.getText().toString().trim();
                        if (!Regex.isValidEmail(inputEmail)) {
                            userInputEmail.setError(getString(R.string.incorrectEmail));
                        } else {
                            mSenderEmail = inputEmail;
                            actualEmail.setText(inputEmail);
                            bottomSheetDialog.dismiss();
                        }
                    }
                }
            });
            bottomSheetDialog.show();
        } else {
            System.gc();
            PopupMenu popupMenu = new PopupMenu(SendFeedback.this, emailWrapper);
            for (int i = 0; i < userAccounts.size(); i++) {
                String singleAccount = userAccounts.get(i);
                popupMenu.getMenu().add(0, i, i, singleAccount);
            }
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    actualEmail.setText(item.getTitle().toString());
                    mSenderEmail = item.getTitle().toString();
                    return false;
                }
            });
            popupMenu.show();
        }
    }

    private void sendFeedback(String feedbackBody) {
        GMailSender gMailSender = new GMailSender();
        String deviceInfo = "\n\n No device information";
        if (useDeviceInfoCheckBox.isChecked()) {
            deviceInfo = "Phone Manufacture : " + Device.getDeviceManufacturer() +
                    "\n\n Phone Model : " + Device.getDeviceModel() +
                    "\n\n Phone number of cores : " + Device.getNumberOfCores() +
                    "\n\n Phone maximum heap size : " + Device.getMaximumHeapSize() +
                    "\n\n Phone available ram memory : " + Device.getFreeMemoryInBytes();
        }
        gMailSender.sendMail(Constants.SUBJECT_FEEDBACK,
                "User message : " + feedbackBody + " \n\n Sender Email : " + mSenderEmail
                        +
                        "\n\n Device information : \n" + deviceInfo

                , mSenderEmail, Constants.FEEDBACK_EMAIL, new GeneralCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressSnack.setText(getString(R.string.feedbackSent)).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        progressSnack.dismiss();
                                    }
                                }).show();
                                enableTouches();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String s) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressSnack.setText(getString(R.string.feedbackFailSent)).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        progressSnack.dismiss();
                                        finish();
                                    }
                                }).show();
                                enableTouches();
                            }
                        });
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!PermissionsChecker.isAccountPermissionGranted(getApplicationContext())) {
            Snackbar.make(sendFeedbackButton, getString(R.string.accountPermissionNeeded), Snackbar.LENGTH_LONG).show();
        } else {
            userAccounts = UserDetails.getUserAccountList(SendFeedback.this);
            initAccounts();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
