package kashmirr.social.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kashmirr.social.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kashmirr.social.adapters.MessagesAdapter;
import kashmirr.social.callbacks.GeneralCallback;
import kashmirr.social.decorators.DividerItemDecoration;
import kashmirr.social.interfaces.MyMessagesItemClickListener;
import kashmirr.social.models.UserMessagesModel;
import kashmirr.social.utils.RealmHelper;
import kashmirr.social.utils.SharedHelper;

import static kashmirr.social.activities.Chat.UPDATE_USER_MESSAGES;

public class Messages extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, MyMessagesItemClickListener {

    private SharedHelper mSharedHelper;

    @BindView(R.id.messagesRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.messagesSwipe)
    SwipeRefreshLayout mMessagesSwipe;
    @BindView(R.id.noMessageLayout)
    View mNoMessageLayout;
    @BindView(R.id.messagesRootLayout)
    RelativeLayout messagesRootLayout;
    private MessagesAdapter messagesAdapter;
    private String finalOpponentId;
    private SharedHelper sharedHelper;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        sharedHelper = new SharedHelper(this);
        ButterKnife.bind(this);
        gson = new Gson();
        mMessagesSwipe.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        messagesAdapter = new MessagesAdapter(this);
        mMessagesSwipe.setOnRefreshListener(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.myMessages));
        }
        if (sharedHelper.getMessagesColor() != null) {
            messagesRootLayout.setBackgroundColor(Color.parseColor(sharedHelper.getMessagesColor()));
        }
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(this));


        mRecyclerView.setAdapter(messagesAdapter);
        messagesAdapter.setOnItemClickListener(this);
        mSharedHelper = new SharedHelper(this);
        mMessagesSwipe.post(new Runnable() {
            @Override
            public void run() {
                mMessagesSwipe.setRefreshing(true);
            }
        });
        getUserMessages();

        LocalBroadcastManager.getInstance(this).registerReceiver(messageUpdateReceiver, new IntentFilter(getPackageName() + "Messages"));
    }

    private void deleteMessage(final String opponentId) {
        RealmHelper.getInstance().deleteMessageRow(sharedHelper.getUserId(), opponentId, new GeneralCallback() {
            @Override
            public void onSuccess(Object o) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getUserMessages();
                    }
                });
            }

            @Override
            public void onFailure(Object o) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getUserMessages() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                RealmHelper.getInstance().getUserMessages(sharedHelper.getUserId(), new GeneralCallback<List<UserMessagesModel>>() {
                    @Override
                    public void onSuccess(final List<UserMessagesModel> userMessagesModels) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMessagesSwipe.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMessagesSwipe.setRefreshing(false);
                                    }
                                });
                                if (userMessagesModels.isEmpty()) {
                                    messagesAdapter.clear();
                                    mNoMessageLayout.setVisibility(View.VISIBLE);
                                } else {
                                    messagesAdapter.setUserMessagesModels(userMessagesModels);
                                    mNoMessageLayout.setVisibility(View.GONE);
                                }
                            }
                        });

                    }

                    @Override
                    public void onFailure(List<UserMessagesModel> userMessagesModels) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMessagesSwipe.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mMessagesSwipe.setRefreshing(false);
                                    }
                                });
                                Toast.makeText(Messages.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageUpdateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        getUserMessages();
    }


    @Override
    public void onItemClick(Object clickedItem) {
        String finalId;
        UserMessagesModel userMessagesModel = (UserMessagesModel) clickedItem;

        if (userMessagesModel.getUserId().equals(mSharedHelper.getUserId())) {
            finalId = userMessagesModel.getOpponentId();
        } else {
            finalId = userMessagesModel.getUserId();
        }

        userMessagesModel.setHasRead(true);
        int index = messagesAdapter.getUserMessagesModels().indexOf(userMessagesModel);
        RealmHelper.getInstance().updateMessageReadStatus(userMessagesModel.getMessageId(), true, null);
        messagesAdapter.notifyItemChanged(index);

        RealmHelper.getInstance().removeNotificationCount(this, Integer.valueOf(finalId));

        Intent intent = new Intent(getApplicationContext(), Chat.class);
        intent.putExtra("firstName", userMessagesModel.getFirstName());
        intent.putExtra("lastName", userMessagesModel.getLastName());
        intent.putExtra("opponentId", finalId);
        intent.putExtra("isSocialAccount", userMessagesModel.isSocialAccount());
        intent.putExtra("opponentImage", userMessagesModel.getImageName());
        startActivityForResult(intent, UPDATE_USER_MESSAGES);
    }

    @Override
    public void onItemLongClick(Object clickedItem) {
        final UserMessagesModel userMessagesModel = (UserMessagesModel) clickedItem;

        if (userMessagesModel.getUserId().equals(mSharedHelper.getUserId())) {
            finalOpponentId = userMessagesModel.getOpponentId();
        } else {
            finalOpponentId = userMessagesModel.getUserId();
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(Messages.this);
        builder.setTitle(getString(R.string.chooseAction));
        builder.setMessage(getString(R.string.profileText) + " : " +
                userMessagesModel.getFirstName() + " "
                + userMessagesModel.getLastName());


        builder.setNegativeButton(getString(R.string.dismiss), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.viewProfile), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(), OpponentProfile.class);
                intent.putExtra("id", finalOpponentId);
                intent.putExtra("firstName", userMessagesModel.getFirstName());
                intent.putExtra("lastName", userMessagesModel.getLastName());
                intent.putExtra("isFriend", userMessagesModel.isFriend());
                startActivity(intent);
            }
        });
        builder.setNegativeButton(getString(R.string.deleteMessage), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
                AlertDialog.Builder yesNoDialog = new AlertDialog.Builder(Messages.this);
                yesNoDialog.setTitle(getString(R.string.areYouSure));
                yesNoDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                yesNoDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(finalOpponentId);
                    }
                });
                yesNoDialog.show();

            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UPDATE_USER_MESSAGES:
                getUserMessages();
                break;
        }
    }

    private BroadcastReceiver messageUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getUserMessages();
        }
    };
}
