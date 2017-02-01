package ink.va.activities;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.adapters.MessagesAdapter;
import ink.va.decorators.DividerItemDecoration;
import ink.va.interfaces.MyMessagesItemClickListener;
import ink.va.models.MyMessagesModel;
import ink.va.models.UserMessagesModel;
import ink.va.utils.RealmHelper;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Messages extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, MyMessagesItemClickListener {

    private SharedHelper mSharedHelper;

    @Bind(R.id.messagesRecyclerView)
    RecyclerView mRecyclerView;
    @Bind(R.id.messagesSwipe)
    SwipeRefreshLayout mMessagesSwipe;
    @Bind(R.id.noMessageLayout)
    View mNoMessageLayout;
    @Bind(R.id.messagesRootLayout)
    RelativeLayout messagesRootLayout;
    private UserMessagesModel userMessagesModel;
    private MessagesAdapter messagesAdapter;
    private String finalOpponentId;
    private Snackbar deleteRequestSnack;
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
    }

    private void makeDeleteRequest(final String opponentId) {
        Call<ResponseBody> deleteRequest = Retrofit.getInstance().getInkService().requestDelete(mSharedHelper.getUserId(), opponentId);
        deleteRequest.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    makeDeleteRequest(opponentId);
                    return;
                }
                if (response.body() == null) {
                    makeDeleteRequest(opponentId);
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        RealmHelper.getInstance().removeMessage(opponentId, mSharedHelper.getUserId());
                        getUserMessages();
                    } else {
                        hideSnack(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                makeDeleteRequest(opponentId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserMessages();
    }

    private void getUserMessages() {

        Call<MyMessagesModel> myMessagesCall = Retrofit.getInstance()
                .getInkService().getMyMessages(mSharedHelper.getUserId());
        myMessagesCall.enqueue(new Callback<MyMessagesModel>() {
            @Override
            public void onResponse(Call<MyMessagesModel> call, Response<MyMessagesModel> response) {

                boolean success = response.body().isSuccess();

                if (success) {
                    hideSnack(true);
                    if (mMessagesSwipe != null && mMessagesSwipe.isRefreshing()) {
                        mMessagesSwipe.setRefreshing(false);
                    }

                    List<UserMessagesModel> userMessagesModels = response.body().getUserMessagesModels();
                    messagesAdapter.clear();
                    if (userMessagesModels.isEmpty()) {
                        mNoMessageLayout.setVisibility(View.VISIBLE);
                    } else {
                        messagesAdapter.setUserMessagesModels(userMessagesModels);
                        mNoMessageLayout.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(Messages.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(Call<MyMessagesModel> call, Throwable t) {
                Toast.makeText(Messages.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRefresh() {
        getUserMessages();
    }

    private void showSnack() {
        deleteRequestSnack = Snackbar.make(mRecyclerView, getString(R.string.deleteingMessage), Snackbar.LENGTH_INDEFINITE);
        deleteRequestSnack.show();
    }

    private void hideSnack(boolean success) {
        if (deleteRequestSnack != null) {
            if (deleteRequestSnack.isShown()) {
                if (success) {
                    deleteRequestSnack.setText(getString(R.string.messageDeleted));
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(Integer.valueOf(finalOpponentId));
                    mSharedHelper.removeLastNotificationId(finalOpponentId);
                } else {
                    deleteRequestSnack.setText(getString(R.string.somethingWentWrong));
                }
                deleteRequestSnack.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteRequestSnack.dismiss();
                    }
                });
            }
        }
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

        RealmHelper.getInstance().removeMessageCount(Integer.valueOf(finalId));

        Intent intent = new Intent(getApplicationContext(), Chat.class);
        intent.putExtra("firstName", userMessagesModel.getFirstName());
        intent.putExtra("lastName", userMessagesModel.getLastName());
        intent.putExtra("opponentId", finalId);
        intent.putExtra("isSocialAccount", userMessagesModel.isSocialAccount());
        intent.putExtra("opponentImage", userMessagesModel.getImageName());
        startActivity(intent);
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
                        showSnack();
                        makeDeleteRequest(finalOpponentId);
                    }
                });
                yesNoDialog.show();

            }
        });
        builder.show();
    }
}
