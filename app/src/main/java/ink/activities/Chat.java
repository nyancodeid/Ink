package ink.activities;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.ink.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Chat extends AppCompatActivity {

    @Bind(R.id.sendChatMessage)
    fab.FloatingActionButton mSendChatMessage;
    @Bind(R.id.writeEditText)
    EditText mWriteEditText;
    @Bind(R.id.writeEditTextInputter)
    TextInputLayout mWriteEditTextInputter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String firstName = bundle.getString("firstName");
            //action bar set ups.
            if (actionBar != null) {
                actionBar.setTitle(firstName);
            }
        }
        //action bar set ups
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSendChatMessage.setEnabled(false);
        mWriteEditText.addTextChangedListener(chatTextWatcher);
        mWriteEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mWriteEditTextInputter.setHint(getString(R.string.writing));
                } else {
                    mWriteEditTextInputter.setHint(getString(R.string.writeMessageHint));
                }
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private TextWatcher chatTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() <= 0) {
                mSendChatMessage.setEnabled(false);
            } else {
                mSendChatMessage.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
