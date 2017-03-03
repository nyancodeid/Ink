package ink.va.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_game_view);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mafiaRoomsModel = Parcels.unwrap(extras.getParcelable("mafiaRoomsModel"));
            getSupportActionBar().setTitle(mafiaRoomsModel.getRoomName());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.replyToRoomIV)
    public void replyToRoomIVClicked() {
        mafiaRoleHolder.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.nightDayIV)
    public void sss() {
        mafiaRoleHolder.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mafia_sniper_role));
    }

    private void clearImage() {
        mafiaRoleHolder.setImageDrawable(null);
    }
}
