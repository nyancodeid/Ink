package ink.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.adapters.GroupMessagesAdapter;
import ink.models.GroupMessagesModel;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.RecyclerTouchListener;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SingleGroupView extends AppCompatActivity {

    @Bind(R.id.groupCollapsingToolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.groupToolbar)
    Toolbar mToolbar;
    @Bind(R.id.groupImage)
    ImageView mGroupImageView;
    @Bind(R.id.groupBackgroundColor)
    RelativeLayout mGroupBackgroundColor;
    @Bind(R.id.joinGroupButton)
    Button mJoinGroupButton;
    @Bind(R.id.groupSingleDescription)
    TextView mGroupSingleDescription;
    @Bind(R.id.groupSingleFollowersCount)
    TextView mGroupSingleFollowersCount;
    @Bind(R.id.ownerImageView)
    ImageView mOwnerImageView;
    @Bind(R.id.addMessageToGroup)
    FloatingActionButton mAddMessageToGroup;
    @Bind(R.id.groupMessagesRecycler)
    RecyclerView groupMessagesRecycler;
    @Bind(R.id.groupMessagesLoading)
    AVLoadingIndicatorView groupMessagesLoading;
    @Bind(R.id.noGroupMessageLayout)
    RelativeLayout noGroupMessageLayout;

    private String mGroupName = "";
    private BroadcastReceiver broadcastReceiver;
    private boolean isFollowing;
    private String mOwnerImage;
    private String mGroupId;
    private String mGroupColor;
    private String mGroupImage;
    private String mGroupDescription;
    private String mGroupOwnerId;
    private String mGroupOwnerName;
    private String mCount;
    private List<GroupMessagesModel> groupMessagesModels;
    private GroupMessagesModel groupMessagesModel;
    private GroupMessagesAdapter groupMessagesAdapter;
    private SharedHelper mSharedHelper;
    private boolean isMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_group_view);
        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);
        Bundle extras = getIntent().getExtras();
        groupMessagesModels = new ArrayList<>();
        groupMessagesAdapter = new GroupMessagesAdapter(groupMessagesModels, this);
        mJoinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestJoin();
            }
        });
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Snackbar.make(mGroupImageView, getString(R.string.messagePosted), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
                getGroupMessages();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + "SingleGroupView"));

        groupMessagesRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    mAddMessageToGroup.hide(true);
                else if (dy < 0)
                    mAddMessageToGroup.show(true);
            }
        });

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        groupMessagesRecycler.setLayoutManager(new LinearLayoutManager(this));
        groupMessagesRecycler.setItemAnimator(itemAnimator);


        groupMessagesRecycler.addOnItemTouchListener(new RecyclerTouchListener(this, groupMessagesRecycler, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                GroupMessagesModel singleModel = groupMessagesModels.get(position);
                if (singleModel.getSenderId().equals(mSharedHelper.getUserId())) {
                    Intent intent = new Intent(getApplicationContext(), MyProfile.class);
                    startActivity(intent);
                } else {
                    String name = singleModel.getSenderName();
                    String[] splited = name.split("\\s+");
                    String firstName = splited[0];
                    String lastName = splited[1];
                    Intent intent = new Intent(getApplicationContext(), OpponentProfile.class);
                    intent.putExtra("id", singleModel.getSenderId());
                    intent.putExtra("firstName", firstName);
                    intent.putExtra("lastName", lastName);
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        groupMessagesRecycler.setAdapter(groupMessagesAdapter);
        if (extras != null) {
            mGroupName = extras.getString("groupName");
            mGroupId = extras.getString("groupId");
            mGroupColor = extras.getString("groupColor");
            mGroupImage = extras.getString("groupImage");
            mGroupDescription = extras.getString("groupDescription");
            mGroupOwnerId = extras.getString("groupOwnerId");
            mGroupOwnerName = extras.getString("groupOwnerName");
            mCount = extras.getString("count");
            mOwnerImage = extras.getString("ownerImage");
            isMember = extras.getBoolean("isMember");
        }
        if (!isMember) {
            mJoinGroupButton.setVisibility(View.VISIBLE);
            mAddMessageToGroup.setVisibility(View.GONE);
        } else {
            mJoinGroupButton.setVisibility(View.GONE);
            mAddMessageToGroup.setVisibility(View.VISIBLE);
        }
        getGroupMessages();
        mGroupSingleDescription.setText(mGroupDescription);
        mGroupSingleFollowersCount.setText(mCount + " " + getString(R.string.participantText));
        setSupportActionBar(mToolbar);
        mCollapsingToolbar.setTitle(mGroupName);

        if (mGroupImage != null && !mGroupImage.isEmpty()) {
            Picasso.with(this).load(Constants.MAIN_URL + Constants.GROUP_IMAGES_FOLDER + mGroupImage).fit().centerCrop()
                    .into(mGroupImageView);
        } else {
            Picasso.with(this).load(R.drawable.no_image_box).fit().centerCrop()
                    .into(mGroupImageView);
        }
        if (mOwnerImage != null && !mOwnerImage.isEmpty()) {
            Picasso.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                    mOwnerImage).transform(new CircleTransform()).fit()
                    .centerCrop().into(mOwnerImageView);
        } else {
            Picasso.with(this).load(R.drawable.no_image).transform(new CircleTransform()).fit()
                    .centerCrop().into(mOwnerImageView);
        }

        if (mGroupColor != null && !mGroupColor.isEmpty()) {
            mGroupBackgroundColor.setBackgroundColor(Color.parseColor(mGroupColor));
            mJoinGroupButton.setTextColor(Color.parseColor(mGroupColor));
            mAddMessageToGroup.setColorNormal(Color.parseColor(mGroupColor));
            mAddMessageToGroup.setColorPressed(Color.parseColor(mGroupColor));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void requestJoin() {
        final Call<ResponseBody> requestJoinCall = Retrofit.getInstance().getInkService().requestJoin(mGroupOwnerId,
                mSharedHelper.getUserId(), mSharedHelper.getFirstName() + " " + mSharedHelper.getLastName(),
                mSharedHelper.getImageLink(), mGroupId);
        requestJoinCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    requestJoin();
                    return;
                }
                if (response.body() == null) {
                    requestJoin();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    Log.d("fsafasfsfas", "onResponse: " + responseBody);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                requestJoin();
            }
        });
    }


    @OnClick(R.id.addMessageToGroup)
    public void addMessage() {
        Intent intent = new Intent(getApplicationContext(), CreateGroupPost.class);
        intent.putExtra("groupId", mGroupId);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void getGroupMessages() {
        groupMessagesLoading.setVisibility(View.VISIBLE);
        if (groupMessagesModels != null) {
            groupMessagesModels.clear();
        }
        groupMessagesAdapter.notifyDataSetChanged();
        Call<ResponseBody> messagesCall = Retrofit.getInstance().getInkService().getGroupMessages(mGroupId);
        messagesCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getGroupMessages();
                    return;
                }
                if (response.body() == null) {
                    getGroupMessages();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        boolean hasMessages = jsonObject.optBoolean("hasMessages");
                        if (hasMessages) {
                            noGroupMessageLayout.setVisibility(View.GONE);
                            JSONArray messagesArray = jsonObject.optJSONArray("messages");
                            for (int i = 0; i < messagesArray.length(); i++) {
                                JSONObject eachObject = messagesArray.optJSONObject(i);
                                String groupId = eachObject.optString("group_id");
                                String groupMessage = eachObject.optString("group_message");
                                String senderId = eachObject.optString("sender_id");
                                String senderImage = eachObject.optString("sender_image");
                                String senderName = eachObject.optString("sender_name");
                                String groupMessageId = eachObject.optString("group_message_id");
                                groupMessagesModel = new GroupMessagesModel(groupId, groupMessage,
                                        senderId, senderImage, senderName, groupMessageId);
                                groupMessagesModels.add(groupMessagesModel);
                                groupMessagesAdapter.notifyDataSetChanged();
                            }
                            hideMessageLoading();
                        } else {
                            hideMessageLoading();
                            noGroupMessageLayout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        getGroupMessages();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getGroupMessages();
            }
        });
    }

    private void hideMessageLoading() {
        groupMessagesLoading.setVisibility(View.GONE);
    }

}
