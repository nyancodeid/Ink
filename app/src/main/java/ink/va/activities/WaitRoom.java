package ink.va.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ink.va.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.omegle.Omegle;
import ink.omegle.core.OmegleException;
import ink.omegle.core.OmegleMode;
import ink.omegle.core.OmegleSession;
import ink.omegle.core.OmegleSpyStranger;
import ink.omegle.event.OmegleEventAdaptor;
import ink.va.adapters.RandomChatAdapter;
import ink.va.callbacks.GeneralCallback;
import ink.va.models.RandomChatModel;
import ink.va.service.TaskRemoveService;
import ink.va.utils.InputField;
import ink.va.utils.SharedHelper;

import static ink.omegle.core.OmegleMode.NORMAL;
import static ink.omegle.core.OmegleMode.SPY_QUESTION;


public class WaitRoom extends BaseActivity {

    private static final String TAG = WaitRoom.class.getSimpleName();
    @Bind(R.id.chatRouletteRecycler)
    RecyclerView chatRouletteRecycler;
    @Bind(R.id.chatRouletteMessageBody)
    EditText chatRouletteMessageBody;
    @Bind(R.id.chatRouletteSendMessage)
    FloatingActionButton chatRouletteSendMessage;
    @Bind(R.id.connectDisconnectButton)
    Button connectDisconnectButton;
    @Bind(R.id.actualStatus)
    TextView actualStatus;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.chosenTypeSpinner)
    AppCompatSpinner chosenTypeSpinner;

    private SharedHelper sharedHelper;
    private RandomChatAdapter chatAdapter;
    private RandomChatModel chatModel;
    private List<RandomChatModel> chatModels;
    private boolean mIsDisconnected = false;
    private Omegle omegle;
    private boolean connected;
    private List<String> types;
    private String chosenType;
    private Thread mWorkerThread;
    private Thread mSendThread;
    private OmegleSession omegleSession;
    private Thread mDisconnectThread;
    private String question;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chat_background_png));
        setContentView(R.layout.activity_wait_room);
        ButterKnife.bind(this);


        chosenType = getString(R.string.normalOmegeleType);
        sharedHelper = new SharedHelper(this);
        chatModels = new ArrayList<>();
        chatAdapter = new RandomChatAdapter(chatModels, this);
        omegle = new Omegle();
        startService(new Intent(this, TaskRemoveService.class));

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        chatRouletteRecycler.setLayoutManager(linearLayoutManager);
        chatRouletteRecycler.setItemAnimator(itemAnimator);
        chatRouletteRecycler.setAdapter(chatAdapter);
        chatRouletteMessageBody.setHint(getString(R.string.waitingToFindOpponent));
        chatRouletteSendMessage.setEnabled(false);

        types = new ArrayList<>();
        types.add(getString(R.string.normalOmegeleType));
        types.add(getString(R.string.spyOmegleMode));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
        chosenTypeSpinner.setAdapter(arrayAdapter);
        chosenTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                chosenType = types.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        chatRouletteRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    hideKeyboard();
                }
            }
        });


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.waitRoom));
        }
        chatRouletteMessageBody.setHint(getString(R.string.waitingToFindOpponent));
        chatRouletteMessageBody.setEnabled(false);
        chatRouletteMessageBody.setHint(getString(R.string.waitingToFindOpponent));


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

    private void showQuestionPicker(final GeneralCallback<String> questionCallback) {
        InputField.createInputFieldView(this, new InputField.ClickHandler() {
            @Override
            public void onPositiveClicked(Object... result) {
                String writtenMessage = (String) result[0];
                AlertDialog dialog = (AlertDialog) result[1];
                dialog.dismiss();
                question = writtenMessage;
                chosenTypeSpinner.setSelection(1);
                questionCallback.onSuccess(question);
            }

            @Override
            public void onNegativeClicked(Object... result) {
                AlertDialog dialog = (AlertDialog) result[1];
                dialog.dismiss();
                chosenTypeSpinner.setSelection(0);
                questionCallback.onFailure(null);
            }
        }, getString(R.string.questionToAsk), true, 10);
    }


    @OnClick(R.id.connectDisconnectButton)
    public void connectClicked() {
        if (connected) {
            connected = false;
            mDisconnectThread = null;
            mDisconnectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        omegleSession.disconnect();
                    } catch (OmegleException e) {
                        e.printStackTrace();
                    }
                }
            });
            mDisconnectThread.start();

            connectDisconnectButton.setText(getString(R.string.connect));
        } else {
            if (chosenType.equals(getString(R.string.normalOmegeleType))) {
                connected = true;
                startOmegle(NORMAL, null);
                connectDisconnectButton.setText(getString(R.string.disconnect));
            } else {
                showQuestionPicker(new GeneralCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        connected = true;
                        startOmegle(SPY_QUESTION, s);
                        connectDisconnectButton.setText(getString(R.string.disconnect));
                    }

                    @Override
                    public void onFailure(String s) {

                    }
                });
            }

        }
    }

    private void startOmegle(final OmegleMode omegleMode, @Nullable final String question) {
        if (mWorkerThread != null) {
            mWorkerThread = null;
        }

        chatModels.clear();
        chatAdapter.notifyDataSetChanged();
        chosenTypeSpinner.setEnabled(false);
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                switch (omegleMode) {
                    case NORMAL:
                        try {
                            omegleSession = omegle.openSession(NORMAL, new OmegleEventAdaptor() {
                                @Override
                                public void chatWaiting(OmegleSession session) {
                                    super.chatWaiting(session);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.VISIBLE);
                                            actualStatus.setTextColor(ContextCompat.getColor(WaitRoom.this, R.color.colorPrimary));
                                            actualStatus.setText(getString(R.string.waitingForOpponents));
                                        }
                                    });

                                }

                                @Override
                                public void chatConnected(OmegleSession session) {
                                    super.chatConnected(session);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.GONE);
                                            actualStatus.setText(getString(R.string.connectedToOpponent));
                                            chatRouletteMessageBody.setHint(getString(R.string.writeMessageHint));
                                            chatRouletteMessageBody.setEnabled(true);
                                        }
                                    });
                                }

                                @Override
                                public void chatMessage(OmegleSession session, final String message) {
                                    super.chatMessage(session, message);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            scrollToBottom();
                                            RandomChatModel randomChatModel = new RandomChatModel(message, false);
                                            chatModels.add(randomChatModel);
                                            int index = chatModels.indexOf(randomChatModel);
                                            chatAdapter.notifyItemInserted(index);
                                        }
                                    });
                                }

                                @Override
                                public void strangerDisconnected(OmegleSession session) {
                                    super.strangerDisconnected(session);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideKeyboard();
                                            chosenTypeSpinner.setEnabled(true);
                                            chatRouletteMessageBody.setHint(getString(R.string.waitingToFindOpponent));
                                            chatRouletteMessageBody.setEnabled(false);
                                            connected = false;
                                            connectDisconnectButton.setText(getString(R.string.connect));
                                            actualStatus.setTextColor(ContextCompat.getColor(WaitRoom.this, R.color.red));
                                            actualStatus.setText(getString(R.string.disconnectedToOpponent));
                                        }
                                    });
                                }

                                @Override
                                public void strangerTyping(OmegleSession session) {
                                    super.strangerTyping(session);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
//                                            actualStatus.setText(getString(R.string.typing));
                                        }
                                    });
                                }

                                @Override
                                public void strangerStoppedTyping(OmegleSession session) {
                                    super.strangerStoppedTyping(session);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            actualStatus.setText(getString(R.string.connectedToOpponent));
                                        }
                                    });
                                }

                                @Override
                                public void recaptchaRequired(OmegleSession session, Map<String, Object> variables) {
                                    super.recaptchaRequired(session, variables);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                }

                                @Override
                                public void recaptchaRejected(OmegleSession session, Map<String, Object> variables) {
                                    super.recaptchaRejected(session, variables);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                }

                                @Override
                                public void spyMessage(OmegleSession session, OmegleSpyStranger stranger, String message) {
                                    super.spyMessage(session, stranger, message);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                }

                                @Override
                                public void spyTyping(OmegleSession session, OmegleSpyStranger stranger) {
                                    super.spyTyping(session, stranger);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                }

                                @Override
                                public void spyStoppedTyping(OmegleSession session, OmegleSpyStranger stranger) {
                                    super.spyStoppedTyping(session, stranger);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                }

                                @Override
                                public void spyDisconnected(OmegleSession session, OmegleSpyStranger stranger) {
                                    super.spyDisconnected(session, stranger);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                }

                                @Override
                                public void question(OmegleSession session, String question) {
                                    super.question(session, question);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                        }
                                    });
                                }

                                @Override
                                public void omegleError(OmegleSession session, String string) {
                                    super.omegleError(session, string);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    hideKeyboard();
                                                    chosenTypeSpinner.setEnabled(true);
                                                    chatRouletteMessageBody.setHint(getString(R.string.waitingToFindOpponent));
                                                    chatRouletteMessageBody.setEnabled(false);
                                                    connected = false;
                                                    connectDisconnectButton.setText(getString(R.string.connect));
                                                    actualStatus.setTextColor(ContextCompat.getColor(WaitRoom.this, R.color.red));
                                                    actualStatus.setText(getString(R.string.disconnectedToOpponent));
                                                }
                                            });
                                        }
                                    });
                                }

                                @Override
                                public void messageSent(OmegleSession session, final String string) {
                                    super.messageSent(session, string);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            scrollToBottom();
                                            mSendThread = null;
                                            progressBar.setVisibility(View.GONE);
                                            chatRouletteMessageBody.setEnabled(true);
                                            chatRouletteMessageBody.setText("");
                                            RandomChatModel randomChatModel = new RandomChatModel(string, true);
                                            chatModels.add(randomChatModel);
                                            int index = chatModels.indexOf(randomChatModel);
                                            chatAdapter.notifyItemInserted(index);
                                        }
                                    });
                                }

                                @Override
                                public void chatDisconnected(OmegleSession session) {
                                    super.chatDisconnected(session);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideKeyboard();
                                            chosenTypeSpinner.setEnabled(true);
                                            chatRouletteMessageBody.setHint(getString(R.string.waitingToFindOpponent));
                                            chatRouletteMessageBody.setEnabled(false);
                                            connected = false;
                                            connectDisconnectButton.setText(getString(R.string.connect));
                                            actualStatus.setTextColor(ContextCompat.getColor(WaitRoom.this, R.color.red));
                                            actualStatus.setText(getString(R.string.disconnectedToOpponent));
                                        }
                                    });
                                }
                            });
                        } catch (OmegleException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SPY_QUESTION:
                        try {
                            omegleSession = omegle.openSession(OmegleMode.SPY_QUESTION, question, new OmegleEventAdaptor() {
                                @Override
                                public void chatWaiting(OmegleSession session) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.VISIBLE);
                                            actualStatus.setTextColor(ContextCompat.getColor(WaitRoom.this, R.color.colorPrimary));
                                            actualStatus.setText(getString(R.string.waitingForOpponents));
                                        }
                                    });
                                }

                                @Override
                                public void chatConnected(OmegleSession session) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.GONE);
                                            actualStatus.setTextColor(ContextCompat.getColor(WaitRoom.this, R.color.colorPrimary));
                                            actualStatus.setText(getString(R.string.youWatchingNow) + " " + question);
                                        }
                                    });
                                }

                                @Override
                                public void spyMessage(OmegleSession session, final OmegleSpyStranger stranger, final String message) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (stranger) {
                                                case Stranger_1:
                                                    chatModel = new RandomChatModel(message, true);
                                                    chatModels.add(chatModel);
                                                    int index = chatModels.indexOf(chatModel);
                                                    chatAdapter.notifyItemInserted(index);
                                                    break;
                                                case Stranger_2:
                                                    chatModel = new RandomChatModel(message, false);
                                                    chatModels.add(chatModel);
                                                    index = chatModels.indexOf(chatModel);
                                                    chatAdapter.notifyItemInserted(index);
                                                    break;
                                            }
                                        }
                                    });

                                }

                                @Override
                                public void chatDisconnected(OmegleSession session) {
                                    super.chatDisconnected(session);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideKeyboard();
                                            chosenTypeSpinner.setEnabled(true);
                                            chatRouletteMessageBody.setHint(getString(R.string.waitingToFindOpponent));
                                            chatRouletteMessageBody.setEnabled(false);
                                            connected = false;
                                            connectDisconnectButton.setText(getString(R.string.connect));
                                            actualStatus.setTextColor(ContextCompat.getColor(WaitRoom.this, R.color.red));
                                            actualStatus.setText(getString(R.string.disconnectedToOpponent));
                                        }
                                    });
                                }

                                @Override
                                public void spyDisconnected(OmegleSession session, OmegleSpyStranger stranger) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideKeyboard();
                                            chosenTypeSpinner.setEnabled(true);
                                            chatRouletteMessageBody.setHint(getString(R.string.waitingToFindOpponent));
                                            chatRouletteMessageBody.setEnabled(false);
                                            connected = false;
                                            connectDisconnectButton.setText(getString(R.string.connect));
                                            actualStatus.setTextColor(ContextCompat.getColor(WaitRoom.this, R.color.red));
                                            actualStatus.setText(getString(R.string.disconnectedToOpponent));
                                        }
                                    });
                                }

                                @Override
                                public void question(OmegleSession session, String question) {

                                }

                                @Override
                                public void omegleError(OmegleSession session, String string) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideKeyboard();
                                            chosenTypeSpinner.setEnabled(true);
                                            chatRouletteMessageBody.setHint(getString(R.string.waitingToFindOpponent));
                                            chatRouletteMessageBody.setEnabled(false);
                                            connected = false;
                                            connectDisconnectButton.setText(getString(R.string.connect));
                                            actualStatus.setTextColor(ContextCompat.getColor(WaitRoom.this, R.color.red));
                                            actualStatus.setText(getString(R.string.disconnectedToOpponent));
                                        }
                                    });

                                }
                            });
                        } catch (OmegleException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
        mWorkerThread.start();

    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(chatRouletteRecycler.getWindowToken(), 0);

    }

    @OnClick(R.id.chatRouletteSendMessage)
    public void chatRouletteSendMessage() {
        sendMessage();
    }

    private void sendMessage() {
        chatRouletteMessageBody.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        mSendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    omegleSession.send(chatRouletteMessageBody.getText().toString());
                } catch (OmegleException e) {

                    e.printStackTrace();
                }
            }
        });
        mSendThread.start();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        if (mIsDisconnected) {
            disconnectFromOpponent();
        }
        super.onResume();
    }

    private void disconnectFromOpponent() {

    }


    private void scrollToBottom() {
        chatRouletteRecycler.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                chatRouletteRecycler.smoothScrollToPosition(chatAdapter.getItemCount());
            }
        });
    }


}
