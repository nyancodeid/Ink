package ink.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.utils.CircleTransform;
import ink.utils.Constants;

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

    private String mGroupName = "";
    private boolean isFollowing;
    private String mOwnerImage;
    private String mGroupId;
    private String mGroupColor;
    private String mGroupImage;
    private String mGroupDescription;
    private String mGroupOwnerId;
    private String mGroupOwnerName;
    private String mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_group_view);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
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
        }
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

    @OnClick(R.id.addMessageToGroup)
    public void addMessage() {
        Intent intent = new Intent(getApplicationContext(), CreateGroupPost.class);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

}
