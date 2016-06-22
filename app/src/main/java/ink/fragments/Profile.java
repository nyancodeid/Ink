package ink.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.ink.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by USER on 2016-06-22.
 */
public class Profile extends AppCompatActivity {
    private String mFriendId;
    private String mFirstName;
    private String mLastName;
    private String mPhoneNumber;
    private Target mTarget;
    private ImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);
        Bundle extras = getIntent().getExtras();
        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        Picasso.with(this).load(R.drawable.no_image).into(getTarget());
        ActionBar actionBar = getSupportActionBar();
        if (extras != null) {
            mFriendId = extras.getString("id");
            mFirstName = extras.getString("firstName");
            mLastName = extras.getString("lastName");
            mPhoneNumber = extras.getString("phoneNumber");
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(mFirstName + " " + mLastName);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        supportFinishAfterTransition();
        return super.onOptionsItemSelected(item);
    }


    private Target getTarget() {
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mProfileImage.setBackground(new BitmapDrawable(getResources(), bitmap));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return mTarget;
    }
}
