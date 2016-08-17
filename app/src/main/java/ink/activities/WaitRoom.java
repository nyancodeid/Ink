package ink.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.ink.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.adapters.ChatAdapter;
import ink.callbacks.GeneralCallback;
import ink.models.ChatModel;
import ink.service.ChatRouletteDestroyService;
import ink.service.RemoveChatRouletteService;
import ink.utils.Constants;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WaitRoom extends BaseActivity {

    private static final String TAG = WaitRoom.class.getSimpleName();
    @Bind(R.id.chatRouletteRecycler)
    RecyclerView chatRouletteRecycler;
    @Bind(R.id.chatRouletteMessageBody)
    EditText chatRouletteMessageBody;
    @Bind(R.id.chatRouletteSendMessage)
    FloatingActionButton chatRouletteSendMessage;
    @Bind(R.id.connectDisconnectButton)
    FloatingActionButton connectDisconnectButton;
    @Bind(R.id.actualStatus)
    TextView actualStatus;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    private ShowcaseView.Builder showcaseViewBuilder;
    private SharedHelper sharedHelper;
    private boolean isConnectedToWaitRoom;
    private boolean shouldWaitForWaiters;
    private String foundOpponentId;
    private ChatAdapter chatAdapter;
    private ChatModel chatModel;
    private List<ChatModel> chatModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_room);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        chatModels = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatModels, this);


        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chatRouletteRecycler.setLayoutManager(linearLayoutManager);
        chatRouletteRecycler.setItemAnimator(itemAnimator);
        chatRouletteRecycler.setAdapter(chatAdapter);

        chatRouletteRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(chatRouletteRecycler.getWindowToken(), 0);
                }
            }
        });


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.waitRoom));
        }
        chatRouletteSendMessage.setEnabled(false);
        chatRouletteMessageBody.setEnabled(false);

        if (sharedHelper.shouldShowShowCase()) {
            showTutorial();
        } else {
            waitersQueAction(Constants.ACTION_INSERT, Constants.STATUS_WAITING_NOT_AVAILABLE, new GeneralCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    isConnectedToWaitRoom = true;
                    actualStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                    actualStatus.setText(getString(R.string.connectedToQue));
                }

                @Override
                public void onFailure(String s) {

                }
            });
        }


        chatRouletteMessageBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    chatRouletteSendMessage.setEnabled(false);
                } else {
                    chatRouletteSendMessage.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void showTutorial() {
        showcaseViewBuilder = new ShowcaseView.Builder(this);
        showcaseViewBuilder.withMaterialShowcase();
        showcaseViewBuilder.setTarget(new ViewTarget(actualStatus));
        showcaseViewBuilder.setContentTitle(getString(R.string.statustChanageTitle));
        showcaseViewBuilder.setContentText(getString(R.string.statusBrief));
        showcaseViewBuilder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showcaseViewBuilder.setTarget(new ViewTarget(connectDisconnectButton));
                showcaseViewBuilder.setContentTitle(getString(R.string.connectDisconnectTitle));
                showcaseViewBuilder.setContentText(getString(R.string.connectDisconnectBrief));
                showcaseViewBuilder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showcaseViewBuilder.setTarget(new ViewTarget(chatRouletteMessageBody));
                        showcaseViewBuilder.setContentTitle(getString(R.string.messageBodyTitle));
                        showcaseViewBuilder.setContentText(getString(R.string.messageBodyBrief));
                        showcaseViewBuilder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showcaseViewBuilder.setTarget(new ViewTarget(chatRouletteSendMessage));
                                showcaseViewBuilder.setContentTitle(getString(R.string.sendMessageTitle));
                                showcaseViewBuilder.setContentText(getString(R.string.sendMessageBrief));
                                showcaseViewBuilder.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showcaseViewBuilder.setTarget(new ViewTarget(chatRouletteRecycler));
                                        showcaseViewBuilder.setContentTitle(getString(R.string.messagesPlaceTitle));
                                        showcaseViewBuilder.setContentText(getString(R.string.messagesPlaceBrief));
                                        showcaseViewBuilder.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                showcaseViewBuilder.setTarget(new ViewTarget(chatRouletteRecycler));
                                                showcaseViewBuilder.setContentTitle(getString(R.string.endingTitle));
                                                showcaseViewBuilder.setContentText(getString(R.string.endingBrief));
                                                showcaseViewBuilder.setOnClickListener(null);
                                                sharedHelper.setShouldShowShowCase(false);
                                                waitersQueAction(Constants.ACTION_INSERT, Constants.STATUS_WAITING_NOT_AVAILABLE, new GeneralCallback<String>() {
                                                    @Override
                                                    public void onSuccess(String s) {
                                                        isConnectedToWaitRoom = true;
                                                    }

                                                    @Override
                                                    public void onFailure(String s) {

                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        showcaseViewBuilder.build();
    }


    @OnClick(R.id.chatRouletteSendMessage)
    public void chatRouletteSendMessage() {
        sendMessage();
    }

    private void sendMessage() {
        String message = chatRouletteMessageBody.getText().toString().trim();
        ChatModel tempChat = new ChatModel(false, null, null, sharedHelper.getUserId(), foundOpponentId, message, true, Constants.STATUS_NOT_DELIVERED, null,
                null, null);
        chatModels.add(tempChat);
        chatAdapter.notifyDataSetChanged();
        final int itemLocation = chatModels.indexOf(tempChat);
        chatRouletteMessageBody.setText("");
        scrollToBottom();
        Call<ResponseBody> chatRouletteSendMessageCall = Retrofit.getInstance().getInkService().sendChatRouletteMessage(
                sharedHelper.getUserId(), foundOpponentId, message);
        chatRouletteSendMessageCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    sendMessage();
                    return;
                }
                if (response.body() == null) {
                    sendMessage();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        chatModels.get(itemLocation).setDeliveryStatus(Constants.STATUS_DELIVERED);
                        chatAdapter.notifyItemChanged(itemLocation);
                    } else {
                        Snackbar.make(connectDisconnectButton, getString(R.string.messageNotSent),
                                Snackbar.LENGTH_LONG).show();
                    }
                    scrollToBottom();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                sendMessage();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.connectDisconnectButton)
    public void connectDisconnectButton() {
        if (chatModels != null) {
            chatModels.clear();
        }
        if (!isConnectedToWaitRoom) {
            if (actualStatus != null) {
                Snackbar.make(actualStatus, getString(R.string.notConnectedToWaitRoom), Snackbar.LENGTH_LONG).show();
            }

            return;
        }

        if (connectDisconnectButton.getTag().equals(getString(R.string.connect))) {
            connectDisconnectButton.setTag(getString(R.string.disconnect));
            connectDisconnectButton.setImageResource(R.drawable.disconnect_icon);
            LocalBroadcastManager.getInstance(this).registerReceiver(messagesReceiver,
                    new IntentFilter(getPackageName() + "WaitRoom"));
            shouldWaitForWaiters = true;
            progressBar.setVisibility(View.VISIBLE);
            getWaiters();
            waitersQueAction(Constants.ACTION_UPDATE, Constants.STATUS_AVAILABLE, new GeneralCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    actualStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                    actualStatus.setText(getString(R.string.waitingForOpponents));
                }

                @Override
                public void onFailure(String s) {

                }
            });
        } else {
            connectDisconnectButton.setTag(getString(R.string.connect));
            connectDisconnectButton.setImageResource(R.drawable.connect_icon);
            shouldWaitForWaiters = false;
            chatRouletteMessageBody.setEnabled(false);
            if (chatModels != null) {
                chatModels.clear();
                chatAdapter.notifyDataSetChanged();
                scrollToBottom();
            }

            progressBar.setVisibility(View.VISIBLE);
            waitersQueAction(Constants.ACTION_UPDATE, Constants.STATUS_WAITING_NOT_AVAILABLE, new GeneralCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    actualStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                    actualStatus.setText(getString(R.string.notConnectedToOpponent));
                    progressBar.setVisibility(View.GONE);
                    if (foundOpponentId != null) {
                        sendDisconnect(foundOpponentId);
                        foundOpponentId = null;
                    }
                }

                @Override
                public void onFailure(String s) {
                }
            });
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messagesReceiver);
        }
    }

    private void sendDisconnect(final String foundOpponentId) {
        Call<ResponseBody> disconnectCall = Retrofit.getInstance().getInkService().sendDisconnectNotification(foundOpponentId);
        disconnectCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    sendDisconnect(foundOpponentId);
                    return;
                }
                if (response.body() == null) {
                    sendDisconnect(foundOpponentId);
                    return;
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                sendDisconnect(foundOpponentId);
            }
        });
    }

    private BroadcastReceiver messagesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String isDisconnected = extras.getString("isDisconnected");
                if (isDisconnected.equals("1")) {
                    actualStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                    actualStatus.setText(getString(R.string.opponentFound));
                    progressBar.setVisibility(View.GONE);
                    chatRouletteMessageBody.setEnabled(true);
                    shouldWaitForWaiters = false;

                    String currentUserId = extras.getString("currentUserId");
                    String opponentId = extras.getString("opponentId");
                    String message = extras.getString("message");
                    chatModel = new ChatModel(false, null, null, currentUserId, opponentId, message, true, Constants.STATUS_DELIVERED, null,
                            null, null);
                    chatModels.add(chatModel);
                    chatAdapter.notifyDataSetChanged();
                    scrollToBottom();
                } else {
                    disconnectFromOpponent();
                }
            }
        }
    };

    private void disconnectFromOpponent() {
        progressBar.setVisibility(View.GONE);
        shouldWaitForWaiters = false;
        connectDisconnectButton.setTag(getString(R.string.connect));
        connectDisconnectButton.setImageResource(R.drawable.connect_icon);
        chatRouletteMessageBody.setEnabled(false);
        waitersQueAction(Constants.ACTION_UPDATE, Constants.STATUS_WAITING_NOT_AVAILABLE, new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                shouldWaitForWaiters = false;
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(WaitRoom.this);
                bottomSheetDialog.setTitle(getString(R.string.userDisconnected));
                LayoutInflater inflater = getLayoutInflater();
                View bottomSheetView = inflater.inflate(R.layout.disconnect_bottom_view, null);
                bottomSheetDialog.setContentView(bottomSheetView);
                Button closeBottomSheet = (Button) bottomSheetView.findViewById(R.id.closeBottomSheet);
                closeBottomSheet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LocalBroadcastManager.getInstance(WaitRoom.this).unregisterReceiver(messagesReceiver);
                        bottomSheetDialog.dismiss();
                    }
                });
                bottomSheetDialog.show();
                actualStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                actualStatus.setText(getString(R.string.notConnectedToOpponent));
            }

            @Override
            public void onFailure(String s) {
            }
        });
    }


    private void waitersQueAction(final String action, final String status, final GeneralCallback<String> callback) {
        if (foundOpponentId == null) {
            foundOpponentId = "0";
        }
        Call<ResponseBody> waitersQueActionCall = Retrofit.getInstance().getInkService().waitersQueAction(sharedHelper.getUserId(),
                sharedHelper.getFirstName() + " " + sharedHelper.getLastName(), status, action, foundOpponentId);
        waitersQueActionCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    waitersQueAction(action, status, callback);
                    return;
                }
                if (response.body() == null) {
                    waitersQueAction(action, status, callback);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        if (callback != null) {
                            callback.onSuccess("success");
                        }
                    } else {
                        waitersQueAction(action, status, callback);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                waitersQueAction(action, status, callback);
            }
        });
    }

    private void getWaiters() {
        progressBar.setVisibility(View.VISIBLE);
        chatRouletteMessageBody.setEnabled(false);
        Call<ResponseBody> getWaitersCall = Retrofit.getInstance().getInkService().getWaiters(sharedHelper.getUserId());
        if (shouldWaitForWaiters) {
            getWaitersCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response == null) {
                        getWaiters();
                        return;
                    }
                    if (response.body() == null) {
                        getWaiters();
                        return;
                    }
                    try {
                        String responseBody = response.body().string();
                        final JSONObject jsonObject = new JSONObject(responseBody);
                        boolean success = jsonObject.optBoolean("success");
                        if (success) {
                            boolean isMemberAvailable = jsonObject.optBoolean("isMemberAvailable");
                            if (!isMemberAvailable) {
                                if (shouldWaitForWaiters) {
                                    scheduleTask();
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                }
                            } else {
                                String opponentId = jsonObject.optString("waiter_id");
                                handleOpponentFound(opponentId);
                            }
                        } else {
                            getWaiters();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    getWaiters();
                }
            });
        }
    }

    private void handleOpponentFound(final String opponentId) {
        foundOpponentId = opponentId;
        Intent intent = new Intent(getApplicationContext(), ChatRouletteDestroyService.class);
        intent.putExtra("opponentId", opponentId);
        startService(intent);

        waitersQueAction(Constants.ACTION_UPDATE, Constants.STATUS_IN_CHAT, new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                Snackbar.make(connectDisconnectButton, getString(R.string.opponentFound),
                        Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
                actualStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                actualStatus.setText(getString(R.string.opponentFound));
                progressBar.setVisibility(View.GONE);
                chatRouletteMessageBody.setEnabled(true);
                shouldWaitForWaiters = false;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    private void scheduleTask() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getWaiters();
            }
        }, 2000);
    }

    private void scrollToBottom() {
        chatRouletteRecycler.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                chatRouletteRecycler.smoothScrollToPosition(chatAdapter.getItemCount());
            }
        });
    }


    @Override
    protected void onDestroy() {
        Intent intent = new Intent(getApplicationContext(), RemoveChatRouletteService.class);
        intent.putExtra("opponentId", foundOpponentId);
        if (foundOpponentId != null) {
            foundOpponentId = null;
        }
        startService(intent);
        if (messagesReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messagesReceiver);
        }
        super.onDestroy();
    }
}
