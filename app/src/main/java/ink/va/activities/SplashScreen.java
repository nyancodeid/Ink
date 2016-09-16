package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.ink.va.R;

public class SplashScreen extends BaseActivity {
    private boolean isAppOriginal;
    private String debugKeyHas = "GXEMUTFFeZejMCClv1bXr7Zbid8=";
    private String releaseKeyHash = "JeFV2v/aHMVmxkndxmzynZNlMC8=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        String currentKeyHash = ink.va.utils.Debug.getKeyHash(this);


        if (currentKeyHash.trim().equals(debugKeyHas.trim())) {
            isAppOriginal = true;
        } else if (currentKeyHash.trim().equals(releaseKeyHash.trim())) {
            isAppOriginal = true;
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