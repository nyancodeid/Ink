package ink.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.ink.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.adapters.MessagesAdapter;
import ink.decorators.DividerItemDecoration;
import ink.models.UserMessagesModel;
import ink.utils.RealmHelper;
import ink.utils.RecyclerTouchListener;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Messages extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SharedHelper mSharedHelper;

    @Bind(R.id.messagesRecyclerView)
    RecyclerView mRecyclerView;
    @Bind(R.id.messagesLoadingProgress)
    AVLoadingIndicatorView mMessagesLoadingProgress;
    @Bind(R.id.messagesSwipe)
    SwipeRefreshLayout mMessagesSwipe;
    @Bind(R.id.noMessageLayout)
    NestedScrollView mNoMessageLayout;
    private List<UserMessagesModel> userMessagesModels;
    private UserMessagesModel userMessagesModel;
    private MessagesAdapter messagesAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
    private String finalOpponentId;
    private Snackbar deleteRequestSnack;
    private boolean isOnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        ButterKnife.bind(this);
        isOnCreate = true;
        mMessagesSwipe.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        userMessagesModels = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(userMessagesModels, this);
        messagesAdapter.setShouldStartAnimation(true);
        mMessagesSwipe.setOnRefreshListener(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.myMessages));
        }

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(this));


        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String finalId;
                if (userMessagesModels.get(position).getUserId().equals(mSharedHelper.getUserId())) {
                    finalId = userMessagesModels.get(position).getOpponentId();
                } else {
                    finalId = userMessagesModels.get(position).getUserId();
                }
                Intent intent = new Intent(getApplicationContext(), Chat.class);
                intent.putExtra("firstName", userMessagesModels.get(position).getFirstName());
                intent.putExtra("opponentId", finalId);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, final int position) {
                System.gc();
                if (userMessagesModels.get(position).getUserId().equals(mSharedHelper.getUserId())) {
                    finalOpponentId = userMessagesModels.get(position).getOpponentId();
                } else {
                    finalOpponentId = userMessagesModels.get(position).getUserId();
                }
                final AlertDialog.Builder builder = new AlertDialog.Builder(Messages.this);
                builder.setTitle(getString(R.string.chooseAction));
                builder.setMessage(getString(R.string.profileText) + " : " +
                        userMessagesModels.get(position).getFirstName() + " "
                        + userMessagesModels.get(position).getLastName());


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
                        intent.putExtra("firstName", userMessagesModels.get(position).getFirstName());
                        intent.putExtra("lastName", userMessagesModels.get(position).getLastName());
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(getString(R.string.deleteMessage), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                        System.gc();
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
        }));
        mRecyclerView.setAdapter(messagesAdapter);
        mSharedHelper = new SharedHelper(this);
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

    private void getUserMessages() {
        Call<ResponseBody> myMessagesCall = Retrofit.getInstance()
                .getInkService().getMyMessages(mSharedHelper.getUserId());
        myMessagesCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response == null) {
                        getUserMessages();
                    }
                    if (response.body() == null) {
                        getUserMessages();
                    }
                    System.gc();
                    if (userMessagesModels != null) {
                        userMessagesModels.clear();
                    }
                    messagesAdapter.notifyDataSetChanged();
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    JSONArray messages = jsonObject.optJSONArray("messages");

                    String currentUserId = mSharedHelper.getUserId();
                    for (int i = 0; i < messages.length(); i++) {
                        JSONObject eachObject = messages.optJSONObject(i);
                        String userId = eachObject.optString("user_id");
                        String opponentId = eachObject.optString("opponent_id");
                        String messageId = eachObject.optString("message_id");
                        String message = StringEscapeUtils.unescapeJava(eachObject.optString("message"));
                        String firstName = eachObject.optString("firstName");
                        String lastName = eachObject.optString("lastName");
                        String imageName = eachObject.optString("imageName");
                        String date = eachObject.optString("date");
                        String deleteUserId = eachObject.optString("delete_user_id");
                        String deleteOpponentId = eachObject.optString("delete_opponent_id");

                        if (currentUserId.equals(deleteUserId) || currentUserId.equals(deleteOpponentId)) {
                            continue;
                        }
                        String finalDate;
                        try {
                            Date dateObject = dateFormat.parse(date);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(dateObject);

                            int monthInt = calendar.get(Calendar.MONTH);
                            int dayInt = calendar.get(Calendar.DAY_OF_MONTH) + 1;
                            int hourInt = calendar.get(Calendar.HOUR_OF_DAY);
                            int minutesInt = calendar.get(Calendar.MINUTE);
                            int secondsInt = calendar.get(Calendar.SECOND);
                            String month = String.valueOf(monthInt);
                            String day = String.valueOf(dayInt);
                            String hours = String.valueOf(hourInt);
                            String minutes = String.valueOf(minutesInt);
                            String seconds = String.valueOf(secondsInt);

                            if (monthInt < 10) {
                                month = "0" + monthInt;
                            }
                            if (dayInt < 10) {
                                day = "0" + dayInt;
                            }
                            if (hourInt < 10) {
                                hours = "0" + hourInt;
                            }
                            if (minutesInt < 10) {
                                minutes = "0" + minutesInt;
                            }
                            if (secondsInt < 10) {
                                seconds = "0" + secondsInt;
                            }


                            String year = String.valueOf(calendar.get(Calendar.YEAR));

                            finalDate = year + "/" + month + "/" + day + "\n" + hours + ":" + minutes + ":" + seconds;
                        } catch (ParseException e) {
                            finalDate = date;
                            e.printStackTrace();
                        }

                        userMessagesModel = new UserMessagesModel(userId, opponentId, messageId, message,
                                firstName, lastName, imageName, finalDate, imageName);
                        userMessagesModels.add(userMessagesModel);
                        messagesAdapter.notifyDataSetChanged();
                    }

                    if (mMessagesSwipe != null) {
                        mMessagesSwipe.setRefreshing(false);
                    }
                    mMessagesLoadingProgress.setVisibility(View.GONE);
                    if (userMessagesModels.size() <= 0) {
                        mNoMessageLayout.setVisibility(View.VISIBLE);
                    }
                    hideSnack(true);
                } catch (IOException e) {
                    hideSnack(true);
                    e.printStackTrace();
                } catch (JSONException e) {
                    hideSnack(true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getUserMessages();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        if (isOnCreate) {
            messagesAdapter.setShouldStartAnimation(false);
            isOnCreate = false;
        }
        getUserMessages();
        super.onResume();
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

}
