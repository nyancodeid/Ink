package ink.va.activities;

import android.app.NotificationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.callbacks.GeneralCallback;
import ink.va.models.ChatModel;
import ink.va.service.MessageService;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.RealmHelper;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;

import static ink.va.utils.Constants.EVENT_SEND_MESSAGE;
import static ink.va.utils.Constants.NOTIFICATION_MESSAGE_BUNDLE_KEY;

public class ReplyView extends BaseActivity {

    @BindView(R.id.replyToUserTV)
    TextView replyToUserTV;
    @BindView(R.id.replyBody)
    EditText mReplyBody;
    @BindView(R.id.replyMessage)
    fab.FloatingActionButton mReplyMessage;
    @BindView(R.id.sendProgress)
    View sendProgress;
    @BindView(R.id.opponentImage)
    ImageView opponentIV;

    private String mOpponentId;
    private String mCurrentUserId;
    private String opponentImage;
    private String mFirstName;
    private String mLastName;

    private SharedHelper sharedHelper;
    private boolean isSocialAccount;
    private MessageService messageService;
    private Gson chatGSON;
    private String jsonExtra;
    private JSONObject receivedMessageJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        sharedHelper = new SharedHelper(this);
        chatGSON = new Gson();
        if (extras != null) {
            jsonExtra = extras.getString(NOTIFICATION_MESSAGE_BUNDLE_KEY);

            try {
                receivedMessageJson = new JSONObject(jsonExtra);

                mOpponentId = receivedMessageJson.optString("userId");
                mCurrentUserId = receivedMessageJson.optString("opponentId");
                isSocialAccount = receivedMessageJson.optBoolean("isCurrentUserSocial");
                opponentImage = receivedMessageJson.optString("currentUserImage");
                mFirstName = receivedMessageJson.optString("firstName");
                mLastName = receivedMessageJson.optString("lastName");

                loadImage();

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(Integer.valueOf(mOpponentId));
                RealmHelper.getInstance().removeNotificationCount(Integer.valueOf(mOpponentId));

                sharedHelper.removeLastNotificationId(mOpponentId);
                replyToUserTV.setText(getString(R.string.replyTo) + " " + mFirstName + " " + mLastName);
                mReplyBody.requestFocus();
                mReplyMessage.setEnabled(false);

            } catch (JSONException e) {
                e.printStackTrace();
            }

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
        String messageToSend = StringEscapeUtils.escapeJava(mReplyBody.getText().toString().replaceAll(":\\)", "\u263A")
                .replaceAll(":\\(", "\u2639").replaceAll(":D", "\uD83D\uDE00"));
        if (messageService == null) {
            Toast.makeText(messageService, getString(R.string.feather_try_again), Toast.LENGTH_SHORT).show();
        } else {
            replyMessage.setVisibility(View.GONE);
            sendProgress.setVisibility(View.VISIBLE);

            messageService.connectSocket();

            final ChatModel chatModel = chatGSON.fromJson(receivedMessageJson.toString(), ChatModel.class);
            chatModel.setDate(Time.getCurrentTime());
            chatModel.setMessageId(String.valueOf(System.currentTimeMillis()));
            chatModel.setMessage(messageToSend);
            chatModel.setUserId(mCurrentUserId);
            chatModel.setOpponentId(mOpponentId);
            chatModel.setFirstName(sharedHelper.getFirstName());
            chatModel.setLastName(sharedHelper.getLastName());
            chatModel.setOpponentFirstName(mFirstName);
            chatModel.setOpponentLastName(mLastName);
            chatModel.setStickerUrl("");
            chatModel.setStickerChosen(false);
            chatModel.setSocialAccount(isSocialAccount);
            chatModel.setCurrentUserSocial(sharedHelper.isSocialAccount());
            chatModel.setCurrentUserImage(sharedHelper.getImageLink());
            chatModel.setOpponentImage(opponentImage);

            localMessageInsert(chatModel, receivedMessageJson);
        }
    }

    @Override
    public void onServiceConnected(MessageService messageService) {
        super.onServiceConnected(messageService);
        this.messageService = messageService;
    }

    private void localMessageInsert(ChatModel chatModel, final JSONObject messageJson) {
        RealmHelper.getInstance().insertMessage(chatModel, new GeneralCallback() {
            @Override
            public void onSuccess(Object o) {
                messageService.emit(EVENT_SEND_MESSAGE, messageJson);
                finish();
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            }

            @Override
            public void onFailure(Object o) {
                replyMessage.setVisibility(View.VISIBLE);
                sendProgress.setVisibility(View.GONE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ReplyView.this, getString(R.string.failedToSent), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadImage() {
        if (opponentImage != null && !opponentImage.isEmpty()) {
            if (isSocialAccount) {
                Ion.with(this).load(opponentImage).withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform())
                        .intoImageView(opponentIV);
            } else {
                String encodedImage = Uri.encode(opponentImage);
                Ion.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage)
                        .withBitmap().placeholder(R.drawable.user_image_placeholder).transform(new CircleTransform()).intoImageView(opponentIV);
            }
        } else {
            Ion.with(this).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform())
                    .intoImageView(opponentIV);
        }
    }

}
