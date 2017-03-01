package ink.va.activities;

import android.os.Bundle;

import com.ink.va.R;

import org.parceler.Parcels;

import ink.va.models.MafiaRoomsModel;

public class MafiaGameView extends BaseActivity {
    private MafiaRoomsModel mafiaRoomsModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_game_view);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mafiaRoomsModel = Parcels.unwrap(extras.getParcelable("mafiaRoomsModel"));
        }
    }
}
