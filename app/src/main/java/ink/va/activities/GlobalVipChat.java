package ink.va.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.adapters.VipGlobalChatAdapter;
import ink.va.interfaces.ItemClickListener;
import ink.va.interfaces.RequestCallback;
import ink.va.interfaces.VipGlobalChatClickListener;
import ink.va.models.UserModel;
import ink.va.models.VipGlobalChatModel;
import ink.va.models.VipGlobalChatResponseModel;
import ink.va.utils.DialogUtils;
import ink.va.utils.Keyboard;
import ink.va.utils.ProgressDialog;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.User;
import okhttp3.ResponseBody;

import static android.view.KeyEvent.KEYCODE_ENTER;
import static ink.va.utils.Constants.VIP_GLOBAL_CHAT_TYPE_DELETE;
import static ink.va.utils.Constants.VIP_GLOBAL_CHAT_TYPE_GET;
import static ink.va.utils.Constants.VIP_GLOBAL_CHAT_TYPE_SEND;

public class GlobalVipChat extends BaseActivity implements VipGlobalChatClickListener {

    @BindView(R.id.globalChatRecycler)
    RecyclerView globalChatRecycler;
    @BindView(R.id.noVipMessages)
    TextView noVipMessages;
    @BindView(R.id.globalChatField)
    EditText globalChatField;
    @BindView(R.id.sendGlobalMessage)
    ImageView sendGlobalMessage;
    @BindView(R.id.sendingProgress)
    View sendingProgress;
    @BindView(R.id.refreshGlobalChat)
    ImageView refreshGlobalChat;
    @BindView(R.id.newMessageWrapper)
    RelativeLayout newMessageWrapper;

    private String chosenMembership;
    private VipGlobalChatAdapter vipGlobalChatAdapter;
    private Gson gson;
    private Typeface typeface;
    private SharedHelper sharedHelper;
    private LinearLayoutManager linearLayoutManager;
    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;
    private ProgressDialog transferDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_vip_chat);
        ButterKnife.bind(this);
        transferDialog = new ProgressDialog();
        transferDialog.setTitle(getString(R.string.trasnferring));
        transferDialog.setMessage(getString(R.string.trasnferring_coins_text));
        sharedHelper = new SharedHelper(this);
        setStatusBarColor(R.color.vip_status_bar_color);
        hideActionBar();
        sendGlobalMessage.setEnabled(false);
        fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeOutAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        globalChatRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    changeNewMessageVisibility(false);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        initTextListener();
        initKeyListener();

        gson = new Gson();
        typeface = Typeface.createFromAsset(getAssets(), "fonts/vip_regular.ttf");
        noVipMessages.setTypeface(typeface);
        vipGlobalChatAdapter = new VipGlobalChatAdapter(this);
        vipGlobalChatAdapter.setVipGlobalChatClickListener(this);
        chosenMembership = getIntent().getExtras() != null ? getIntent().getExtras().getString("membershipType") : null;
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        globalChatRecycler.setLayoutManager(linearLayoutManager);
        globalChatRecycler.setAdapter(vipGlobalChatAdapter);
        getMessages();
    }

    private void initKeyListener() {
        globalChatField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                switch (keyEvent.getAction()) {
                    case KEYCODE_ENTER:
                        if (!globalChatField.getText().toString().trim().isEmpty()) {
                            sendMessage(globalChatField.getText().toString());
                        }

                        break;
                }
                return false;
            }
        });
    }


    private void initTextListener() {
        globalChatField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    sendGlobalMessage.setEnabled(false);
                } else {
                    sendGlobalMessage.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    private void scrollToBottom() {
        globalChatRecycler.post(new Runnable() {
            @Override
            public void run() {
                globalChatRecycler.scrollToPosition(vipGlobalChatAdapter.getItemCount() - 1);
            }
        });

    }

    private void getMessages() {
        showVipLoading();
        vipGlobalChatAdapter.clear();
        makeRequest(Retrofit.getInstance().getInkService().vipGlobalChatAction(null, null, null, VIP_GLOBAL_CHAT_TYPE_GET), null, false, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                VipGlobalChatResponseModel vipGlobalChatResponseModel = (VipGlobalChatResponseModel) result;
                hideVipLoading();
                if (vipGlobalChatResponseModel.isSuccess()) {
                    if (!vipGlobalChatResponseModel.getVipGlobalChatModels().isEmpty()) {
                        hideNoChat();
                        scrollToBottom();
                        vipGlobalChatAdapter.setChatModels(vipGlobalChatResponseModel.getVipGlobalChatModels());
                    } else {
                        showNoChat();
                    }
                } else {
                    Snackbar.make(globalChatRecycler, getString(R.string.serverErrorText), Snackbar.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onRequestFailed(Object[] result) {
                hideVipLoading();
            }
        });
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            RemoteMessage remoteMessage = extras.getParcelable("data");

            String messageId = remoteMessage.getData().get("messageId");
            String senderId = remoteMessage.getData().get("senderId");

            if (!senderId.equals(sharedHelper.getUserId())) {
                String message = remoteMessage.getData().get("message");
                String senderImageUrl = remoteMessage.getData().get("senderImageUrl");
                String firstName = remoteMessage.getData().get("firstName");
                String lastName = remoteMessage.getData().get("lastName");
                String isSocialAccount = remoteMessage.getData().get("isSocialAccount");
                String vipMembershipType = remoteMessage.getData().get("vipMembershipType");
                String gender = remoteMessage.getData().get("gender");

                UserModel userModel = new UserModel();
                userModel.setImageUrl(senderImageUrl);
                userModel.setFirstName(firstName);
                userModel.setLastName(lastName);
                userModel.setUserId(senderId);
                userModel.setSocialAccount(Boolean.valueOf(isSocialAccount));
                userModel.setGender(gender);
                userModel.setVipMembershipType(vipMembershipType);


                VipGlobalChatModel vipGlobalChatModel = new VipGlobalChatModel();
                vipGlobalChatModel.setMessage(message);
                vipGlobalChatModel.setMessageId(Integer.valueOf(messageId));
                vipGlobalChatModel.setSenderId(senderId);
                vipGlobalChatModel.setUser(userModel);

                if (linearLayoutManager.findLastVisibleItemPosition() != vipGlobalChatAdapter.getItemCount() - 1) {
                    changeNewMessageVisibility(true);
                }

                vipGlobalChatAdapter.insertItem(vipGlobalChatModel);
                hideNoChat();

            }
        }
    };

    private void changeNewMessageVisibility(boolean visible) {
        if (visible && newMessageWrapper.getVisibility() != View.VISIBLE) {

            fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    newMessageWrapper.setVisibility(View.VISIBLE);
                    newMessageWrapper.setEnabled(true);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            newMessageWrapper.startAnimation(fadeInAnimation);
        } else if (!visible && newMessageWrapper.getVisibility() != View.GONE) {
            fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    newMessageWrapper.setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    newMessageWrapper.setVisibility(View.GONE);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            newMessageWrapper.startAnimation(fadeOutAnimation);
        }
    }

    @OnClick(R.id.newMessageWrapper)
    public void hideNewMessage() {
        changeNewMessageVisibility(false);
        scrollToBottom();
    }

    @Override
    protected void onStart() {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(getPackageName() + ".GlobalVipChat"));
        super.onStart();
    }


    private boolean isLastItemDisplaying() {
        if (globalChatRecycler.getAdapter().getItemCount() != 0) {
            int lastVisibleItemPosition = ((LinearLayoutManager) globalChatRecycler.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == globalChatRecycler.getAdapter().getItemCount() - 1)
                return true;
        }
        return false;
    }

    private void showNoChat() {
        if (noVipMessages.getVisibility() == View.GONE) {
            noVipMessages.setVisibility(View.VISIBLE);
        }

    }

    private void hideNoChat() {
        if (noVipMessages.getVisibility() == View.VISIBLE) {
            noVipMessages.setVisibility(View.GONE);
        }

    }

    @Override
    public void onBackPressed() {
        finish();
        overrideActivityAnimation();
    }

    @Override
    public void onItemClicked(VipGlobalChatModel vipGlobalChatModel) {

    }

    @Override
    public void onMoreIconClicked(View clickedView, final VipGlobalChatModel vipGlobalChatModel) {
        DialogUtils.showPopUp(this, clickedView, new ItemClickListener<MenuItem>() {
            @Override
            public void onItemClick(MenuItem clickedItem) {
                switch (clickedItem.getItemId()) {
                    case 0:
                        AlertDialog.Builder builder = new AlertDialog.Builder(GlobalVipChat.this);
                        builder.setTitle(getString(R.string.areYouSure));
                        builder.setCancelable(false);
                        builder.setMessage(getString(R.string.deleteMessageWarning));
                        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        final AlertDialog alertDialog = builder.show();
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                                deleteMessage(vipGlobalChatModel);
                            }
                        });
                        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                            }
                        });
                        break;
                }
            }
        }, getString(R.string.delete));
    }


    private void showVipCoinsDialog(final UserModel userModel) {
        final Dialog dialog = new Dialog(GlobalVipChat.this);
        dialog.setContentView(R.layout.coins_chooser_dialog);
        final Button acceptCoins = (Button) dialog.findViewById(R.id.acceptCoins);
        final EditText coinsFiled = (EditText) dialog.findViewById(R.id.coinsFiled);
        acceptCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coinsFiled.getText().toString().trim().isEmpty()) {
                    coinsFiled.setError(getString(R.string.emptyField));
                } else {
                    coinsFiled.setError(null);
                    int inputtedCoins;
                    try {
                        inputtedCoins = Integer.valueOf(coinsFiled.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        inputtedCoins = 0;
                        e.printStackTrace();
                    }
                    if (inputtedCoins == 0) {
                        dialog.dismiss();
                        Snackbar.make(globalChatRecycler, getString(R.string.input_number), Snackbar.LENGTH_LONG).show();
                    } else if (Integer.valueOf(User.get().getCoins()) < inputtedCoins) {
                        dialog.dismiss();
                        Snackbar.make(globalChatRecycler, getString(R.string.not_enough_coins), Snackbar.LENGTH_LONG).show();
                    } else {
                        dialog.dismiss();
                        transferCoins(inputtedCoins, sharedHelper.getUserId(), userModel.getUserId());
                    }
                }

            }
        });
        dialog.show();
    }

    private void deleteMessage(final VipGlobalChatModel vipGlobalChatModel) {
        showVipLoading();
        makeRequest(Retrofit.getInstance().getInkService().vipGlobalChatAction(String.valueOf(vipGlobalChatModel.getMessageId()),
                null, null, VIP_GLOBAL_CHAT_TYPE_DELETE), null, false, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                hideVipLoading();
                vipGlobalChatAdapter.removeItem(vipGlobalChatModel);

                if (vipGlobalChatAdapter.isListEmpty()) {
                    showNoChat();
                } else {
                    hideNoChat();
                }
                Snackbar.make(globalChatRecycler, getString(R.string.messageDeleted), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
            }

            @Override
            public void onRequestFailed(Object[] result) {
                hideVipLoading();
            }
        });
    }

    @OnClick(R.id.sendGlobalMessage)
    public void sendClicked() {
        String message = globalChatField.getText().toString();
        sendMessage(message);
    }

    private void transferCoins(final int coinsAmount, final String transferrerId, final String receiverId) {
        transferDialog.show();
        makeRequest(Retrofit.getInstance().getInkService().transferCoins(transferrerId, receiverId, coinsAmount), null, false, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                try {
                    String responseBoy = ((ResponseBody) result).string();
                    JSONObject jsonObject = new JSONObject(responseBoy);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        transferDialog.hide();
                        int userCoinsLeft = jsonObject.optInt("userCoinsLeft");
                        User.get().setCoins(userCoinsLeft);
                        Toast.makeText(GlobalVipChat.this, getString(R.string.coins_transferred), Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(globalChatRecycler, getString(R.string.serverErrorText), Snackbar.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Snackbar.make(globalChatRecycler, getString(R.string.serverErrorText), Snackbar.LENGTH_LONG).show();
                    transferDialog.hide();
                    e.printStackTrace();
                } catch (JSONException e) {
                    Snackbar.make(globalChatRecycler, getString(R.string.serverErrorText), Snackbar.LENGTH_LONG).show();
                    transferDialog.hide();
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                transferDialog.hide();
            }
        });
    }

    private void sendMessage(String message) {
        Keyboard.hideKeyboard(this);
        changeMessageFieldsState(false);
        sendingProgress.setVisibility(View.VISIBLE);
        makeRequest(Retrofit.getInstance().getInkService().vipGlobalChatAction(null, sharedHelper.getUserId(), message, VIP_GLOBAL_CHAT_TYPE_SEND), null, false, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object result) {
                changeMessageFieldsState(true);
                sendingProgress.setVisibility(View.GONE);
                hideNoChat();
                if (result != null) {
                    globalChatField.setText("");
                    vipGlobalChatAdapter.insertItem(((VipGlobalChatResponseModel) result).getVipGlobalChatModels().get(0));
                    scrollToBottom();
                } else {
                    Snackbar.make(globalChatRecycler, getString(R.string.messageNotSent), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }
            }

            @Override
            public void onRequestFailed(Object[] result) {
                sendingProgress.setVisibility(View.GONE);
                changeMessageFieldsState(true);
            }
        });
    }

    @OnClick(R.id.refreshGlobalChat)
    public void refresh() {
        Keyboard.hideKeyboard(this);
        getMessages();
    }

    private void changeMessageFieldsState(boolean enable) {
        globalChatField.setEnabled(enable);
        sendGlobalMessage.setEnabled(enable);
    }

    @Override
    protected void onDestroy() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onSendCoinsClicked(@Nullable UserModel userModel) {
        showVipCoinsDialog(userModel);
    }

    @Override
    public void onSendMessageClicked(@Nullable UserModel userModel) {
        Intent intent = new Intent(getApplicationContext(), Chat.class);
        intent.putExtra("firstName", userModel.getFirstName());
        intent.putExtra("lastName", userModel.getLastName());
        intent.putExtra("opponentId", userModel.getUserId());
        intent.putExtra("isSocialAccount", userModel.isSocialAccount());
        intent.putExtra("opponentImage", userModel.getImageUrl());
        startActivity(intent);
        overrideActivityAnimation();
    }

}
