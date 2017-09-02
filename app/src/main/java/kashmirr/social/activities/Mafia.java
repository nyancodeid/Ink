package kashmirr.social.activities;

import android.content.Intent;
import android.os.Bundle;

import com.kashmirr.social.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class Mafia extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.playMafia)
    public void playClicked() {
        startActivity(new Intent(this, MafiaRoomActivity.class));
        finish();
    }
}
