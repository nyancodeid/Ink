package ink.va.activities;

import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.view.MenuItem;
import android.widget.EditText;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MafiaAddRoomActivity extends BaseActivity {
    @BindView(R.id.roomNameTV)
    EditText roomNameTV;
    @BindView(R.id.durationMorningED)
    EditText durationMorningED;
    @BindView(R.id.nightDurationED)
    EditText nightDurationED;

    @BindView(R.id.languageSpinner)
    AppCompatSpinner languageSpinner;
    @BindView(R.id.gameTypeSpinner)
    AppCompatSpinner gameTypeSpinner;
    @BindView(R.id.gameMorningDurationSpinner)
    AppCompatSpinner gameMorningDurationSpinner;
    @BindView(R.id.gameNightDurationSpinner)
    AppCompatSpinner gameNightDurationSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_add_room);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle(getString(R.string.addRoom));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.saveAddRoom)
    public void saveAddRoomClicked() {

    }
}
