package ink.va.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.utils.SharedHelper;

public class MafiaRoomActivity extends BaseActivity {

    @BindView(R.id.roomRecycler)
    RecyclerView roomRecycler;
    private SharedHelper sharedHelper;
    @BindView(R.id.addRoomButton)
    FloatingActionButton addRoomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_room);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        if (sharedHelper.getMenuButtonColor() != null) {
            addRoomButton.setColorFilter(Color.parseColor(sharedHelper.getMenuButtonColor()),
                    PorterDuff.Mode.SRC_ATOP);
        }
    }
}
