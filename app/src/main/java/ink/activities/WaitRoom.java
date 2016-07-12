package ink.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.ink.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.callbacks.GeneralCallback;
import ink.service.RemoveChatRouletteService;
import ink.utils.Constants;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WaitRoom extends AppCompatActivity {

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
    private ShowcaseView.Builder showcaseViewBuilder;
    private SharedHelper sharedHelper;
    private boolean isConnectedToWaitRoom;
    private boolean shouldWaitForWaiters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_room);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
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

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        startService(new Intent(getApplicationContext(), RemoveChatRouletteService.class));
        super.onDestroy();
    }

    @OnClick(R.id.connectDisconnectButton)
    public void connectDisconnectButton() {
        if (!isConnectedToWaitRoom) {
            if (actualStatus != null) {
                Snackbar.make(actualStatus, getString(R.string.notConnectedToWaitRoom), Snackbar.LENGTH_LONG).show();
            }

            return;
        }

        if (connectDisconnectButton.getTag().equals(getString(R.string.connect))) {
            connectDisconnectButton.setTag(getString(R.string.disconnect));
            connectDisconnectButton.setImageResource(R.drawable.disconnect_icon);
            LocalBroadcastManager.getInstance(this).registerReceiver(messagesReceiver, new IntentFilter(getPackageName() + "WaitRoom"));
            shouldWaitForWaiters = true;
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
            waitersQueAction(Constants.ACTION_UPDATE, Constants.STATUS_WAITING_NOT_AVAILABLE, new GeneralCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    actualStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                    actualStatus.setText(getString(R.string.notConnectedToOpponent));
                }

                @Override
                public void onFailure(String s) {
                }
            });
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messagesReceiver);
        }
    }

    private BroadcastReceiver messagesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };


    private void waitersQueAction(final String action, final String status, final GeneralCallback<String> callback) {
        Call<ResponseBody> waitersQueActionCall = Retrofit.getInstance().getInkService().waitersQueAction(sharedHelper.getUserId(),
                sharedHelper.getFirstName() + " " + sharedHelper.getLastName(), status, action);
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
                        Log.d("fasfasfasfas", "onResponse: " + responseBody);
                        JSONObject jsonObject = new JSONObject(responseBody);
                        boolean success = jsonObject.optBoolean("success");
                        if (success) {
                            boolean isMemberAvailable = jsonObject.optBoolean("isMemberAvailable");
                            if (!isMemberAvailable) {
                                if (shouldWaitForWaiters) {
                                    scheduleTask();
                                }
                            } else {
                                // TODO: 2016-07-13 grab the members
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

    private void scheduleTask() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                getWaiters();
            }
        };
        executor.schedule(task, 5, TimeUnit.SECONDS);
    }
}
