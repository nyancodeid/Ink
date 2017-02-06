package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.callbacks.GeneralCallback;
import ink.va.utils.Animations;
import ink.va.utils.Constants;
import ink.va.utils.ProcessManager;
import ink.va.utils.Random;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BlackJack extends BaseActivity {

    @BindView(R.id.playerLayout)
    LinearLayout playerLayout;
    @BindView(R.id.dealerLayout)
    LinearLayout dealerLayout;
    @BindView(R.id.playerScore)
    TextView playerScore;
    @BindView(R.id.takeCard)
    Button takeCard;
    @BindView(R.id.openCards)
    Button openCards;
    @BindView(R.id.coinsTV)
    TextView coinsTV;
    @BindView(R.id.potTV)
    TextView potTV;
    private Animation fadeInAnimation;
    private int blackJack = 21;
    private List<Integer> dealerCountArray;
    private List<Integer> playerCountArray;
    private int maxCards = 5;
    private int playerCount;
    private int dealerCount;
    private int playerSumCount;
    private int dealerSumCount;
    private List<Drawable> dealersHiddenCard;
    private List<ImageView> dealersHiddenImageView;
    private int dealerMinimumHand = 17;
    private int maximumPot;
    int currentBanks = 0;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_jack);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        dealerCountArray = new LinkedList<>();
        playerCountArray = new LinkedList<>();
        dealersHiddenCard = new LinkedList<>();
        dealersHiddenImageView = new LinkedList<>();
        maximumPot = getIntent().getExtras() != null ? getIntent().getExtras().getInt("pot") : 0;
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);

        switch (maximumPot) {
            case 1:
                potTV.setText(getString(R.string.low_pot));
                break;
            case 5:
                potTV.setText(getString(R.string.medium_pot));
                break;
            case 10:
                potTV.setText(getString(R.string.high_pot));
                break;
        }

        initCoinsPot();

    }

    private boolean initCoinsPot() {
        if (User.get().getCoins() < maximumPot) {
            Snackbar.make(dealerLayout, getString(R.string.not_enough_coins), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.buyCoins), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(getApplicationContext(), BuyCoins.class), Constants.BUY_COINS_REQUEST_CODE);
                }
            }).show();
            changeButtons(false);
            coinsTV.setText(getString(R.string.coinsText, User.get().getCoins()));
            return false;
        } else {
            drawGame();
            changeButtons(true);
            currentBanks = maximumPot * 2;
            int userLeftCoins = User.get().getCoins() - maximumPot;
            User.get().setCoins(userLeftCoins);
            coinsTV.setText(getString(R.string.coinsText, User.get().getCoins()));
            return true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ProcessManager.hasHacks(this)) {
            AlertDialog alertDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle(getString(R.string.error));
            builder.setMessage(getString(R.string.hack_engine_detected));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog = builder.show();
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.BUY_COINS_REQUEST_CODE:
                boolean coinsBought = data.getExtras().getBoolean(Constants.COINS_BOUGHT_KEY);
                if (coinsBought) {
                    updateUserCoins();
                }
                break;
        }
    }

    private void updateUserCoins() {
        initCoinsPot();
    }

    private void drawGame() {
        drawPlayerCard(false, null);
        drawPlayerCard(true, null);
        drawDealerCard(false, null);
        drawDealerCard(true, null);
    }

    @OnClick(R.id.takeCard)
    public void takeCardClicked() {
        drawPlayerCard(true, null);
    }

    private void drawPlayerCard(boolean applyMargin, @Nullable GeneralCallback generalCallback) {
        final Drawable newCard = getRandomCard(false);
        sumUpHandValue(false);
        openNewCard(false, applyMargin, newCard, generalCallback);
    }

    private void drawDealerCard(boolean applyMargin, @Nullable GeneralCallback generalCallback) {
        final Drawable newCard = getRandomCard(true);
        sumUpHandValue(true);
        openNewCard(true, applyMargin, newCard, generalCallback);
    }

    private void openNewCard(boolean dealersCard, boolean applyMargin, final Drawable newCard, GeneralCallback generalCallback) {
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
        if (applyMargin && dealersCard) {
            dealersHiddenCard.add(newCard);
            dealersHiddenImageView.add(imageView);
        }
        Animations.flip(imageView, applyMargin && dealersCard ? ContextCompat.getDrawable(this, R.drawable.card_background) : newCard);
        if (generalCallback != null) {
            generalCallback.onSuccess(null);
        }
    }

    private void sumUpHandValue(boolean sumDealer) {
        if (sumDealer) {
            dealerCountArray.add(dealerCount);
            int sum = 0;
            for (int playerCurrentCount : dealerCountArray) {
                sum = sum + playerCurrentCount;
            }
            dealerSumCount = sum;
        } else {
            playerCountArray.add(playerCount);
            int sum = 0;
            for (int playerCurrentCount : playerCountArray) {
                sum = sum + playerCurrentCount;
            }
            playerSumCount = sum;
        }

        checkBlackJackAndBurn();

    }

    @OnClick(R.id.openCards)
    public void stand() {
        openDealersCard();
    }

    private void checkDealerHand() {
        boolean checkDealerCards = false;
        int randomLogic = Random.getRandomNumberInRange(0, 10);
        switch (randomLogic) {
            case 0:
                checkDealerCards = true;
                break;
            case 1:
                checkDealerCards = false;
                break;
            case 2:
                checkDealerCards = false;
                break;
            case 3:
                checkDealerCards = true;
                break;
            case 4:
                checkDealerCards = true;
                break;
            case 5:
                checkDealerCards = false;
                break;
            case 6:
                checkDealerCards = false;
                break;
            case 7:
                checkDealerCards = true;
                break;
            case 8:
                checkDealerCards = false;
                break;
            case 9:
                checkDealerCards = true;
                break;
            case 10:
                checkDealerCards = true;
                break;

        }
        if (dealerSumCount <= 10) {
            checkDealerCards = true;
        }
        if (checkDealerCards) {
            if (dealerSumCount < dealerMinimumHand) {
                drawDealerCard(true, new GeneralCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        checkDealerHand();
                    }

                    @Override
                    public void onFailure(Object o) {

                    }
                });
            }
        }
    }

    private void openDealersCard() {
        checkDealerHand();
        changeButtons(false);
        for (int i = 0; i < dealersHiddenImageView.size(); i++) {
            Drawable drawable = dealersHiddenCard.get(i);
            ImageView imageView = dealersHiddenImageView.get(i);
            Animations.flip(imageView, drawable);
        }

        if (dealerSumCount <= blackJack && dealerSumCount > playerSumCount) {
            User.get().setCoins(User.get().getCoins() - maximumPot);
            coinsTV.setText(getString(R.string.coinsText, User.get().getCoins()));
            Snackbar.make(dealerLayout, getString(R.string.you_lost), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.restart), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restartGame();
                }
            }).show();
        } else if (dealerSumCount < playerSumCount && playerSumCount <= blackJack) {
            int finalUserCoins = User.get().getCoins() + currentBanks;
            User.get().setCoins(finalUserCoins);
            Snackbar.make(dealerLayout, getString(R.string.you_won, currentBanks), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.restart), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restartGame();
                }
            }).show();
        } else if (dealerSumCount == playerSumCount) {
            User.get().setCoins(User.get().getCoins() + maximumPot);
            coinsTV.setText(getString(R.string.coinsText, User.get().getCoins()));
            Snackbar.make(dealerLayout, getString(R.string.draw), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.restart), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restartGame();
                }
            }).show();
        } else if (dealerSumCount > blackJack) {
            int finalUserCoins = User.get().getCoins() + currentBanks;
            User.get().setCoins(finalUserCoins);
            coinsTV.setText(getString(R.string.coinsText, User.get().getCoins()));
            Snackbar.make(dealerLayout, getString(R.string.you_won, currentBanks), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.restart), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restartGame();
                }
            }).show();
        }
        silentCoinsUpdate();
    }

    private void silentCoinsUpdate() {

        Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().silentCoinsUpdate(sharedHelper.getUserId(), String.valueOf(User.get().getCoins()), Constants.USER_COINS_TOKEN);
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    silentCoinsUpdate();
                    return;
                }
                if (response.body() == null) {
                    silentCoinsUpdate();
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (!success) {
                        Toast.makeText(BlackJack.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(BlackJack.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(BlackJack.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(BlackJack.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeButtons(boolean enabled) {
        if (enabled) {
            takeCard.setEnabled(true);
            takeCard.setAlpha(1);
            openCards.setEnabled(true);
            openCards.setAlpha(1);
        } else {
            takeCard.setEnabled(false);
            takeCard.setAlpha((float) 0.7);
            openCards.setEnabled(false);
            openCards.setAlpha((float) 0.7);
        }
    }


    public Drawable getRandomCard(boolean dealerCard) {
        int randomNumber = Random.getRandomNumberInRange(1, 52);
        switch (randomNumber) {
            case 1:
                if (dealerCard) {
                    if (dealerSumCount + 11 > blackJack) {
                        dealerCount = 1;
                    } else {
                        dealerCount = 11;
                    }
                } else {
                    if (playerSumCount + 11 > blackJack) {
                        playerCount = 1;
                    } else {
                        playerCount = 11;
                    }
                }

                return ContextCompat.getDrawable(this, R.drawable.ace_red_heart);
            case 2:
                if (dealerCard) {
                    dealerCount = 2;
                } else {
                    playerCount = 2;
                }

                return ContextCompat.getDrawable(this, R.drawable.two_red_heart);
            case 3:
                if (dealerCard) {
                    dealerCount = 3;
                } else {
                    playerCount = 3;
                }
                return ContextCompat.getDrawable(this, R.drawable.three_red_heart);
            case 4:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }

                return ContextCompat.getDrawable(this, R.drawable.king_black_arrow);
            case 5:
                if (dealerCard) {
                    dealerCount = 4;
                } else {
                    playerCount = 4;
                }

                return ContextCompat.getDrawable(this, R.drawable.four_red_heart);
            case 6:
                if (dealerCard) {
                    dealerCount = 5;
                } else {
                    playerCount = 5;
                }

                return ContextCompat.getDrawable(this, R.drawable.five_red_heart);
            case 7:
                if (dealerCard) {
                    dealerCount = 6;
                } else {
                    playerCount = 6;
                }

                return ContextCompat.getDrawable(this, R.drawable.six_red_heart);
            case 8:
                if (dealerCard) {
                    dealerCount = 7;
                } else {
                    playerCount = 7;
                }

                return ContextCompat.getDrawable(this, R.drawable.seven_red_heart);
            case 9:
                if (dealerCard) {
                    dealerCount = 8;
                } else {
                    playerCount = 8;
                }

                return ContextCompat.getDrawable(this, R.drawable.eight_red_heart);
            case 10:
                if (dealerCard) {
                    dealerCount = 9;
                } else {
                    playerCount = 9;
                }

                return ContextCompat.getDrawable(this, R.drawable.nine_red_heart);
            case 11:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }

                return ContextCompat.getDrawable(this, R.drawable.ten_red_heart);
            case 12:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }

                return ContextCompat.getDrawable(this, R.drawable.vallet_red_heart);
            case 13:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }

                return ContextCompat.getDrawable(this, R.drawable.queen_red_heart);
            case 14:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }

                return ContextCompat.getDrawable(this, R.drawable.king_red_heart);
            case 15:

                if (dealerCard) {
                    if (dealerSumCount + 11 > blackJack) {
                        dealerCount = 1;
                    } else {
                        dealerCount = 11;
                    }

                } else {
                    if (playerSumCount + 11 > blackJack) {
                        playerCount = 1;
                    } else {
                        playerCount = 11;
                    }

                }


                return ContextCompat.getDrawable(this, R.drawable.ace_black_heart);
            case 16:
                if (dealerCard) {
                    dealerCount = 2;
                } else {
                    playerCount = 2;
                }


                return ContextCompat.getDrawable(this, R.drawable.two_black_heart);
            case 17:
                if (dealerCard) {
                    dealerCount = 3;
                } else {
                    playerCount = 3;
                }


                return ContextCompat.getDrawable(this, R.drawable.three_black_heart);
            case 18:
                if (dealerCard) {
                    dealerCount = 4;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.four_black_heart);
            case 19:
                if (dealerCard) {
                    dealerCount = 5;
                } else {
                    playerCount = 5;
                }


                return ContextCompat.getDrawable(this, R.drawable.five_black_heart);
            case 20:

                if (dealerCard) {
                    dealerCount = 6;
                } else {
                    playerCount = 6;
                }


                return ContextCompat.getDrawable(this, R.drawable.six_black_heart);
            case 21:
                if (dealerCard) {
                    dealerCount = 7;
                } else {
                    playerCount = 7;
                }

                return ContextCompat.getDrawable(this, R.drawable.seven_black_heart);
            case 22:
                if (dealerCard) {
                    dealerCount = 8;
                } else {
                    playerCount = 8;
                }


                return ContextCompat.getDrawable(this, R.drawable.eight_black_heart);
            case 23:
                if (dealerCard) {
                    dealerCount = 9;
                } else {
                    playerCount = 9;
                }


                return ContextCompat.getDrawable(this, R.drawable.nine_black_heart);
            case 24:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.ten_black_heart);
            case 25:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.vallet_black_heart);
            case 26:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.queen_black_heart);
            case 27:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.king_black_heart);
            case 28:
                if (dealerCard) {
                    if (dealerSumCount + 11 > dealerSumCount) {
                        dealerCount = 1;
                    } else {
                        dealerCount = 11;
                    }

                } else {
                    if (playerSumCount + 11 > blackJack) {
                        playerCount = 1;
                    } else {
                        playerCount = 11;
                    }

                }

                return ContextCompat.getDrawable(this, R.drawable.ace_red_arrow);
            case 29:
                if (dealerCard) {
                    dealerCount = 2;
                } else {
                    playerCount = 2;
                }


                return ContextCompat.getDrawable(this, R.drawable.two_red_arrow);
            case 30:
                if (dealerCard) {
                    dealerCount = 3;
                } else {
                    playerCount = 3;
                }


                return ContextCompat.getDrawable(this, R.drawable.three_red_arrow);
            case 31:
                if (dealerCard) {
                    dealerCount = 4;
                } else {
                    playerCount = 4;
                }


                return ContextCompat.getDrawable(this, R.drawable.four_red_arrow);
            case 32:
                if (dealerCard) {
                    dealerCount = 5;
                } else {
                    playerCount = 5;
                }


                return ContextCompat.getDrawable(this, R.drawable.five_red_arrow);
            case 33:
                if (dealerCard) {
                    dealerCount = 6;
                } else {
                    playerCount = 6;
                }


                return ContextCompat.getDrawable(this, R.drawable.six_red_arrow);
            case 34:
                if (dealerCard) {
                    dealerCount = 7;
                } else {
                    playerCount = 7;
                }


                return ContextCompat.getDrawable(this, R.drawable.seven_red_arrow);
            case 35:
                if (dealerCard) {
                    dealerCount = 8;
                } else {
                    playerCount = 8;
                }


                return ContextCompat.getDrawable(this, R.drawable.eight_red_arrow);
            case 36:
                if (dealerCard) {
                    dealerCount = 9;
                } else {
                    playerCount = 9;
                }

                return ContextCompat.getDrawable(this, R.drawable.nine_red_arrow);
            case 37:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.ten_red_arrow);
            case 38:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.vallet_red_arrow);
            case 39:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.queen_red_arrow);
            case 40:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.king_red_arrow);
            case 41:
                if (dealerCard) {
                    if (dealerSumCount + 11 > blackJack) {
                        dealerCount = 1;
                    } else {
                        dealerCount = 11;
                    }
                } else {
                    if (playerSumCount + 11 > blackJack) {
                        playerCount = 1;
                    } else {
                        playerCount = 11;
                    }
                }


                return ContextCompat.getDrawable(this, R.drawable.ace_black_arrow);
            case 42:
                if (dealerCard) {
                    dealerCount = 2;
                } else {
                    playerCount = 2;
                }


                return ContextCompat.getDrawable(this, R.drawable.two_black_arrow);
            case 43:
                if (dealerCard) {
                    dealerCount = 3;
                } else {
                    playerCount = 3;
                }


                return ContextCompat.getDrawable(this, R.drawable.three_black_arrow);
            case 44:
                if (dealerCard) {
                    dealerCount = 4;
                } else {
                    playerCount = 4;
                }


                return ContextCompat.getDrawable(this, R.drawable.four_black_arrow);
            case 45:
                if (dealerCard) {
                    dealerCount = 5;
                } else {
                    playerCount = 5;
                }


                return ContextCompat.getDrawable(this, R.drawable.five_black_arrow);
            case 46:
                if (dealerCard) {
                    dealerCount = 6;
                } else {
                    playerCount = 6;
                }


                return ContextCompat.getDrawable(this, R.drawable.six_black_arrow);
            case 47:
                if (dealerCard) {
                    dealerCount = 7;
                } else {
                    playerCount = 7;
                }


                return ContextCompat.getDrawable(this, R.drawable.seven_black_arrow);
            case 48:
                if (dealerCard) {
                    dealerCount = 8;
                } else {
                    playerCount = 8;
                }


                return ContextCompat.getDrawable(this, R.drawable.eight_black_arrow);
            case 49:
                if (dealerCard) {
                    dealerCount = 9;
                } else {
                    playerCount = 9;
                }

                return ContextCompat.getDrawable(this, R.drawable.nine_black_arrow);
            case 50:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.ten_black_arrow);
            case 51:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.vallet_black_arrow);
            case 52:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.queen_black_arrow);
            default:
                if (dealerCard) {
                    dealerCount = 10;
                } else {
                    playerCount = 10;
                }


                return ContextCompat.getDrawable(this, R.drawable.king_black_arrow);
        }

    }

    private void checkBlackJackAndBurn() {
        //Meaning the player got 2 cards,so we need to check for the blackjack and we shall check if the player had burn while taking the cards
        if (playerSumCount > blackJack) {
            //the game has ended for the player
            playerScore.setText(getString(R.string.player_hand, playerSumCount));
            changeButtons(false);
            User.get().setCoins(User.get().getCoins() - maximumPot);
            coinsTV.setText(getString(R.string.coinsText, User.get().getCoins()));
            Snackbar.make(dealerLayout, getString(R.string.you_lost), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.restart), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restartGame();
                }
            }).show();
        } else {
            playerScore.setText(getString(R.string.player_hand, playerSumCount));
            if (maxCardsExceeded()) {
                playerScore.setText(getString(R.string.player_hand, playerSumCount));
                openDealersCard();

            } else {
                if (playerSumCount == blackJack) {
                    Snackbar.make(dealerLayout, getString(R.string.blackjack_hit), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                }
            }
        }

    }

    private void restartGame() {
        endGame();
        initCoinsPot();
    }

    private void endGame() {
        playerCountArray.clear();
        playerSumCount = 0;
        playerCount = 0;
        dealerCountArray.clear();
        dealerCount = 0;
        dealerSumCount = 0;
        dealersHiddenImageView.clear();
        dealersHiddenCard.clear();
        dealerLayout.removeAllViews();
        dealerLayout.invalidate();
        playerLayout.removeAllViews();
        playerLayout.invalidate();
    }

    private boolean maxCardsExceeded() {
        return playerCountArray.size() >= maxCards;
    }

}
