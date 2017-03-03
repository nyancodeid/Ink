package ink.va.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ink.va.R;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.models.MafiaRoomsModel;
import ink.va.utils.SharedHelper;
import ink.va.utils.TransparentPanel;

public class MafiaGameView extends BaseActivity {
    private MafiaRoomsModel mafiaRoomsModel;
    @BindView(R.id.playersLoading)
    ProgressBar playersLoading;
    @BindView(R.id.playersRecycler)
    RecyclerView playersRecycler;
    @BindView(R.id.replyToRoomED)
    EditText replyToRoomED;
    @BindView(R.id.activity_mafia_game_view)
    TransparentPanel transparentPanel;
    @BindView(R.id.replyToRoomIV)
    ImageView replyToRoom;
    @BindView(R.id.chatLoading)
    ProgressBar chatLoading;
    @BindView(R.id.nightDayIV)
    ImageView nightDayIV;
    @BindView(R.id.mafiaRoleView)
    View mafiaRoleView;
    @BindView(R.id.mafiaRoleExplanationTV)
    TextView mafiaRoleExplanationTV;
    @BindView(R.id.mafiaRoleHolder)
    ImageView mafiaRoleHolder;
    @BindView(R.id.closeRoleView)
    Button closeRoleView;
    private Animation slideOutWithFade;
    private Animation slideInWithFade;
    private SharedHelper sharedHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_game_view);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        slideOutWithFade = AnimationUtils.loadAnimation(this, R.anim.slide_out_with_fade);
        slideInWithFade = AnimationUtils.loadAnimation(this, R.anim.slide_in_with_fade);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mafiaRoomsModel = Parcels.unwrap(extras.getParcelable("mafiaRoomsModel"));
            getSupportActionBar().setTitle(mafiaRoomsModel.getRoomName());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initEditText(isParticipant());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.replyToRoomIV)
    public void replyToRoomIVClicked() {
        openRoleView(R.drawable.mafia_sniper_role);
    }

    @OnClick(R.id.closeRoleView)
    public void closeRoleClicked() {
        closeRoleView();
    }

    private void clearImage() {
        mafiaRoleHolder.setImageDrawable(null);
    }

    private void setButtonState(boolean enabled) {
        closeRoleView.setEnabled(enabled);
    }

    private void openRoleView(final int roleResourceId) {
        mafiaRoleView.setVisibility(View.VISIBLE);
        slideInWithFade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setButtonState(true);
                mafiaRoleHolder.setImageDrawable(ContextCompat.getDrawable(MafiaGameView.this, roleResourceId));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mafiaRoleView.startAnimation(slideInWithFade);
    }

    private void closeRoleView() {
        setButtonState(false);
        slideOutWithFade.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mafiaRoleView.setVisibility(View.GONE);
                clearImage();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mafiaRoleView.startAnimation(slideOutWithFade);
    }

    private void initEditText(boolean enabled) {
        if (enabled) {
            replyToRoomED.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = s.toString().toString();
                    if (text.isEmpty()) {
                        replyToRoom.setEnabled(false);
                    } else {
                        replyToRoom.setEnabled(true);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else {
            replyToRoomED.setEnabled(false);
            replyToRoomED.setClickable(false);
            replyToRoomED.setFocusable(false);
            replyToRoomED.setFocusableInTouchMode(false);
            replyToRoomED.setHint(getString(R.string.cantReply));
            replyToRoom.setEnabled(false);
        }

    }

    private boolean isParticipant() {
        boolean isParticipant = false;
        String currentUserId = sharedHelper.getUserId();
        for (String eachId : mafiaRoomsModel.getJoinedUserIds()) {
            if (eachId.equals(currentUserId)) {
                isParticipant = true;
                break;
            }
        }
        return isParticipant;
    }
}
