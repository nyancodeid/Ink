package ink.va.activities;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.DialogUtils;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_BLACK;
import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_GOLD;
import static ink.va.utils.MembershipTypes.MEMBERSHIP_TYPE_RED;


public class VIPActivity extends BaseActivity {

    @Bind(R.id.rootImageView)
    ImageView rootImageView;

    @Bind(R.id.exploreVip)
    Button exploreButton;

    @Bind(R.id.vipGlobalChat)
    Button globalChatButton;

    @Bind(R.id.vipRootTitle)
    TextView vipRootTitle;

    @Bind(R.id.giftWrapper)
    View giftWrapper;

    @Bind(R.id.acceptGift)
    Button closeGiftView;

    @Bind(R.id.membershipTypeTV)
    TextView membershipTypeTV;

    @Bind(R.id.membershipTitle)
    TextView membershipTitle;

    @Bind(R.id.giftImageView)
    ImageView giftImageView;

    private Animation slideInWithFade;
    private Animation slideOutWithFade;
    private Typeface typeface;
    private String giftType;
    private String chosenMembership;
    private SharedHelper sharedHelper;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);
        ButterKnife.bind(this);
        setStatusBarColor(R.color.vip_status_bar_color);
        sharedHelper = new SharedHelper(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        slideInWithFade = AnimationUtils.loadAnimation(this, R.anim.slide_in_with_fade);
        slideOutWithFade = AnimationUtils.loadAnimation(this, R.anim.slide_out_with_fade);

        typeface = Typeface.createFromAsset(getAssets(), "fonts/vip_regular.ttf");
        vipRootTitle.setTypeface(typeface);

        Bundle extras = getIntent().getExtras();
        boolean isFirstVipLogin = extras != null ? extras.getBoolean("firstVipLogin") : false;
        boolean hasGift = extras != null ? extras.getBoolean("hasGift") : false;
        giftType = extras != null ? extras.getString("giftType") : null;
        int resourceId = R.drawable.black_member_card;
        if (giftType != null) {
            switch (giftType) {
                case MEMBERSHIP_TYPE_GOLD:
                    chosenMembership = MEMBERSHIP_TYPE_GOLD;
                    resourceId = R.drawable.gold_member_card;
                    break;
                case MEMBERSHIP_TYPE_RED:
                    chosenMembership = MEMBERSHIP_TYPE_RED;
                    resourceId = R.drawable.red_member_card;
                    break;
                case MEMBERSHIP_TYPE_BLACK:
                    chosenMembership = MEMBERSHIP_TYPE_BLACK;
                    resourceId = R.drawable.black_member_card;
                    break;
            }
        }
        if (hasGift) {
            giftImageView.setImageResource(resourceId);
        }

        membershipTypeTV.setText(giftType != null ? getString(R.string.membershipType, giftType) : "");
        if (isFirstVipLogin) {
            changeButtonVisibility(true);
            showIntro(hasGift);
        } else {
            changeButtonVisibility(false);
            loadMenu();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void loadMenu() {
        exploreButton.startAnimation(slideInWithFade);
        globalChatButton.startAnimation(slideInWithFade);
    }

    private void showIntro(boolean hasGift) {
        if (hasGift) {
            closeGiftView.setClickable(false);
            closeGiftView.setEnabled(false);
            giftWrapper.setVisibility(View.VISIBLE);
            slideInWithFade.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    closeGiftView.setClickable(true);
                    closeGiftView.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            giftWrapper.startAnimation(slideInWithFade);
        } else {
            changeButtonVisibility(false);
            loadMenu();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        overrideActivityAnimation();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overrideActivityAnimation();
    }

    @OnClick(R.id.vipGlobalChat)
    public void globeChatClicked() {
    }

    @OnClick(R.id.exploreVip)
    public void exploreClicked() {

    }

    @OnClick(R.id.acceptGift)
    public void acceptGiftClicked() {
        giftWrapper.clearAnimation();
        closeGiftView.setClickable(false);
        closeGiftView.setEnabled(false);
        giftWrapper.startAnimation(slideOutWithFade);
        slideOutWithFade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                giftWrapper.setVisibility(View.GONE);
                showVipLoading();
                changeMembership();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void changeMembership() {
        final Call<ResponseBody> changeMembership = Retrofit.getInstance().getInkService().changeMembership(sharedHelper.getUserId(), chosenMembership);
        changeMembership.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    changeMembership();
                    return;
                }
                if (response.body() == null) {
                    changeMembership();
                    return;
                }
                hideVipLoading();
                changeButtonVisibility(false);
                loadMenu();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (!success) {
                        DialogUtils.showDialog(VIPActivity.this, getString(R.string.error), getString(R.string.membershipChangeError), false, null, false, null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideVipLoading();
                DialogUtils.showDialog(VIPActivity.this, getString(R.string.error), getString(R.string.serverErrorText), false, null, false, null);
            }
        });
    }

    private void changeButtonVisibility(boolean hide) {
        exploreButton.setVisibility(hide ? View.GONE : View.VISIBLE);
        globalChatButton.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("VIP Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
