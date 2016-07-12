package ink.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.ink.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.utils.SharedHelper;

public class WaitRoom extends AppCompatActivity {

    @Bind(R.id.chatRouletteRecycler)
    RecyclerView chatRouletteRecycler;
    @Bind(R.id.chatRouletteMessageBody)
    EditText chatRouletteMessageBody;
    @Bind(R.id.chatRouletteSendMessage)
    FloatingActionButton chatRouletteSendMessage;
    @Bind(R.id.connectDisconnectButton)
    FloatingActionButton connectDisconnectButton;
    @Bind(R.id.actualStatus)
    TextView actualStatus;
    private View menuItem;

    private ShowcaseView.Builder showcaseViewBuilder;
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_room);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.waitRoom));
        }
        chatRouletteSendMessage.setEnabled(false);
        chatRouletteMessageBody.setEnabled(false);

        if (sharedHelper.shouldShowShowCase()) {
            showTutorial();
        }


        chatRouletteMessageBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    chatRouletteSendMessage.setEnabled(false);
                } else {
                    chatRouletteSendMessage.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void showTutorial() {
        showcaseViewBuilder = new ShowcaseView.Builder(this);
        showcaseViewBuilder.withMaterialShowcase();
        showcaseViewBuilder.setTarget(new ViewTarget(actualStatus));
        showcaseViewBuilder.setContentTitle(getString(R.string.statustChanageTitle));
        showcaseViewBuilder.setContentText(getString(R.string.statusBrief));
        showcaseViewBuilder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showcaseViewBuilder.setTarget(new ViewTarget(connectDisconnectButton));
                showcaseViewBuilder.setContentTitle(getString(R.string.connectDisconnectTitle));
                showcaseViewBuilder.setContentText(getString(R.string.connectDisconnectBrief));
                showcaseViewBuilder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showcaseViewBuilder.setTarget(new ViewTarget(chatRouletteMessageBody));
                        showcaseViewBuilder.setContentTitle(getString(R.string.messageBodyTitle));
                        showcaseViewBuilder.setContentText(getString(R.string.messageBodyBrief));
                        showcaseViewBuilder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showcaseViewBuilder.setTarget(new ViewTarget(chatRouletteSendMessage));
                                showcaseViewBuilder.setContentTitle(getString(R.string.sendMessageTitle));
                                showcaseViewBuilder.setContentText(getString(R.string.sendMessageBrief));
                                showcaseViewBuilder.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showcaseViewBuilder.setTarget(new ViewTarget(chatRouletteRecycler));
                                        showcaseViewBuilder.setContentTitle(getString(R.string.messagesPlaceTitle));
                                        showcaseViewBuilder.setContentText(getString(R.string.messagesPlaceBrief));
                                        showcaseViewBuilder.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                showcaseViewBuilder.setTarget(new ViewTarget(chatRouletteRecycler));
                                                showcaseViewBuilder.setContentTitle(getString(R.string.endingTitle));
                                                showcaseViewBuilder.setContentText(getString(R.string.endingBrief));
                                                showcaseViewBuilder.setOnClickListener(null);
                                                sharedHelper.setShouldShowShowCase(false);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        showcaseViewBuilder.build();
    }


    @OnClick(R.id.chatRouletteSendMessage)
    public void chatRouletteSendMessage() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.connectDisconnectButton)
    public void connectDisconnectButton() {
        if (connectDisconnectButton.getTag().equals(getString(R.string.connect))) {
            connectDisconnectButton.setTag(getString(R.string.disconnect));
            connectDisconnectButton.setImageResource(R.drawable.disconnect_icon);
        } else {
            connectDisconnectButton.setTag(getString(R.string.connect));
            connectDisconnectButton.setImageResource(R.drawable.connect_icon);
        }
    }
}
