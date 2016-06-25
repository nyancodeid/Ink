package ink.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ink.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.adapters.ChatAdapter;
import ink.models.ChatModel;
import ink.utils.RecyclerTouchListener;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chat extends AppCompatActivity {

    @Bind(R.id.sendChatMessage)
    fab.FloatingActionButton mSendChatMessage;
    @Bind(R.id.writeEditText)
    EditText mWriteEditText;
    @Bind(R.id.noMessageLayout)
    NestedScrollView mNoMessageLayout;
    @Bind(R.id.chatRecyclerView)
    RecyclerView mRecyclerView;

    private String mOpponentId;
    String mCurrentUserId;
    private SharedHelper sharedHelper;

    private List<ChatModel> mChatModelArrayList = new ArrayList<>();
    private ChatAdapter mChatAdapter;
    private ChatModel mChatModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        sharedHelper = new SharedHelper(this);
        Bundle bundle = getIntent().getExtras();
        mChatAdapter = new ChatAdapter(mChatModelArrayList, this);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(itemAnimator);
        mRecyclerView.setAdapter(mChatAdapter);
        if (bundle != null) {
            String firstName = bundle.getString("firstName");
            mOpponentId = bundle.getString("opponentId");
            mCurrentUserId = sharedHelper.getUserId();
            getMessages();
            //action bar set ups.
            if (actionBar != null) {
                actionBar.setTitle(firstName);
            }
        }
        //action bar set ups
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                ChatModel chatModel = mChatModelArrayList.get(position);
                if (chatModel.isClickable()) {
                    Toast.makeText(Chat.this, chatModel.getMessageId(), Toast.LENGTH_SHORT).show();
                }
            }
        }));

        mSendChatMessage.setEnabled(false);
        mWriteEditText.addTextChangedListener(chatTextWatcher);

    }


    @OnClick(R.id.sendChatMessage)
    public void sendChatMessage() {

        if (mNoMessageLayout.getVisibility() == View.VISIBLE) {
            mNoMessageLayout.setVisibility(View.GONE);
        }
        Call<ResponseBody> sendMessageResponse = Retrofit.getInstance().getInkService().sendMessage(mCurrentUserId, mOpponentId, mWriteEditText.getText().toString());
        ChatModel tempChat = new ChatModel(null, mCurrentUserId, mOpponentId, mWriteEditText.getText().toString(), false);
        mChatModelArrayList.add(tempChat);
        final int lastIndex = mChatModelArrayList.indexOf(tempChat);
        mChatAdapter.notifyDataSetChanged();
        mWriteEditText.setText("");


        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                mRecyclerView.smoothScrollToPosition(mChatAdapter.getItemCount());
            }
        });

        sendMessageResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        String messageId = jsonObject.optString("message_id");
                        mChatModelArrayList.get(lastIndex).setMessageId(messageId);
                        mChatModelArrayList.get(lastIndex).setClickable(true);
                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void getMessages() {
        Call<ResponseBody> messagesResponse = Retrofit.getInstance().getInkService().getMessages(mCurrentUserId, mOpponentId);
        messagesResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        JSONArray messagesArray = jsonObject.optJSONArray("messages");
                        if (messagesArray.length() <= 0) {
                            mNoMessageLayout.setVisibility(View.VISIBLE);
                        } else {
                            for (int i = 0; i < messagesArray.length(); i++) {
                                JSONObject eachObject = messagesArray.optJSONObject(i);
                                String messageId = eachObject.optString("message_id");
                                String opponentId = eachObject.optString("opponent_id");
                                String message = eachObject.optString("message");
                                String userId = eachObject.optString("user_id");
                                mChatModel = new ChatModel(messageId, userId, opponentId, message, true);
                                mChatModelArrayList.add(mChatModel);
                                mChatAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
                        builder.setTitle(getString(R.string.serverErrorTitle));
                        builder.setMessage(getString(R.string.serverErrorText));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                        builder.show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private TextWatcher chatTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() <= 0) {
                mSendChatMessage.setEnabled(false);
            } else {
                mSendChatMessage.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
