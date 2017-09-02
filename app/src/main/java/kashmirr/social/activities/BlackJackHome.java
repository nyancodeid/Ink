package kashmirr.social.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.kashmirr.social.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlackJackHome extends AppCompatActivity {

    private AlertDialog alertDialog;
    private String[] pots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_jack_home);
        ButterKnife.bind(this);
        pots = new String[]{getString(R.string.low_pot), getString(R.string.medium_pot), getString(R.string.high_pot)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(pots, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int pot = 0;
                switch (which) {
                    case 0:
                        pot = 1;
                        break;
                    case 1:
                        pot = 5;
                        break;
                    case 2:
                        pot = 10;
                        break;
                }
                Intent intent = new Intent(BlackJackHome.this, BlackJack.class);
                intent.putExtra("pot", pot);
                startActivity(intent);
                finish();

            }
        });
        alertDialog = builder.create();
    }


    @OnClick(R.id.playBlackJack)
    public void playClicked() {
        alertDialog.show();
    }
}
