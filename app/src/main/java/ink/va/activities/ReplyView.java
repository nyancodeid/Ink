package ink.va.activities;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.ink.va.R;

import org.apache.commons.lang3.StringEscapeUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.service.SendReplyService;
import ink.va.utils.SharedHelper;

public class ReplyView extends BaseActivity {

    @Bind(R.id.messageReceived)
    TextView mMessageReceived;
    @Bind(R.id.replyBody)
    EditText mReplyBody;
    @Bind(R.id.replyMessage)
    fab.FloatingActionButton mReplyMessage;
    private String mOpponentId;
    private String mCurrentUserId;
    private String userImage;
    private String opponentImage;
    private String userName;
    private String mDeleteUserId;
    private String mDeleteOpponentId;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        sharedHelper = new SharedHelper(this);
        if (extras != null) {
            mOpponentId = extras.getString("mOpponentId");
            mCurrentUserId = extras.getString("mCurrentUserId");
            userImage = extras.getString("userImage");
            opponentImage = extras.getString("opponentImage");
            userName = extras.getString("username");
            mDeleteUserId = extras.getString("deleteUserId");
            mDeleteOpponentId = extras.getString("deleteOpponentId");

            if (extras.containsKey("notificationId")) {
                Log.d("sfasfasfas", "onCreate: "+"contains");
                sharedHelper.removeLastNotificationId(mOpponentId);
            }else{
                Log.d("sfasfasfas", "onCreate: "+"not contains");
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(Integer.valueOf(mOpponentId));
            sharedHelper.removeLastNotificationId(mOpponentId);
            mMessageReceived.setText("Reply to" + " " + userName);
            mReplyBody.requestFocus();
            mReplyMessage.setEnabled(false);
        }
        mReplyBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() <= 0) {
                    mReplyMessage.setEnabled(false);
                } else {
                    mReplyMessage.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @OnClick(R.id.backButton)
    public void backButton() {
        finish();
    }

    @OnClick(R.id.replyMessage)
    public void replyMessage() {
        String messageToSend = StringEscapeUtils.escapeJava(mReplyBody.getText().toString().trim());
        Intent intent = new Intent(getApplicationContext(), SendReplyService.class);
        intent.putExtra("message", messageToSend);
        intent.putExtra("currentUserId", mCurrentUserId);
        intent.putExtra("opponentId", mOpponentId);
        intent.putExtra("userImage", userImage);
        intent.putExtra("opponentImage", opponentImage);
        intent.putExtra("deleteUserId", mDeleteUserId);
        intent.putExtra("deleteOpponentId", mDeleteOpponentId);
        startService(intent);
        finish();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }
}
