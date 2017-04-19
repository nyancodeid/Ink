package ink.va.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Switch;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.SharedHelper;


public class Settings extends BaseActivity {

    @BindView(R.id.groupNotificationSwitch)
    Switch groupSwitch;

    @BindView(R.id.commentNotificationSwitch)
    Switch commentSwitch;


    @BindView(R.id.likeNotificationSwitch)
    Switch likeSwitch;

    @BindView(R.id.snowSwitch)
    Switch snowSwitch;

    @BindView(R.id.mafiaSwitch)
    Switch mafiaSwitch;

    @BindView(R.id.greetingSwitch)
    Switch greetingSwitch;


    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        sharedHelper = new SharedHelper(this);
        groupSwitch.setChecked(sharedHelper.showGroupNotification());
        commentSwitch.setChecked(sharedHelper.showCommentNotification());
        likeSwitch.setChecked(sharedHelper.showLikeNotification());
        snowSwitch.setChecked(sharedHelper.showSnow());
        mafiaSwitch.setChecked(sharedHelper.showMafiaNotification());
        greetingSwitch.setChecked(sharedHelper.showGreeting());

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setTitle(getString(R.string.title_settings));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.groupNotificationSwitchWrapper)
    public void setGroupNotificationSwitchWrapper() {
        if (groupSwitch.isChecked()) {
            groupSwitch.setChecked(false);
            sharedHelper.putShowGroupNotification(false);
        } else {
            groupSwitch.setChecked(true);
            sharedHelper.putShowGroupNotification(true);
        }
    }

    @OnClick(R.id.commentNotificationSwitchWrapper)
    public void setCommentNotificationSwitchWrapper() {
        if (commentSwitch.isChecked()) {
            commentSwitch.setChecked(false);
            sharedHelper.putShowCommentNotification(false);
        } else {
            commentSwitch.setChecked(true);
            sharedHelper.putShowCommentNotification(true);
        }
    }


    @OnClick(R.id.likeNotificationSwitchWrapper)
    public void setLikeNotificationSwitchWrapper() {
        if (likeSwitch.isChecked()) {
            likeSwitch.setChecked(false);
            sharedHelper.putShowLikeNotification(false);
        } else {
            likeSwitch.setChecked(true);
            sharedHelper.putShowLikeNotification(true);
        }
    }

    @OnClick(R.id.mafiaWrapper)
    public void mafiaWrapperClicked() {
        if (mafiaSwitch.isChecked()) {
            mafiaSwitch.setChecked(false);
            sharedHelper.putShowMafiaNotification(false);
        } else {
            mafiaSwitch.setChecked(true);
            sharedHelper.putShowMafiaNotification(true);
        }
    }

    @OnClick(R.id.greetingWrapper)
    public void greetingWrapperClicked() {
        if (greetingSwitch.isChecked()) {
            greetingSwitch.setChecked(false);
            sharedHelper.putShowGreeting(false);
        } else {
            greetingSwitch.setChecked(true);
            sharedHelper.putShowGreeting(true);
        }
    }

    @OnClick(R.id.snowWrapper)
    public void setSnowWrapper() {
        if (snowSwitch.isChecked()) {
            snowSwitch.setChecked(false);
            sharedHelper.putShowSnow(false);
        } else {
            snowSwitch.setChecked(true);
            sharedHelper.putShowSnow(true);
        }
    }

    @OnClick(R.id.backUpServiceWrapper)
    public void backUpClicked() {
        startActivity(new Intent(getApplicationContext(), BackUpActivity.class));
    }

    @OnClick(R.id.securityWrapper)
    public void securityClicked() {
        startActivity(new Intent(getApplicationContext(), SecurityActivity.class));
    }

    @OnClick(R.id.enterSecurity)
    public void enterSecurityClicked() {
        securityClicked();
    }

    @OnClick(R.id.enterBackUp)
    public void enterClicked() {
        backUpClicked();
    }
}
