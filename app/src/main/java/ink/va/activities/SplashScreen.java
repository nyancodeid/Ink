package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ink.va.R;

import java.util.List;

import ink.va.utils.ProcessManager;
import ink.va.utils.SharedHelper;


public class SplashScreen extends AppCompatActivity {
    private boolean isAppOriginal;
    private String debugKeyHas = "GXEMUTFFeZejMCClv1bXr7Zbid8=";
    private String releaseKeyHash = "JeFV2v/aHMVmxkndxmzynZNlMC8=";
    private String amazonKeyHash = "C2MtEkZmc42C5GvtiILtYM+/OI8=";
    private SharedHelper sharedHelper;
    private View splashRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        splashRootView = findViewById(R.id.splashRootView);
        String currentKeyHash = ink.va.utils.Debug.getKeyHash(this);
        sharedHelper = new SharedHelper(this);
        sharedHelper.putServerNewsOnStartup(true);

        if (currentKeyHash.trim().equals(debugKeyHas.trim())) {
            isAppOriginal = true;
        } else if (currentKeyHash.trim().equals(releaseKeyHash.trim())) {
            isAppOriginal = true;
        } else if (currentKeyHash.trim().equals(amazonKeyHash.trim())) {
            isAppOriginal = true;
        }

        List<String> processes = ProcessManager.getRunningProcesses(this);
        for (String process : processes) {
            Log.d("fsakjfksajflksa", "onCreate: " + process);
        }

        if (isAppOriginal) {
            Intent intent = new Intent(this, Intro.class);
            startActivity(intent);
            finish();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.thisWontWork));
            builder.setMessage(getString(R.string.applicationModified));
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }

    }
}
