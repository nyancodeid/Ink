package kashmirr.social.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.widget.Button;

import com.kashmirr.social.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatRoulette extends BaseActivity {

    @BindView(R.id.joinWaitRoom)
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
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.joinWaitRoom)
    public void joinWaitRoom() {
        PopupMenu popupMenu = new PopupMenu(this, joinWaitRoom);
        popupMenu.getMenu().add(0, 0, 0, getString(R.string.englishRoom));
        popupMenu.getMenu().add(1, 1, 1, getString(R.string.russianRoom));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 0:
                        Intent intent = new Intent(getApplicationContext(), WaitRoom.class);
                        intent.putExtra("language", "en");
                        startActivity(intent);
                        return false;
                    case 1:
                        intent = new Intent(getApplicationContext(), WaitRoom.class);
                        intent.putExtra("language", "ru");
                        startActivity(intent);
                        return false;
                }
                return false;
            }
        });
        popupMenu.show();

    }
}
