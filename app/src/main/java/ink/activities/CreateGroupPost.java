package ink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.ink.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupPost extends AppCompatActivity {

    @Bind(R.id.currentUserName)
    TextView currentUserName;
    @Bind(R.id.currentUserImage)
    ImageView currentUserImage;
    @Bind(R.id.groupInputField)
    EditText groupInputField;
    @Bind(R.id.sendGroupMessage)
    FloatingActionButton sendGroupMessageIcon;
    @Bind(R.id.groupMessageSpin)
    SpinKitView groupMessageSpin;
    private String groupId;

    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_post);
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            groupId = bundle.getString("groupId");
        }


        sharedHelper = new SharedHelper(this);
        if (!sharedHelper.getImageLink().isEmpty()) {
            Picasso.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + sharedHelper.getImageLink()).error(R.drawable.image_laoding_error)
                    .placeholder(R.drawable.no_image_yet_state).
                    transform(new CircleTransform()).fit().centerCrop().into(currentUserImage);
        } else {
            Picasso.with(this).load(R.drawable.no_image).
                    transform(new CircleTransform()).fit().centerCrop().into(currentUserImage);
        }
        currentUserName.setText(sharedHelper.getFirstName() + " " + sharedHelper.getLastName());
        sendGroupMessageIcon.setShowAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_scale_up));
        sendGroupMessageIcon.setHideAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_scale_down));
        disableButton();
        groupInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    disableButton();
                } else {
                    enableButtons();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void disableButton() {
        sendGroupMessageIcon.showButtonInMenu(true);
        sendGroupMessageIcon.setEnabled(false);
    }

    private void enableButtons() {
        sendGroupMessageIcon.showButtonInMenu(true);
        sendGroupMessageIcon.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
    }

    @OnClick(R.id.sendGroupMessage)
    public void postGroupMessageWrapper() {
        sendGroupMessage();
    }

    private void sendGroupMessage() {
        Call<ResponseBody> sendGroupMessageCall = Retrofit.getInstance().getInkService().sendGroupMessage(groupId,
                groupInputField.getText().toString().trim(), sharedHelper.getUserId(),
                sharedHelper.getImageLink(), sharedHelper.getFirstName() + " " + sharedHelper.getLastName());
        groupInputField.setText("");
        groupMessageSpin.setVisibility(View.VISIBLE);
        groupInputField.setEnabled(false);
        sendGroupMessageCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    sendGroupMessage();
                    return;
                }
                if (response.body() == null) {
                    sendGroupMessage();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        finish();
                        overridePendingTransition(R.anim.activity_scale_up, R.anim.activity_scale_down);
                        LocalBroadcastManager.getInstance(CreateGroupPost.this).sendBroadcast(new Intent(getPackageName() +
                                "SingleGroupView"));
                    } else {
                        sendGroupMessage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                sendGroupMessage();
            }
        });

    }
}