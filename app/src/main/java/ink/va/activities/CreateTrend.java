package ink.va.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.utils.DialogUtils;
import ink.va.utils.ErrorCause;
import ink.va.utils.Keyboard;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.fragments.WhatsTrending.UPDATE_TRENDS;

public class CreateTrend extends BaseActivity {

    private List<String> trendCategories;
    @BindView(R.id.autoCompleteTrendCategoriesTV)
    AutoCompleteTextView autoCompleteTrendCategoriesTV;
    @BindView(R.id.isPremiumTV)
    TextView isPremiumTV;
    @BindView(R.id.trendNoticeTV)
    TextView trendNoticeTV;
    @BindView(R.id.finalPriceTV)
    TextView finalPriceTV;

    @BindView(R.id.isPremiumSwitch)
    Switch isPremiumSwitch;

    @BindView(R.id.trendExternalED)
    EditText trendExternalED;
    @BindView(R.id.trendImageUrlED)
    EditText trendImageUrlED;
    @BindView(R.id.trendContentED)
    EditText trendContentED;
    @BindView(R.id.trendTitleED)
    EditText trendTitleED;
    private ProgressDialog progressDialog;

    private int advertisementPrice;
    private int topTrendPrice;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trend);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.connecting));
        progressDialog.setMessage(getString(R.string.loadingText));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        trendCategories = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trendCategories = extras.getStringArrayList("trendCategories");
            advertisementPrice = Integer.valueOf(extras.getString("advertisementPrice"));
            topTrendPrice = Integer.valueOf(extras.getString("topTrendPrice"));

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, trendCategories);
            autoCompleteTrendCategoriesTV.setAdapter(adapter);
        }
        isPremiumTV.setText(getString(R.string.isPremiumText, Integer.valueOf(topTrendPrice)));
        trendNoticeTV.setText(getString(R.string.createTrendNotice, Integer.valueOf(advertisementPrice)));
        updatePricingMessage(advertisementPrice);
        isPremiumSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    updatePricingMessage(advertisementPrice + topTrendPrice);
                } else {
                    updatePricingMessage(advertisementPrice);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.createTrend)
    public void createTrendClicked() {
        checkBeforeAttempt();
    }

    private void checkBeforeAttempt() {
        boolean canProceed = true;

        if (trendExternalED.getText().toString().trim().isEmpty()) {
            trendExternalED.setError(getString(R.string.emptyField));
            canProceed = false;
        }

        if (trendImageUrlED.getText().toString().trim().isEmpty()) {
            trendImageUrlED.setError(getString(R.string.emptyField));
            canProceed = false;
        }

        if (trendContentED.getText().toString().trim().isEmpty()) {
            trendContentED.setError(getString(R.string.emptyField));
            canProceed = false;
        }

        if (trendTitleED.getText().toString().trim().isEmpty()) {
            trendTitleED.setError(getString(R.string.emptyField));
            canProceed = false;
        }

        if (autoCompleteTrendCategoriesTV.getText().toString().trim().isEmpty()) {
            autoCompleteTrendCategoriesTV.setError(getString(R.string.emptyField));
            canProceed = false;
        }

        if (canProceed) {
            createTrend();
        }
    }

    private void createTrend() {
        String imageUrl = trendImageUrlED.getText().toString().trim();
        progressDialog.show();
        checkImageUrl(imageUrl);
    }

    private void checkImageUrl(final String imageUrl) {
        progressDialog.setTitle(getString(R.string.checking));
        progressDialog.setMessage(getString(R.string.checkingImage));

        Ion.with(this).load(imageUrl).asBitmap().setCallback(new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(Exception e, Bitmap result) {
                if (e != null) {
                    progressDialog.dismiss();
                    DialogUtils.showDialog(CreateTrend.this, getString(R.string.error), getString(R.string.urlError), true, null, false, null);
                } else {
                    callCreateTrend(imageUrl);
                }
            }
        });
    }

    private void callCreateTrend(final String imageUrl) {
        progressDialog.setTitle(getString(R.string.creating));
        progressDialog.setMessage(getString(R.string.creatingTrend));
        Keyboard.hideKeyboard(this);
        Retrofit.getInstance().getInkService().addAdvertisement(trendTitleED.getText().toString().trim(), trendContentED.getText().toString().trim(),
                imageUrl, trendExternalED.getText().toString().trim(), autoCompleteTrendCategoriesTV.getText().toString().trim(), isPremiumSwitch.isChecked(),
                sharedHelper.getUserId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response == null) {
                            callCreateTrend(imageUrl);
                            return;
                        }

                        if (response.body() == null) {
                            callCreateTrend(imageUrl);
                            return;
                        }
                        progressDialog.dismiss();
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            boolean success = jsonObject.optBoolean("success");
                            if (success) {
                                DialogUtils.showDialog(CreateTrend.this, getString(R.string.success), getString(R.string.trendCreated), false, new DialogUtils.DialogListener() {
                                    @Override
                                    public void onNegativeClicked() {

                                    }

                                    @Override
                                    public void onDialogDismissed() {

                                    }

                                    @Override
                                    public void onPositiveClicked() {
                                        setResult(UPDATE_TRENDS);
                                        finish();
                                    }
                                }, false, null);
                            } else {
                                String cause = jsonObject.optString("cause");
                                if (cause.equals(ErrorCause.NOT_ENOUGH_COINS)) {
                                    DialogUtils.showDialog(CreateTrend.this, getString(R.string.error), getString(R.string.not_enough_coins), true, null, false, null);
                                } else {
                                    DialogUtils.showDialog(CreateTrend.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
                                }
                            }
                        } catch (IOException e) {
                            DialogUtils.showDialog(CreateTrend.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
                            e.printStackTrace();
                        } catch (JSONException e) {
                            DialogUtils.showDialog(CreateTrend.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        progressDialog.dismiss();
                        DialogUtils.showDialog(CreateTrend.this, getString(R.string.error), getString(R.string.serverErrorText), true, null, false, null);
                    }
                });
    }

    private void updatePricingMessage(int finalPrice) {
        finalPriceTV.setText(getString(R.string.finalPriceText, finalPrice));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
