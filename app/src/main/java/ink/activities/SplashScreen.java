package ink.activities;

import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.ink.R;

import io.fabric.sdk.android.Fabric;

public class SplashScreen extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash_screen);
        Intent intent = new Intent(this, Intro.class);
        startActivity(intent);
        finish();
    }
}
