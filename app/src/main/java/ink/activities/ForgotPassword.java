package ink.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ink.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ForgotPassword extends AppCompatActivity {
    @Bind(R.id.resetPasswordText)
    TextView resetPasswordText;
    @Bind(R.id.loginContainer)
    LinearLayout loginContainer;
    @Bind(R.id.loginField)
    EditText loginField;
    @Bind(R.id.securityQuestionContainer)
    LinearLayout securityQuestionContainer;
    @Bind(R.id.securityQuestionField)
    EditText securityQuestionField;
    @Bind(R.id.questionHolder)
    TextView questionHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.proceed)
    public void proceed() {

    }

    @OnClick(R.id.submit)
    public void submit() {

    }
}
