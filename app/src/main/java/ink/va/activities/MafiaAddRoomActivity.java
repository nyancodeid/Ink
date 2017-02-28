package ink.va.activities;

import android.os.Bundle;
import android.view.MenuItem;

import com.ink.va.R;


public class MafiaAddRoomActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_add_room);
        getSupportActionBar().setTitle(getString(R.string.addRoom));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
