package ink.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import ink.adapters.GroupMessagesAdapter;
import ink.adapters.MemberAdapter;
import ink.models.GroupMessagesModel;
import ink.models.MemberModel;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.RecyclerTouchListener;
import ink.utils.Retrofit;
import ink.utils.ScrollAwareFABButtonehavior;
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
    android.support.design.widget.FloatingActionButton mAddMessageToGroup;
    @Bind(R.id.groupMessagesRecycler)
    RecyclerView groupMessagesRecycler;
    @Bind(R.id.groupMessagesLoading)
    AVLoadingIndicatorView groupMessagesLoading;
    @Bind(R.id.noGroupMessageLayout)
    RelativeLayout noGroupMessageLayout;

    private String mGroupName = "";
    private boolean isSocialAccount;
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
    private boolean isRequested;
    private MemberModel memberModel;
    private MemberAdapter memberAdapter;
    private List<MemberModel> memberModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_group_view);
        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);
        Bundle extras = getIntent().getExtras();
        groupMessagesModels = new ArrayList<>();
        memberModels = new ArrayList<>();
        memberAdapter = new MemberAdapter(memberModels, this);
        groupMessagesAdapter = new GroupMessagesAdapter(groupMessagesModels, this);
        mJoinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRequested) {
                    requestJoin();
                }
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
                if (!isMember) {
                    return;
                }
                if (dy > 0)
                    mAddMessageToGroup.hide();
                else if (dy < 0)
                    mAddMessageToGroup.show();
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
            isSocialAccount = extras.getBoolean("isSocialAccount");
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
            CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) mAddMessageToGroup.getLayoutParams();
            p.setBehavior(new ScrollAwareFABButtonehavior(this));
            mAddMessageToGroup.setLayoutParams(p);
        }
        getGroupMessages();
        mGroupSingleDescription.setText(mGroupDescription);
        mGroupSingleFollowersCount.setText(mCount + " " + getString(R.string.participantText));
        if (!mCount.equals("0")) {
            mGroupSingleFollowersCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getParticipants();
                }
            });
        }
        setSupportActionBar(mToolbar);
        mCollapsingToolbar.setTitle(mGroupName);

        if (mGroupImage != null && !mGroupImage.isEmpty()) {
            Picasso.with(this).load(Constants.MAIN_URL + Constants.GROUP_IMAGES_FOLDER + mGroupImage).error(R.drawable.image_laoding_error)
                    .placeholder(R.drawable.no_image_yet_state).fit().centerCrop()
                    .into(mGroupImageView);
        } else {
            mGroupImageView.setBackgroundResource(R.drawable.no_group_image);
        }
        if (mOwnerImage != null && !mOwnerImage.isEmpty()) {
            if (isSocialAccount) {
                Picasso.with(this).load(mOwnerImage).error(R.drawable.image_laoding_error)
                        .placeholder(R.drawable.no_image_yet_state).transform(new CircleTransform()).fit()
                        .centerCrop().into(mOwnerImageView);
            } else {
                Picasso.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                        mOwnerImage).error(R.drawable.image_laoding_error)
                        .placeholder(R.drawable.no_image_yet_state).transform(new CircleTransform()).fit()
                        .centerCrop().into(mOwnerImageView);
            }
        } else {
            Picasso.with(this).load(R.drawable.no_image).transform(new CircleTransform()).fit()
                    .centerCrop().into(mOwnerImageView);
        }

        if (mGroupColor != null && !mGroupColor.isEmpty()) {
            mGroupBackgroundColor.setBackgroundColor(Color.parseColor(mGroupColor));
            mJoinGroupButton.setTextColor(Color.parseColor(mGroupColor));
            mAddMessageToGroup.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(mGroupColor)));
            mAddMessageToGroup.setRippleColor(Color.parseColor("#cccccc"));
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getParticipants() {
        System.gc();
        if (memberModels != null) {
            memberModels.clear();
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(SingleGroupView.this);
        builder.setTitle(getString(R.string.membersTitle));
        View memberView = getLayoutInflater().inflate(R.layout.member_dialog_view, null);
        RecyclerView recyclerView = (RecyclerView) memberView.findViewById(R.id.memberRecycler);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.setAdapter(memberAdapter);
        final AVLoadingIndicatorView membersLoading = (AVLoadingIndicatorView) memberView.findViewById(R.id.membersLoading);
        recyclerView.setAdapter(memberAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (memberModels.get(position).getMemberId().equals(mSharedHelper.getUserId())) {
                    startActivity(new Intent(getApplicationContext(), MyProfile.class));
                } else {
                    String name = memberModels.get(position).getMemberName();
                    String[] splited = name.split("\\s+");
                    String firstName = splited[0];
                    String lastName = splited[1];
                    Intent intent = new Intent(getApplicationContext(), OpponentProfile.class);
                    intent.putExtra("id", memberModels.get(position).getMemberId());
                    intent.putExtra("firstName", firstName);
                    intent.putExtra("lastName", lastName);
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        builder.setView(memberView);
        builder.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
        Call<ResponseBody> participantCall = Retrofit.getInstance().getInkService().getParticipants(mGroupId);
        participantCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getParticipants();
                    return;
                }
                if (response.body() == null) {
                    getParticipants();
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        JSONArray jsonArray = jsonObject.optJSONArray("members");
                        if (jsonArray.length() <= 0) {
                            Snackbar.make(membersLoading, getString(R.string.noMembers), Snackbar.LENGTH_LONG).show();
                            membersLoading.setVisibility(View.GONE);
                            return;
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject eachObject = jsonArray.optJSONObject(i);
                            String memberId = eachObject.optString("participant_id");
                            String memberName = eachObject.optString("participant_name");
                            String memberImage = eachObject.optString("participant_image");
                            String memberItemId = eachObject.optString("participant_item_id");
                            String memberGroupId = eachObject.optString("participant_group_id");
                            memberModel = new MemberModel(memberId, memberName, memberImage, memberItemId
                                    , memberGroupId);
                            memberModels.add(memberModel);
                            memberAdapter.notifyDataSetChanged();
                        }
                        membersLoading.setVisibility(View.GONE);
                    } else {
                        getParticipants();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getParticipants();
            }
        });
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
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        mJoinGroupButton.setText(getString(R.string.pending));
                        Snackbar.make(mJoinGroupButton, getString(R.string.successfullyRequested), Snackbar.LENGTH_SHORT).show();
                        isRequested = true;
                    } else {
                        requestJoin();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
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
        Call<ResponseBody> messagesCall = Retrofit.getInstance().getInkService().getGroupMessages(mGroupId, mSharedHelper.getUserId());
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
                        isRequested = jsonObject.optBoolean("alreadyRequested");
                        if (isRequested) {
                            mJoinGroupButton.setText(getString(R.string.pending));
                        } else {
                            mJoinGroupButton.setText(getString(R.string.joinText));
                        }
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
                                        senderId, senderImage, senderName, groupMessageId, isRequested);
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
