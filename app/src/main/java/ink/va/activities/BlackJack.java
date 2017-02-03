package ink.va.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ink.va.R;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.Animations;
import ink.va.utils.ProcessManager;
import ink.va.utils.Random;


public class BlackJack extends BaseActivity {

    @BindView(R.id.playerLayout)
    LinearLayout playerLayout;
    @BindView(R.id.dealerLayout)
    LinearLayout dealerLayout;
    @BindView(R.id.playerScore)
    TextView playerScore;
    @BindView(R.id.flipIt)
    Button flipIt;
    private Animation fadeInAnimation;
    private int blackJack = 21;
    private List<Integer> dealerCountArray;
    private List<Integer> playerCountArray;
    private int maxCards = 5;
    private int playerCount;
    private int dealerCount;
    private int playerSumCount;
    private int dealerSumCount;
    private boolean restart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_jack);
        ButterKnife.bind(this);
        dealerCountArray = new LinkedList<>();
        playerCountArray = new LinkedList<>();

        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);

        if (ProcessManager.hasHacks(this)) {

        } else {

        }

        playerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawGame();
            }
        });
    }

    private void drawGame() {
        drawPlayerCard(false);
        drawPlayerCard(true);
    }

    @OnClick(R.id.flipIt)
    public void flipItClicked() {
        if (restart) {
            flipIt.setText("ask for a card");
            endGame();
            restart = false;
            drawGame();
        } else {
            flipIt.setText("Flip It");
            drawPlayerCard(true);
        }

    }

    private void drawPlayerCard(boolean applyMargin) {
        final Drawable newCard = getRandomCard();
        sumUpPlayer();
        openNewCard(false, applyMargin, newCard);
    }

    private void drawDealerCard(boolean applyMargin) {
        final Drawable newCard = getRandomCard();
        sumUpPlayer();
        openNewCard(false, applyMargin, newCard);
    }

    private void openNewCard(boolean dealersCard, boolean applyMargin, final Drawable newCard) {
        final ImageView imageView = new ImageView(this);
        imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.card_background));
        imageView.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.image_view_width), (int) getResources().getDimension(R.dimen.image_view_height)));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.setMargins(applyMargin ? (int) getResources().getDimension(R.dimen.image_view_negative_margin) : 0, 0, 0, 0);
        imageView.setLayoutParams(layoutParams);
        if (dealersCard) {
            dealerLayout.addView(imageView);
        } else {
            playerLayout.addView(imageView);
        }
        imageView.startAnimation(fadeInAnimation);
        Animations.flip(imageView, newCard);
    }

    private void sumUpPlayer() {
        playerCountArray.add(playerCount);
        int sum = 0;
        for (int playerCurrentCount : playerCountArray) {
            sum = sum + playerCurrentCount;
        }
        playerSumCount = sum;
        checkBlackJackAndBurn();

    }

    @OnClick(R.id.stand)
    public void stand() {

    }

    public Drawable getRandomCard() {
        int randomNumber = Random.getRandomNumberInRange(1, 52);
        switch (randomNumber) {
            case 1:
                if (playerSumCount + 11 >= blackJack) {
                    playerCount = 1;
                } else {
                    playerCount = 11;
                }
                return ContextCompat.getDrawable(this, R.drawable.ace_red_heart);
            case 2:
                playerCount = 2;
                return ContextCompat.getDrawable(this, R.drawable.two_red_heart);
            case 3:
                playerCount = 3;
                return ContextCompat.getDrawable(this, R.drawable.three_red_heart);
            case 4:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.king_black_arrow);
            case 5:
                playerCount = 4;

                return ContextCompat.getDrawable(this, R.drawable.four_red_heart);
            case 6:
                playerCount = 5;

                return ContextCompat.getDrawable(this, R.drawable.five_red_heart);
            case 7:
                playerCount = 6;

                return ContextCompat.getDrawable(this, R.drawable.six_red_heart);
            case 8:
                playerCount = 7;

                return ContextCompat.getDrawable(this, R.drawable.seven_red_heart);
            case 9:
                playerCount = 8;

                return ContextCompat.getDrawable(this, R.drawable.eight_red_heart);
            case 10:
                playerCount = 9;

                return ContextCompat.getDrawable(this, R.drawable.nine_red_heart);
            case 11:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.ten_red_heart);
            case 12:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.vallet_red_heart);
            case 13:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.queen_red_heart);
            case 14:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.king_red_heart);
            case 15:
                if (playerSumCount + 11 >= blackJack) {
                    playerCount = 1;
                } else {
                    playerCount = 11;
                }

                return ContextCompat.getDrawable(this, R.drawable.ace_black_heart);
            case 16:
                playerCount = 2;

                return ContextCompat.getDrawable(this, R.drawable.two_black_heart);
            case 17:
                playerCount = 3;

                return ContextCompat.getDrawable(this, R.drawable.three_black_heart);
            case 18:
                playerCount = 4;

                return ContextCompat.getDrawable(this, R.drawable.four_black_heart);
            case 19:
                playerCount = 5;

                return ContextCompat.getDrawable(this, R.drawable.five_black_heart);
            case 20:

                playerCount = 6;

                return ContextCompat.getDrawable(this, R.drawable.six_black_heart);
            case 21:
                playerCount = 7;
                return ContextCompat.getDrawable(this, R.drawable.seven_black_heart);
            case 22:
                playerCount = 8;

                return ContextCompat.getDrawable(this, R.drawable.eight_black_heart);
            case 23:
                playerCount = 9;

                return ContextCompat.getDrawable(this, R.drawable.nine_black_heart);
            case 24:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.ten_black_heart);
            case 25:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.vallet_black_heart);
            case 26:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.queen_black_heart);
            case 27:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.king_black_heart);
            case 28:
                if (playerSumCount + 11 >= blackJack) {
                    playerCount = 1;
                } else {
                    playerCount = 11;
                }

                return ContextCompat.getDrawable(this, R.drawable.ace_red_arrow);
            case 29:
                playerCount = 2;

                return ContextCompat.getDrawable(this, R.drawable.two_red_arrow);
            case 30:
                playerCount = 3;

                return ContextCompat.getDrawable(this, R.drawable.three_red_arrow);
            case 31:
                playerCount = 4;

                return ContextCompat.getDrawable(this, R.drawable.four_red_arrow);
            case 32:
                playerCount = 5;

                return ContextCompat.getDrawable(this, R.drawable.five_red_arrow);
            case 33:
                playerCount = 6;

                return ContextCompat.getDrawable(this, R.drawable.six_red_arrow);
            case 34:
                playerCount = 7;

                return ContextCompat.getDrawable(this, R.drawable.seven_red_arrow);
            case 35:
                playerCount = 8;

                return ContextCompat.getDrawable(this, R.drawable.eight_red_arrow);
            case 36:
                playerCount = 8;

                return ContextCompat.getDrawable(this, R.drawable.nine_red_arrow);
            case 37:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.ten_red_arrow);
            case 38:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.vallet_red_arrow);
            case 39:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.queen_red_arrow);
            case 40:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.king_red_arrow);
            case 41:
                if (playerSumCount + 11 >= blackJack) {
                    playerCount = 1;
                } else {
                    playerCount = 11;
                }

                return ContextCompat.getDrawable(this, R.drawable.ace_black_arrow);
            case 42:
                playerCount = 2;

                return ContextCompat.getDrawable(this, R.drawable.two_black_arrow);
            case 43:
                playerCount = 3;

                return ContextCompat.getDrawable(this, R.drawable.three_black_arrow);
            case 44:
                playerCount = 4;

                return ContextCompat.getDrawable(this, R.drawable.four_black_arrow);
            case 45:
                playerCount = 5;

                return ContextCompat.getDrawable(this, R.drawable.five_black_arrow);
            case 46:
                playerCount = 6;

                return ContextCompat.getDrawable(this, R.drawable.six_black_arrow);
            case 47:
                playerCount = 7;

                return ContextCompat.getDrawable(this, R.drawable.seven_black_arrow);
            case 48:
                playerCount = 8;

                return ContextCompat.getDrawable(this, R.drawable.eight_black_arrow);
            case 49:
                playerCount = 9;
                return ContextCompat.getDrawable(this, R.drawable.nine_black_arrow);
            case 50:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.ten_black_arrow);
            case 51:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.vallet_black_arrow);
            case 52:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.queen_black_arrow);
            default:
                playerCount = 10;

                return ContextCompat.getDrawable(this, R.drawable.king_black_arrow);
        }

    }

    private void checkBlackJackAndBurn() {
        //Meaning the player got 2 cards,so we need to check for the blackjack and we shall check if the player had burn while taking the cards

        if (playerSumCount > blackJack) {
            playerScore.setText(getString(R.string.player_hand, playerSumCount));
            flipIt.setText("restart");
            restart = true;
        } else {
            playerScore.setText(getString(R.string.player_hand, playerSumCount));
            if (maxCardsExceeded()) {
                Toast.makeText(this, "must open cards automatically", Toast.LENGTH_SHORT).show();
            } else {
                if (playerCountArray.size() == 2 && playerCountArray.get(0) + playerCountArray.get(1) == blackJack) {
                    boolean dealerHasBlackJack = false;
                    //meaning user has the black jack,now check the dealers hand see if the dealer got the 10
                    for (int dealersCount : dealerCountArray) {
                        if (dealersCount == 10) {
                            dealerHasBlackJack = true;
                        }
                    }
                    if (dealerHasBlackJack) {
                        Toast.makeText(this, "dealer has black jack", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "dealer has not  black jack", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }

    private void endGame() {
        playerCountArray.clear();
        playerSumCount = 0;
        playerCount = 0;
        dealerCountArray.clear();
        dealerCount = 0;
        dealerSumCount = 0;
        playerLayout.removeAllViews();
        playerLayout.invalidate();
    }

    private boolean maxCardsExceeded() {
        return playerCountArray.size() >= maxCards;
    }

}
