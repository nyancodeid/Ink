package ink.va.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Button;

import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatRoulette extends BaseActivity {

    @Bind(R.id.joinWaitRoom)
    Button joinWaitRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_roulette);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.chatRoulette));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        System.gc();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.joinWaitRoom)
    public void joinWaitRoom() {
        startActivity(new Intent(getApplicationContext(), WaitRoom.class));
    }
}
