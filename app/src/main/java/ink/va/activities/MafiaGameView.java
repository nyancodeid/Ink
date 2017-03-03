package ink.va.activities;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.ink.va.R;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.models.MafiaRoomsModel;

public class MafiaGameView extends BaseActivity {
    private MafiaRoomsModel mafiaRoomsModel;
    @BindView(R.id.playersLoading)
    ProgressBar playersLoading;
    @BindView(R.id.playersRecycler)
    RecyclerView playersRecycler;
    @BindView(R.id.replyToRoomED)
    EditText replyToRoomED;

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

}
