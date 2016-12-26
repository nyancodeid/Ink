package ink.va.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.va.utils.ErrorCause;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tyrantgit.explosionfield.ExplosionField;

import static ink.va.fragments.Packs.PACK_BUY_RESULT_CODE;
import static ink.va.utils.Constants.PACK_ID_BUNDLE_KEY;

public class PackFullScreen extends BaseActivity {
    private ExplosionField mExplosionField;
    @Bind(R.id.activity_pack_full_screen)
    View rootLayout;
    private Dialog mProgressDialog;
    private String packId;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pack_full_screen);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        Bundle extras = getIntent().getExtras();
        packId = extras.getString(PACK_ID_BUNDLE_KEY);
        initializeDialog();
        mExplosionField = ExplosionField.attach2Window(this);
    }


    private void buy(View view) {
        mExplosionField.explode(view, new ExplosionField.ExplosionAnimationListener() {
            @Override
            public void onAnimationEnd() {
                showProgress();
                openPack(packId);

            }
        });

    }

    private void initializeDialog() {
        mProgressDialog = new Dialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(R.layout.dialog_progress);
        mProgressDialog.setCancelable(false);
        mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
    }

    public void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        });
    }

    private void openPack(final String packId) {
        Call<ResponseBody> packCall = Retrofit.getInstance().getInkService().openPack(sharedHelper.getUserId(), packId);
        packCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    openPack(packId);
                    return;
                }
                if (response.body() == null) {
                    openPack(packId);
                    return;
                }
                try {
                    hideProgress();
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    AlertDialog.Builder builder = new AlertDialog.Builder(PackFullScreen.this);
                    if (success) {
                        String userCoinsLeft = jsonObject.optString("userCoinsLeft");
                        User.get().setCoins(userCoinsLeft);
                        builder.setTitle(getString(R.string.congratulation));
                        builder.setMessage(getString(R.string.gift_bought_message));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                setResult(PACK_BUY_RESULT_CODE);
                                finish();
                                overrideActivityAnimation();
                            }
                        });
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                dialogInterface.dismiss();
                                setResult(PACK_BUY_RESULT_CODE);
                                finish();
                                overrideActivityAnimation();
                            }
                        });
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                dialogInterface.dismiss();
                                setResult(PACK_BUY_RESULT_CODE);
                                finish();
                                overrideActivityAnimation();
                            }
                        });
                        builder.show();
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(ErrorCause.PACK_ALREADY_BOUGHT)) {
                            builder.setTitle(getString(R.string.error));
                            builder.setMessage(getString(R.string.gift_already_bought));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            builder.setTitle(getString(R.string.error));
                            builder.setMessage(getString(R.string.serverErrorText));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    hideProgress();
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideProgress();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }


}
