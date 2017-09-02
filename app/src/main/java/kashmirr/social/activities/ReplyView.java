package kashmirr.social.activities;

import android.app.NotificationManager;
import android.content.Intent;
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
import com.kashmirr.social.R;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kashmirr.social.callbacks.GeneralCallback;
import kashmirr.social.models.ChatModel;
import kashmirr.social.service.SocketService;
import kashmirr.social.utils.Constants;
import kashmirr.social.utils.ImageLoader;
import kashmirr.social.utils.RealmHelper;
import kashmirr.social.utils.SharedHelper;
import kashmirr.social.utils.Time;

import static kashmirr.social.utils.Constants.EVENT_SEND_MESSAGE;
import static kashmirr.social.utils.Constants.NOTIFICATION_MESSAGE_BUNDLE_KEY;
import static kashmirr.social.utils.Constants.STATUS_DELIVERED;


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
    @BindView(R.id.userMessage)
    TextView userMessageTV;

    private String mOpponentId;
    private String mCurrentUserId;
    private String opponentImage;
    private String mFirstName;
    private String mLastName;

    private SharedHelper sharedHelper;
    private boolean isSocialAccount;
    private SocketService socketService;
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

        ping();

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
                String message = receivedMessageJson.optString("message").isEmpty() ? mFirstName + " " + mLastName + " " +
                        getString(R.string.sentSticker) : receivedMessageJson.optString("message");

                userMessageTV.setText(getString(R.string.lastMessage) + " " + getString(R.string.quoteOpen) + message + getString(R.string.quoteClose));

                loadImage();

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(Integer.valueOf(mOpponentId));
                RealmHelper.getInstance().removeNotificationCount(this, Integer.valueOf(mOpponentId));

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


    @OnClick(R.id.goToIcon)
    public void goToIconClicked() {
        Intent requestsViewIntent = new Intent(this, Chat.class);
        requestsViewIntent.putExtra(NOTIFICATION_MESSAGE_BUNDLE_KEY, receivedMessageJson.toString());
        requestsViewIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        requestsViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(requestsViewIntent);
        finish();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }


    @OnClick(R.id.backButton)
    public void backButton() {
        finish();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    @OnClick(R.id.replyMessage)
    public void replyMessage() {
        String messageToSend = mReplyBody.getText().toString().replaceAll(":\\)", "\u263A")
                .replaceAll(":\\(", "\u2639").replaceAll(":D", "\uD83D\uDE00").trim();
        if (socketService == null) {
            Toast.makeText(socketService, getString(R.string.feather_try_again), Toast.LENGTH_SHORT).show();
        } else {
            replyMessage.setVisibility(View.GONE);
            sendProgress.setVisibility(View.VISIBLE);

            socketService.connectSocket();

            final ChatModel chatModel = chatGSON.fromJson(receivedMessageJson.toString(), ChatModel.class);
            chatModel.setDate(Time.getCurrentTime());
            chatModel.setMessageId(String.valueOf(System.currentTimeMillis()));
            chatModel.setMessage(messageToSend);
            chatModel.setUserId(mCurrentUserId);
            chatModel.setOpponentId(mOpponentId);
            chatModel.setFirstName(mFirstName);
            chatModel.setLastName(mLastName);
            chatModel.setOpponentFirstName(sharedHelper.getFirstName());
            chatModel.setOpponentLastName(sharedHelper.getLastName());
            chatModel.setStickerUrl("");
            chatModel.setDeliveryStatus(STATUS_DELIVERED);
            chatModel.setStickerChosen(false);
            chatModel.setSocialAccount(isSocialAccount);
            chatModel.setCurrentUserSocial(sharedHelper.isSocialAccount());
            chatModel.setCurrentUserImage(sharedHelper.getImageLink());
            chatModel.setOpponentImage(opponentImage);

            try {
                receivedMessageJson.put("messageId", System.currentTimeMillis());
                receivedMessageJson.put("userId", sharedHelper.getUserId());
                receivedMessageJson.put("opponentId", mOpponentId);
                receivedMessageJson.put("firstName", sharedHelper.getFirstName());
                receivedMessageJson.put("lastName", sharedHelper.getLastName());
                receivedMessageJson.put("opponentFirstName", mFirstName);
                receivedMessageJson.put("opponentLastName", mLastName);
                receivedMessageJson.put("opponentImage", opponentImage);
                receivedMessageJson.put("currentUserImage", sharedHelper.getImageLink());
                receivedMessageJson.put("isSocialAccount", isSocialAccount);
                receivedMessageJson.put("isCurrentUserSocial", sharedHelper.isSocialAccount());
                receivedMessageJson.put("message", messageToSend);
                receivedMessageJson.put("date", Time.getCurrentTime());
                receivedMessageJson.put("stickerChosen", false);
                receivedMessageJson.put("stickerUrl", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            localMessageInsert(chatModel, receivedMessageJson);
        }
    }

    @Override
    public void onServiceConnected(SocketService socketService) {
        super.onServiceConnected(socketService);
        this.socketService = socketService;
    }

    private void localMessageInsert(ChatModel chatModel, final JSONObject messageJson) {
        RealmHelper.getInstance().insertMessage(chatModel, new GeneralCallback() {
            @Override
            public void onSuccess(Object o) {
                socketService.setEmitListener(new GeneralCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        socketService.destroyEmitListener();
                        finish();
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                    }

                    @Override
                    public void onFailure(Object o) {

                    }
                });
                socketService.emit(EVENT_SEND_MESSAGE, messageJson);
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

                ImageLoader.loadImage(this, true, false, opponentImage,
                        0, R.drawable.user_image_placeholder, opponentIV, null);
            } else {
                String encodedImage = Uri.encode(opponentImage);
                ImageLoader.loadImage(this, true, false, Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage,
                        0, R.drawable.user_image_placeholder, opponentIV, null);
            }
        } else {
            ImageLoader.loadImage(this, true, true, null,
                    R.drawable.no_image, R.drawable.user_image_placeholder, opponentIV, null);
        }
    }

}
