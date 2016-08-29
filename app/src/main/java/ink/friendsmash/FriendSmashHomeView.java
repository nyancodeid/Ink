package ink.friendsmash;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ink.R;
import com.koushikdutta.ion.Ion;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.activities.BaseActivity;
import ink.models.CoinsResponse;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import ink.utils.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendSmashHomeView extends BaseActivity {

    @Bind(R.id.userImage)
    ImageView userImage;
    @Bind(R.id.welcomeTextView)
    TextView welcomeTextView;
    @Bind(R.id.numCoins)
    TextView coinsView;
    @Bind(R.id.numBombs)
    TextView bombView;
    @Bind(R.id.playButtonWrapper)
    RelativeLayout playButtonWrapper;

    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_smash_home_view);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        if (sharedHelper.getImageLink() != null && !sharedHelper.getImageLink().isEmpty()) {
            if (isSocialAccount()) {
                Ion.with(this).load(sharedHelper.getImageLink()).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(userImage);
            } else {
                Ion.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + sharedHelper.getImageLink())
                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform())
                        .intoImageView(userImage);
            }
        } else {
            Ion.with(this).load(Constants.ANDROID_DRAWABLE_DIR + Constants.NO_IMAGE_NAME).withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(userImage);
        }
        welcomeTextView.setText(getString(R.string.welcome_to_game, sharedHelper.getFirstName()));
        if (User.get().isCoinsLoaded()) {
            coinsView.setText(String.valueOf(User.get().getCoins()));
        } else {
            getCoins();
            playButtonWrapper.setEnabled(false);
            coinsView.setText(getString(R.string.loadingText));
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @OnClick(R.id.playButtonWrapper)
    public void setPlayButtonWrapper() {
        startActivity(new Intent(getApplicationContext(), FriendSmashGameView.class));
    }

    @OnClick(R.id.scoresButton)
    public void scoresButton() {

    }

    @OnClick(R.id.challengeButton)
    public void challengeButton() {

    }


    private void getCoins() {
        final Call<ResponseBody> coinsCall = Retrofit.getInstance().getInkService().getCoins(sharedHelper.getUserId());
        coinsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getCoins();
                    return;
                }
                if (response.body() == null) {
                    getCoins();
                    return;
                }
                Gson gson = new Gson();
                try {
                    CoinsResponse coinsResponse = gson.fromJson(response.body().string(), CoinsResponse.class);
                    if (coinsResponse.success) {
                        User.get().setCoins(coinsResponse.coins);
                        coinsView.setText(String.valueOf(coinsResponse.coins));
                        User.get().setCoinsLoaded(true);
                        User.get().setCoins(coinsResponse.coins);
                        playButtonWrapper.setEnabled(true);
                    } else {
                        getCoins();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getCoins();
            }
        });
    }
}
