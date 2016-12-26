package ink.va.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.activities.MyProfile;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-12-23.
 */

public class FragmentDialog extends DialogFragment implements DialogUtils.DialogListener {

    public static final int ORDER_TYPE_HIDE_PROFILE = 0;
    public static final int ORDER_TYPE_INCOGNITO = 1;
    private ResultListener resultListener;
    private String firstParagraphContent;
    private String secondParagraphContent;
    private int backgroundResource;

    @Bind(R.id.fragmentDialogRoot)
    RelativeLayout fragmentDialogRoot;

    @Bind(R.id.firstParagraphTV)
    TextView firstParagraphTV;

    @Bind(R.id.secondParagraphTV)
    TextView secondParagraphTV;

    @Bind(R.id.doIt)
    Button doIt;

    @Bind(R.id.close)
    Button close;

    @Bind(R.id.progress)
    View progress;

    private int userCoins;
    private int orderType;
    private int orderCost;
    private SharedHelper sharedHelper;
    private Context context;
    private int lastOrderType;


    static FragmentDialog newInstance(int backgroundResource,
                                      String firstParagraphContent,
                                      String secondParagraphContent, @OrderType int orderType,
                                      int userCoins, int orderCost) {
        FragmentDialog fragmentDialog = new FragmentDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("resource", backgroundResource);
        bundle.putInt("userCoins", userCoins);
        bundle.putString("secondParagraphContent", secondParagraphContent);
        bundle.putString("firstParagraphContent", firstParagraphContent);
        bundle.putInt("orderType", orderType);
        bundle.putInt("orderCost", orderCost);
        fragmentDialog.setArguments(bundle);
        return fragmentDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog, container, false);

        ButterKnife.bind(this, view);

        backgroundResource = getArguments().getInt("resource");
        firstParagraphContent = getArguments().getString("firstParagraphContent");
        secondParagraphContent = getArguments().getString("secondParagraphContent");
        orderType = getArguments().getInt("orderType");
        userCoins = getArguments().getInt("userCoins");
        orderCost = getArguments().getInt("orderCost");

        fragmentDialogRoot.setBackgroundResource(backgroundResource);
        firstParagraphTV.setText(firstParagraphContent);
        secondParagraphTV.setText(secondParagraphContent);
        setCancelable(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public FragmentDialog showDialog(FragmentManager fragmentManager,
                                     String firstParagraphContent,
                                     String secondParagraphContent,
                                     int backgroundResource, int userCoins,
                                     @OrderType int orderType, int orderCost) {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag("dialog");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = FragmentDialog.newInstance(backgroundResource,
                firstParagraphContent, secondParagraphContent, orderType, userCoins, orderCost);
        newFragment.show(fragmentTransaction, "dialog");
        return this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Instantiate the EditNameDialogListener so we can send events to the host
            resultListener = (MyProfile) context;
            FragmentDialog.this.context = context;
            sharedHelper = new SharedHelper(context);
        } catch (ClassCastException e) {
        }
    }

    public void hideDialog(boolean success, @OrderType int orderType) {
        if (resultListener != null) {
            resultListener.onDialogClosed();
            if (success) {
                resultListener.onResultSuccess(lastOrderType);
            }
        }
        dismiss();

    }

    @OnClick(R.id.close)
    public void closeClicked() {
        hideDialog(false, lastOrderType);
    }

    @OnClick(R.id.doIt)
    public void doItClicked() {
        if (userCoins < orderCost) {
            Snackbar.make(firstParagraphTV, getString(R.string.not_enough_coins), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            }).show();
        } else {
            completeOrder();
        }
    }

    private void completeOrder() {
        progress.setVisibility(View.VISIBLE);
        disableButtons();
        Call<ResponseBody> responseBodyCall;
        switch (orderType) {
            case ORDER_TYPE_HIDE_PROFILE:
                lastOrderType = ORDER_TYPE_HIDE_PROFILE;
                responseBodyCall = Retrofit.getInstance().getInkService().changeProfile(
                        MyProfile.TYPE_MAKE_PROFILE_HIDDEN, sharedHelper.getUserId());
                break;
            case ORDER_TYPE_INCOGNITO:
                lastOrderType = ORDER_TYPE_INCOGNITO;
                responseBodyCall = Retrofit.getInstance().getInkService().changeProfile(
                        MyProfile.TYPE_GO_INCOGNITO, sharedHelper.getUserId());
                break;
            default:
                lastOrderType = ORDER_TYPE_INCOGNITO;
                responseBodyCall = Retrofit.getInstance().getInkService().changeProfile(
                        MyProfile.TYPE_GO_INCOGNITO, sharedHelper.getUserId());
        }
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    completeOrder();
                    return;
                }
                if (response.body() == null) {
                    completeOrder();
                    return;
                }
                enableButtons();
                progress.setVisibility(View.GONE);
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        DialogUtils.showDialog(context, getString(R.string.success), getString(R.string.order_bought),
                                true, FragmentDialog.this, false, null);
                    } else {
                        DialogUtils.showDialog(context, getString(R.string.error), getString(R.string.orderError),
                                true, null, false, null);
                        hideDialog(false, lastOrderType);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    DialogUtils.showDialog(context, getString(R.string.error), getString(R.string.serverErrorText),
                            true, null, false, null);
                } catch (JSONException e) {
                    DialogUtils.showDialog(context, getString(R.string.error), getString(R.string.serverErrorText),
                            true, null, false, null);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DialogUtils.showDialog(context, getString(R.string.error), getString(R.string.serverErrorText),
                        true, null, false, null);
            }
        });
    }

    @Override
    public void onNegativeClicked() {

    }

    @Override
    public void onDialogDismissed() {
        hideDialog(true, lastOrderType);
    }

    @Override
    public void onPositiveClicked() {
        hideDialog(true, lastOrderType);
    }


    public interface ResultListener {
        void onResultSuccess(@OrderType int orderBought);

        void onDialogClosed();
    }


    @IntDef({ORDER_TYPE_HIDE_PROFILE, ORDER_TYPE_INCOGNITO})
    public @interface OrderType {

    }

    private void disableButtons() {
        doIt.setEnabled(false);
        doIt.setClickable(false);
        close.setClickable(false);
        close.setEnabled(false);
    }

    private void enableButtons() {
        doIt.setEnabled(true);
        doIt.setClickable(true);
        close.setClickable(true);
        close.setEnabled(true);
    }

}
