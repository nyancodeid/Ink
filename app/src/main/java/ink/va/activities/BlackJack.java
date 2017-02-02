package ink.va.activities;

import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;

import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlackJack extends BaseActivity {

    @Bind(R.id.playerSecondCard)
    ImageSwitcher playerSecondCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_jack);
        ButterKnife.bind(this);
        playerSecondCard.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left));
        playerSecondCard.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right));
    }

    @OnClick(R.id.flipIt)
    public void flipItClicked() {
        playerSecondCard.setImageResource(R.drawable.ace_black_heart);
    }
}
