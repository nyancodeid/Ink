package ink.va.activities;

import android.content.Intent;
import android.os.Bundle;

import com.ink.va.R;

public class SplashScreen extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Intent intent = new Intent(this, Intro.class);
        startActivity(intent);
        finish();
    }
}
