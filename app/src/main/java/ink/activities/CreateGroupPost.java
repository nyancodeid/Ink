package ink.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ink.R;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.utils.CircleTransform;
import ink.utils.Constants;
import ink.utils.SharedHelper;

public class CreateGroupPost extends AppCompatActivity {

    @Bind(R.id.currentUserName)
    TextView currentUserName;
    @Bind(R.id.currentUserImage)
    ImageView currentUserImage;
    @Bind(R.id.groupInputField)
    EditText groupInputField;
    @Bind(R.id.sendGroupMessage)
    FloatingActionButton sendGroupMessageIcon;

    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_post);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        if (!sharedHelper.getImageLink().isEmpty()) {
            Picasso.with(this).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + sharedHelper.getImageLink()).
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
        Toast.makeText(CreateGroupPost.this, "hello", Toast.LENGTH_SHORT).show();
    }
}
