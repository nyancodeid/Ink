package ink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.ink.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.utils.SharedHelper;

public class ChatRoulette extends AppCompatActivity {

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
        SharedHelper sharedHelper = new SharedHelper(this);
        if (sharedHelper.shouldShowShowCase()) {
            new ShowcaseView.Builder(this)
                    .withMaterialShowcase()
                    .setTarget(new ViewTarget(joinWaitRoom))
                    .setContentTitle(getString(R.string.welcomeHintText))
                    .setContentText(getString(R.string.clickJoinButtonText))
                    .build();
        }
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
