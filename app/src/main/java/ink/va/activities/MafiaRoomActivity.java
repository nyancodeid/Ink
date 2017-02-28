package ink.va.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.SharedHelper;

public class MafiaRoomActivity extends BaseActivity {

    public static final int ADD_ROOM_REQUEST_CODE = 8;
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
        getSupportActionBar().setTitle(getString(R.string.rooms));
        sharedHelper = new SharedHelper(this);
        if (sharedHelper.getMenuButtonColor() != null) {
            addRoomButton.setBackgroundTintList((ColorStateList.valueOf(Color.parseColor(sharedHelper.getMenuButtonColor()))));
        }
    }

    @OnClick(R.id.addRoomButton)
    public void addRoomClicked() {
        startActivityForResult(new Intent(this, MafiaAddRoomActivity.class), ADD_ROOM_REQUEST_CODE);
    }
}
