package ink.activities;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import ink.utils.AlertDialogView;
import ink.utils.PermissionsChecker;
import ink.utils.Regex;
import ink.utils.UserDetails;

public class ContactSupport extends BaseActivity {

    @Bind(R.id.issueTypeLayout)
    RelativeLayout issueTypeLayout;
    @Bind(R.id.chooseEmailLayout)
    RelativeLayout chooseEmailLayout;
    @Bind(R.id.issueTypeTV)
    TextView issueTypeTV;
    @Bind(R.id.emailTV)
    TextView emailTV;
    @Bind(R.id.sendIssueButton)
    FloatingActionButton sendIssueButton;
    @Bind(R.id.supportMessageInputField)
    EditText supportMessageInputField;
    @Bind(R.id.warningLayout)
    RelativeLayout warningLayout;
    Animation bounceAnimation;
    private String[] issueTypes;
    private static final int ACCOUNTS_REQUEST_CODE = 1;
    private List<String> userAccounts;
    private Animation slideDownAnimation;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_support);
        ButterKnife.bind(this);
        bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_animation);
        slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        userAccounts = UserDetails.getUserAccountList(this);
        ActionBar actionBar = getSupportActionBar();
        progressDialog = new ProgressDialog(ContactSupport.this);
        progressDialog.setTitle(getString(R.string.sending));
        progressDialog.setMessage(getString(R.string.messageBeingSent));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.contactSupport));
        }
        issueTypes = getResources().getStringArray(R.array.issueTypes);

        sendIssueButton.setEnabled(false);
        supportMessageInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    sendIssueButton.setEnabled(false);
                } else {
                    sendIssueButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @OnClick(R.id.issueTypeLayout)
    public void issueTypeLayout() {
        PopupMenu popupMenu = new PopupMenu(ContactSupport.this, issueTypeLayout);
        for (int i = 0; i < issueTypes.length; i++) {
            popupMenu.getMenu().add(0, i, i, issueTypes[i]);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                issueTypeTV.setText(item.getTitle());
                return false;
            }
        });
        popupMenu.show();
    }

    @OnClick(R.id.chooseEmailLayout)
    public void chooseEmailLayout() {
        System.gc();
        if (PermissionsChecker.isAccountPermissionGranted(getApplicationContext())) {
            initAccounts();
        } else {
            ActivityCompat.requestPermissions(ContactSupport.this, new String[]{Manifest.permission.GET_ACCOUNTS}, ACCOUNTS_REQUEST_CODE);
        }
    }

    @OnClick(R.id.sendIssueButton)
    public void sendIssueButton() {
        if (!issueTypeTV.getText().toString().equals(getString(R.string.chooseIssueType)) &&
                !emailTV.getText().toString().equals(getString(R.string.chooseEmail)) &&
                !supportMessageInputField.getText().toString().trim().isEmpty()) {
            sendSupportMessage();
        } else {
            warningLayout.startAnimation(bounceAnimation);
            bounceAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    warningLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void sendSupportMessage() {
        progressDialog.show();
        GMailSender gMailSender = new GMailSender();
        String senderEmail = emailTV.getText().toString();
        gMailSender.sendMail(Constants.SUBJECT_REQUEST_SUPPORT,
                "User message : " + supportMessageInputField.getText().toString().trim() + " \n\n Sender Email : " + senderEmail +
                        "\n\n Issue Type : " + issueTypeTV.getText().toString()
                , senderEmail, Constants.CONTACT_EMAIL, new GeneralCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                AlertDialogView.buildAlertDialog(ContactSupport.this,
                                        getString(R.string.messageSent),
                                        getString(R.string.supportMessageHint),
                                        false);
                                enableTouches();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String s) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                AlertDialogView.buildAlertDialog(ContactSupport.this,
                                        getString(R.string.failedToSent),
                                        getString(R.string.failedToSentMessage),
                                        false);
                                enableTouches();
                            }
                        });
                    }
                });
    }

    @OnClick(R.id.dismissSupportWarning)
    public void dismissSupportWarning() {
        warningLayout.startAnimation(slideDownAnimation);
        slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                warningLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private void disableTouches() {
        issueTypeLayout.setEnabled(false);
        chooseEmailLayout.setEnabled(false);
        supportMessageInputField.setEnabled(false);
    }

    private void enableTouches() {
        issueTypeLayout.setEnabled(true);
        chooseEmailLayout.setEnabled(true);
        supportMessageInputField.setEnabled(true);
        supportMessageInputField.setText("");
    }

    private void initAccounts() {
        System.gc();
        if (userAccounts.size() <= 0) {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ContactSupport.this);
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
                            emailTV.setText(inputEmail);
                            bottomSheetDialog.dismiss();
                        }
                    }
                }
            });
            bottomSheetDialog.show();
        } else {
            System.gc();
            PopupMenu popupMenu = new PopupMenu(ContactSupport.this, chooseEmailLayout);
            for (int i = 0; i < userAccounts.size(); i++) {
                String singleAccount = userAccounts.get(i);
                popupMenu.getMenu().add(0, i, i, singleAccount);
            }
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    emailTV.setText(item.getTitle().toString());
                    return false;
                }
            });
            popupMenu.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!PermissionsChecker.isAccountPermissionGranted(getApplicationContext())) {
            Snackbar.make(emailTV, getString(R.string.accountPermissionNeeded), Snackbar.LENGTH_LONG).show();
        } else {
            userAccounts = UserDetails.getUserAccountList(ContactSupport.this);
            initAccounts();
        }
    }


}
