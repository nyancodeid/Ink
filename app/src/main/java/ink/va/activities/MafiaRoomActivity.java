package ink.va.activities;

import android.os.Bundle;

import com.ink.va.R;

import butterknife.ButterKnife;

public class MafiaRoomActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_room);
        ButterKnife.bind(this);
    }
}
