package ink.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ink.R;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.callbacks.GeneralCallback;
import ink.models.ColorModel;
import ink.utils.Constants;
import ink.utils.ErrorCause;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by USER on 2016-08-30.
 */

public class CustomizeLook extends BaseActivity {

    @Bind(R.id.customizeToolbar)
    Toolbar customizeToolbar;

    @Bind(R.id.customizeFabMenu)
    android.support.design.widget.FloatingActionMenu customizeFabMenu;

    @Bind(R.id.customizeFabMenuText)
    TextView customizeFabMenuText;

    @Bind(R.id.pickNotificationIconColorText)
    TextView pickNotificationIconColorText;

    @Bind(R.id.pickShopIconColorText)
    TextView pickShopIconColorText;

    @Bind(R.id.pickLeftDrawerColorText)
    TextView pickLeftDrawerColorText;

    @Bind(R.id.postBackgroundColorText)
    TextView postBackgroundColorText;

    @Bind(R.id.friendsBackgroundColorText)
    TextView friendsBackgroundColorText;

    @Bind(R.id.messagesBackgroundColorText)
    TextView messagesBackgroundColorText;

    @Bind(R.id.chatBackgroundColorText)
    TextView chatBackgroundColorText;

    @Bind(R.id.requestsBackgroundColorText)
    TextView requestsBackgroundColorText;

    @Bind(R.id.opponentBubbleIcon)
    ImageView opponentBubbleIcon;

    @Bind(R.id.ownBubbleIcon)
    ImageView ownBubbleIcon;

    @Bind(R.id.notificationIcon)
    ImageView notificationIcon;

    @Bind(R.id.shopIcon)
    ImageView shopIcon;

    @Bind(R.id.pickHamburgerIcon)
    ImageView hamburgerIcon;

    @Bind(R.id.ownBubbleCleaner)
    ImageView ownBubbleCleaner;

    @Bind(R.id.opponentBubbleCleaner)
    ImageView opponentBubbleCleaner;

    @Bind(R.id.actionBarCleaner)
    ImageView actionBarCleaner;

    @Bind(R.id.chatFieldCleaner)
    ImageView chatFieldCleaner;

    @Bind(R.id.ownTextCleaner)
    ImageView ownTextCleaner;

    @Bind(R.id.opponentTextCleaner)
    ImageView opponentTextCleaner;

    @Bind(R.id.hamburgerCleaner)
    ImageView hamburgerCleaner;

    @Bind(R.id.statusBarCleaner)
    ImageView statusBarCleaner;

    @Bind(R.id.fabButtonCleaner)
    ImageView fabButtonCleaner;

    @Bind(R.id.notificationCleaner)
    ImageView notificationCleaner;

    @Bind(R.id.shopCleaner)
    ImageView shopCleaner;

    @Bind(R.id.leftPanelCleaner)
    ImageView leftPanelCleaner;

    @Bind(R.id.feedCleaner)
    ImageView feedCleaner;

    @Bind(R.id.sendButtonIcon)
    FloatingActionButton sendButtonIcon;

    @Bind(R.id.sendButtonCleaner)
    ImageView sendButtonCleaner;

    @Bind(R.id.friendsCleaner)
    ImageView friendsCleaner;

    @Bind(R.id.messagesCleaner)
    ImageView messagesCleaner;

    @Bind(R.id.chatCleaner)
    ImageView chatCleaner;

    @Bind(R.id.requestCleaner)
    ImageView requestCleaner;

    private boolean actionBarColorPicked;
    private boolean fabMenuButtonColorPicked;
    private boolean pickNotificationIconColorPicked;
    private boolean pickShopIconColorPicked;
    private boolean pickLeftDrawerColorPicked;
    private boolean feedColorPicked;
    private boolean messagesBackgroundColorPicked;
    private boolean friendsBackgroundColorPicked;
    private boolean chatBackgroundColorPicked;
    private boolean requestBackgroundColorPicked;
    private boolean opponentBubbleColorPicked;
    private boolean ownBubbleColorPicked;
    private boolean hamburgerColorPicked;
    private boolean statusBarColorPicked;
    private boolean sendButtonColorPicked;
    private boolean opponentTextColorPicked;
    private boolean chatFieldColorPicked;
    private boolean ownTextColorPicked;

    private SharedHelper sharedHelper;
    private String oldActionBarColor;
    private String oldMenuButtonColor;
    private String oldNotificationIconColor;
    private String oldShopIconColor;
    private String oldLeftSlidingPanelHeaderColor;
    private String oldFeedColor;
    private String oldFriendsColor;
    private String oldMessagesColor;
    private String oldChatColor;
    private String oldMyRequestColor;
    private String oldOwnBubbleColor;
    private String oldOpponentBubbleColor;
    private String oldHamburgerColor;
    private String oldStatusBarColor;
    private String oldSendButtonColor;
    private String oldChatFieldColor;
    private String oldOwnTextColor;
    private String oldOpponentTextColor;

    private Gson gson;
    private ProgressDialog progressDialog;
    private boolean anythingChanged;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customize_look_view);
        ButterKnife.bind(this);
        gson = new Gson();
        sharedHelper = new SharedHelper(this);
        customizeToolbar = (Toolbar) findViewById(R.id.customizeToolbar);
        setSupportActionBar(customizeToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.actionBar));
        }
        oldOwnBubbleColor = sharedHelper.getOwnBubbleColor();
        oldOpponentBubbleColor = sharedHelper.getOpponentBubbleColor();
        oldActionBarColor = sharedHelper.getActionBarColor();
        oldMenuButtonColor = sharedHelper.getMenuButtonColor();
        oldNotificationIconColor = sharedHelper.getNotificationIconColor();
        oldShopIconColor = sharedHelper.getShopIconColor();
        oldLeftSlidingPanelHeaderColor = sharedHelper.getLeftSlidingPanelHeaderColor();
        oldFeedColor = sharedHelper.getFeedColor();
        oldFriendsColor = sharedHelper.getFriendsColor();
        oldMessagesColor = sharedHelper.getMessagesColor();
        oldChatColor = sharedHelper.getChatColor();
        oldMyRequestColor = sharedHelper.getMyRequestColor();
        oldHamburgerColor = sharedHelper.getHamburgerColor();
        oldStatusBarColor = sharedHelper.getStatusBarColor();
        oldSendButtonColor = sharedHelper.getSendButtonColor();
        oldChatFieldColor = sharedHelper.getChatFieldTextColor();
        oldOwnTextColor = sharedHelper.getOwnTextColor();
        oldOpponentTextColor = sharedHelper.getOpponentTextColor();


        if (sharedHelper.getOwnBubbleColor() != null) {
            ownBubbleIcon.setColorFilter(Color.parseColor(sharedHelper.getOwnBubbleColor()), PorterDuff.Mode.SRC_ATOP);
        }
        if (sharedHelper.getOpponentBubbleColor() != null) {
            opponentBubbleIcon.setColorFilter(Color.parseColor(sharedHelper.getOpponentBubbleColor()), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                checkAndSendResult();
                break;
            case R.id.saveToCloud:
                if (hasAnythingChanged()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CustomizeLook.this);
                    builder.setTitle(getString(R.string.warning));
                    builder.setMessage(getString(R.string.overwriteWarning));
                    builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveToCloud();
                        }
                    });
                    builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                } else {
                    Snackbar.make(friendsCleaner, getString(R.string.nothingWasChanged), Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.restoreFromCloud:
                System.gc();
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomizeLook.this);
                builder.setTitle(getString(R.string.warning));
                builder.setMessage(getString(R.string.localOverwritingWarning));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        restoreFromCloud();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                break;
            case R.id.resetToDefault:
                sharedHelper.resetCustomization();
                Intent intent = new Intent();
                Toast.makeText(CustomizeLook.this, getString(R.string.reseted), Toast.LENGTH_SHORT).show();
                intent.putExtra("reset", true);
                setResult(Constants.REQUEST_CUSTOMIZE_MADE, intent);
                finish();
                break;
            case R.id.removeFromCloud:
                System.gc();
                builder = new AlertDialog.Builder(CustomizeLook.this);
                builder.setTitle(getString(R.string.warning));
                builder.setMessage(getString(R.string.removeFromCloudWarning));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeFromCloud();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeFromCloud() {
        Call<ResponseBody> removeFromCloudCall = Retrofit.getInstance().getInkService().removeFromCloud(sharedHelper.getUserId(), Constants.CUSTOMIZATION_TYPE_REMOVE);
        removeFromCloudCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    removeFromCloud();
                    return;
                }
                if (response.body() == null) {
                    removeFromCloud();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    ColorModel colorModel = gson.fromJson(responseBody, ColorModel.class);
                    if (colorModel.success) {
                        Snackbar.make(friendsCleaner, getString(R.string.dataRemoved), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                        anythingChanged = false;
                    } else {
                        if (colorModel.cause != null) {
                            if (colorModel.cause.equals(ErrorCause.NO_CUSTOMIZATION)) {
                                Snackbar.make(friendsCleaner, getString(R.string.noCustomizationData), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                            } else {
                                Snackbar.make(friendsCleaner, getString(R.string.couldNotRemove), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                            }
                        } else {
                            Snackbar.make(friendsCleaner, getString(R.string.couldNotRemove), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(friendsCleaner, getString(R.string.couldNotRemove), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(friendsCleaner, getString(R.string.couldNotRemove), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
            }
        });
    }

    private void restoreFromCloud() {
        Call<ResponseBody> restoreCall = Retrofit.getInstance().getInkService().restoreCustomization(sharedHelper.getUserId(), Constants.CUSTOMIZATION_TYPE_RESTORE);
        restoreCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    restoreFromCloud();
                    return;
                }
                if (response.body() == null) {
                    restoreFromCloud();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    ColorModel colorModel = gson.fromJson(responseBody, ColorModel.class);
                    if (colorModel.success) {
                        Log.d("fasFafasfasfas", "onResponse: "+colorModel.menuButton);
                        Log.d("fasFafasfasfas", "onResponse: "+responseBody);
                        saveDataLocally(colorModel);
                        Snackbar.make(actionBarCleaner, getString(R.string.dataRestored), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                    } else {
                        if (colorModel.cause != null) {
                            if (colorModel.cause.equals(ErrorCause.NO_CUSTOMIZATION)) {
                                Snackbar.make(friendsCleaner, getString(R.string.noCustomizationData), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                            } else if (colorModel.cause.equals(ErrorCause.SERVER_ERROR)) {
                                Snackbar.make(friendsCleaner, getString(R.string.errorFetchingData), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                            }
                        } else {
                            Snackbar.make(friendsCleaner, getString(R.string.errorFetchingData), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(friendsCleaner, getString(R.string.errorFetchingData), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(friendsCleaner, getString(R.string.failedToRestore), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
            }
        });
    }

    private void saveDataLocally(ColorModel colorModel) {
        Log.d("Fasfasfasfsafas", "saveDataLocally: " + colorModel.menuButton);
        sharedHelper.putStatusBarColor(colorModel.statusBar);
        sharedHelper.putActionBarColor(colorModel.actionBar);
        sharedHelper.putMenuButtonColor(colorModel.menuButton);
        sharedHelper.putSendButtonColor(colorModel.sendButton);
        sharedHelper.putNotificationIconColor(colorModel.notificationIcon);
        sharedHelper.putShopIconColor(colorModel.shopIcon);
        sharedHelper.putHamburgerColor(colorModel.hamburgerIcon);
        sharedHelper.putLeftSlidingPanelColor(colorModel.leftHeader);
        sharedHelper.putFeedColor(colorModel.feedBackground);
        sharedHelper.putFriendsColor(colorModel.friendsBackground);
        sharedHelper.putMessagesColor(colorModel.messagesBackground);
        sharedHelper.putChatColor(colorModel.chatBackground);
        sharedHelper.putMyRequestColor(colorModel.requestBackground);
        sharedHelper.putOpponentBubbleColor(colorModel.opponentBubble);
        sharedHelper.putOwnBubbleColor(colorModel.ownBubble);
        sharedHelper.putOpponentTextColor(colorModel.opponentText);
        sharedHelper.putOwnTextColor(colorModel.ownText);
        sharedHelper.putChatFieldTextColor(colorModel.chatField);

        anythingChanged = true;
    }

    private void saveToCloud() {
        Call<ResponseBody> saveCustomizationCall = Retrofit.getInstance().getInkService().saveCustomization(Constants.CUSTOMIZATION_TYPE_SAVE,
                sharedHelper.getUserId(), sharedHelper.getStatusBarColor() != null ? sharedHelper.getStatusBarColor() : "",
                sharedHelper.getActionBarColor() != null ? sharedHelper.getActionBarColor() : "",
                sharedHelper.getMenuButtonColor() != null ? sharedHelper.getMenuButtonColor() : "",
                sharedHelper.getSendButtonColor() != null ? sharedHelper.getSendButtonColor() : "",
                sharedHelper.getNotificationIconColor() != null ? sharedHelper.getNotificationIconColor() : "",
                sharedHelper.getShopIconColor() != null ? sharedHelper.getShopIconColor() : "",
                sharedHelper.getHamburgerColor() != null ? sharedHelper.getHamburgerColor() : "",
                sharedHelper.getLeftSlidingPanelHeaderColor() != null ? sharedHelper.getLeftSlidingPanelHeaderColor() : "",
                sharedHelper.getFeedColor() != null ? sharedHelper.getFeedColor() : "",
                sharedHelper.getFriendsColor() != null ? sharedHelper.getFriendsColor() : "",
                sharedHelper.getMessagesColor() != null ? sharedHelper.getMessagesColor() : "",
                sharedHelper.getChatColor() != null ? sharedHelper.getChatColor() : "",
                sharedHelper.getMyRequestColor() != null ? sharedHelper.getMyRequestColor() : "",
                sharedHelper.getOpponentBubbleColor() != null ? sharedHelper.getOpponentBubbleColor() : "",
                sharedHelper.getOwnBubbleColor() != null ? sharedHelper.getOwnBubbleColor() : "",
                sharedHelper.getOpponentTextColor() != null ? sharedHelper.getOpponentTextColor() : "",
                sharedHelper.getOwnTextColor() != null ? sharedHelper.getOwnTextColor() : "",
                sharedHelper.getChatFieldTextColor() != null ? sharedHelper.getChatFieldTextColor() : "");
        saveCustomizationCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    saveToCloud();
                    return;
                }
                if (response.body() == null) {
                    saveToCloud();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    ColorModel colorModel = gson.fromJson(responseBody, ColorModel.class);
                    if (colorModel.success) {
                        Snackbar.make(friendsCleaner, getString(R.string.customizationSaved), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();
                    } else {
                        if (colorModel.cause != null) {
                            if (colorModel.cause.equals(ErrorCause.SERVER_ERROR)) {
                                Snackbar.make(friendsCleaner, getString(R.string.errorSaviingCustomaziation), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                            }
                        } else {
                            Snackbar.make(friendsCleaner, getString(R.string.customizationSaved), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Snackbar.make(friendsCleaner, getString(R.string.errorSaviingCustomaziation), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    }).show();
                                }
                            }).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(friendsCleaner, getString(R.string.errorSaviingCustomaziation), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(friendsCleaner, getString(R.string.failedToSaveCloud), Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customization_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void showColorPicker(final GeneralCallback<String> generalCallback) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, Color.parseColor("#3F51B5"), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                // color is the color selected by the user.
                String hexWithoutAlpha = Integer.toHexString(color).toUpperCase().substring(2);
                generalCallback.onSuccess("#" + hexWithoutAlpha);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                generalCallback.onFailure(null);
            }
        });
        dialog.show();

    }

    @OnClick(R.id.pickHamburgerIconWrapper)
    public void pickHamburgerIconWrapper() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                hamburgerColorPicked = true;
                hamburgerIcon.setColorFilter(Color.parseColor(s), PorterDuff.Mode.SRC_ATOP);
                hamburgerCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putHamburgerColor(s);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.statusBarColorPicker)
    public void statusBarColorPicker() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Snackbar.make(actionBarCleaner, getString(R.string.optionNotAvailable), Snackbar.LENGTH_LONG).show();
            return;
        }
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                statusBarCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putStatusBarColor(s);
                Snackbar.make(friendsCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                statusBarColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.chatFieldWrapper)
    public void chatFieldWrapper() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                chatFieldColorPicked = true;
                Snackbar.make(actionBarCleaner, getString(R.string.colorSet), Snackbar.LENGTH_LONG).show();
                sharedHelper.putChatFieldTextColor(s);
                chatFieldCleaner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }


    @OnClick(R.id.ownChatTextColorWrapper)
    public void ownTextColorWrapper() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                ownTextColorPicked = true;
                Snackbar.make(actionBarCleaner, getString(R.string.colorSet), Snackbar.LENGTH_LONG).show();
                sharedHelper.putOwnTextColor(s);
                ownTextCleaner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.opponentChatTextColorWrapper)
    public void opponentTextColorWrapper() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                opponentTextColorPicked = true;
                Snackbar.make(actionBarCleaner, getString(R.string.colorSet), Snackbar.LENGTH_LONG).show();
                sharedHelper.putOpponentTextColor(s);
                opponentTextCleaner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.actionBarColorPicker)
    public void actionBarColorPicker() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                actionBarColorPicked = true;
                customizeToolbar.setBackgroundColor(Color.parseColor(s));
                actionBarCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putActionBarColor(s);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }


    @OnClick(R.id.sendButtonPickerWrapper)
    public void sendButtonPickerWrapper() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                sendButtonColorPicked = true;
                sendButtonIcon.setColorNormal(Color.parseColor(s));
                sharedHelper.putSendButtonColor(s);
                sendButtonCleaner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.fabMenuButtonColorPicker)
    public void fabMenuButtonColorPicker() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                fabMenuButtonColorPicked = true;
                customizeFabMenu.setMenuButtonColorNormal(Color.parseColor(s));
                sharedHelper.putMenuButtonColor(s);
                fabButtonCleaner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.pickNotificationIconColor)
    public void pickNotificationIconColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                pickNotificationIconColorPicked = true;
                notificationIcon.setColorFilter(Color.parseColor(s), PorterDuff.Mode.SRC_ATOP);
                notificationCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putNotificationIconColor(s);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.pickShopIconColor)
    public void pickShopIconColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                shopCleaner.setVisibility(View.VISIBLE);
                shopIcon.setColorFilter(Color.parseColor(s), PorterDuff.Mode.SRC_ATOP);
                sharedHelper.putShopIconColor(s);
                pickShopIconColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.pickLeftDrawerColor)
    public void pickLeftDrawerColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                leftPanelCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putLeftSlidingPanelColor(s);
                Snackbar.make(friendsCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                pickLeftDrawerColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.feedBackgroundColor)
    public void feedBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                feedCleaner.setVisibility(View.VISIBLE);
                Snackbar.make(feedCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                sharedHelper.putFeedColor(s);
                feedColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.friendsBackgroundColor)
    public void friendsBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                friendsBackgroundColorPicked = true;
                friendsCleaner.setVisibility(View.VISIBLE);
                Snackbar.make(friendsCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                sharedHelper.putFriendsColor(s);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.messagesBackgroundColor)
    public void messagesBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                messagesCleaner.setVisibility(View.VISIBLE);
                Snackbar.make(messagesCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                sharedHelper.putMessagesColor(s);
                messagesBackgroundColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.chatBackgroundColor)
    public void chatBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                chatCleaner.setVisibility(View.VISIBLE);
                Snackbar.make(chatCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                sharedHelper.putChatColor(s);
                chatBackgroundColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.requestsBackgroundColor)
    public void requestsBackgroundColor() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                requestCleaner.setVisibility(View.VISIBLE);
                Snackbar.make(requestCleaner, getString(R.string.colorSet), Snackbar.LENGTH_SHORT).show();
                sharedHelper.putMyRequestColor(s);
                requestBackgroundColorPicked = true;
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }


    @OnClick(R.id.opponentBubbleWrapper)
    public void opponentBubbleWrapper() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                opponentBubbleColorPicked = true;
                opponentBubbleIcon.setColorFilter(Color.parseColor(s), PorterDuff.Mode.SRC_ATOP);
                opponentBubbleCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putOpponentBubbleColor(s);

            }

            @Override
            public void onFailure(String s) {

            }
        });
    }


    @OnClick(R.id.ownBubbleWrapper)
    public void ownBubbleWrapper() {
        showColorPicker(new GeneralCallback<String>() {
            @Override
            public void onSuccess(String s) {
                ownBubbleColorPicked = true;
                ownBubbleIcon.setColorFilter(Color.parseColor(s), PorterDuff.Mode.SRC_ATOP);
                ownBubbleCleaner.setVisibility(View.VISIBLE);
                sharedHelper.putOwnBubbleColor(s);
            }

            @Override
            public void onFailure(String s) {

            }
        });
    }

    @OnClick(R.id.statusBarCleaner)
    public void statusBarCleaner() {
        statusBarCleaner.setVisibility(View.GONE);
        statusBarColorPicked = false;
        sharedHelper.putStatusBarColor(oldStatusBarColor);
        Snackbar.make(statusBarCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
    }

    @OnClick(R.id.hamburgerCleaner)
    public void hamburgerCleaner() {
        hamburgerColorPicked = false;
        hamburgerCleaner.setVisibility(View.GONE);
        sharedHelper.putHamburgerColor(oldHamburgerColor);
        hamburgerIcon.setColorFilter(null);
    }

    @OnClick(R.id.actionBarCleaner)
    public void actionBarCleaner() {
        actionBarColorPicked = false;
        actionBarCleaner.setVisibility(View.GONE);
        sharedHelper.putActionBarColor(oldActionBarColor);
        if (oldActionBarColor != null) {
            customizeToolbar.setBackgroundColor(Color.parseColor(oldActionBarColor));
        } else {
            customizeToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }

    @OnClick(R.id.fabButtonCleaner)
    public void fabButtonCleaner() {
        fabMenuButtonColorPicked = false;
        fabButtonCleaner.setVisibility(View.GONE);
        sharedHelper.putMenuButtonColor(oldMenuButtonColor);
        if (oldMenuButtonColor != null) {
            customizeFabMenu.setMenuButtonColorNormal(Color.parseColor(oldMenuButtonColor));
        } else {
            customizeFabMenu.setMenuButtonColorNormal(ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }

    @OnClick(R.id.ownBubbleCleaner)
    public void ownBubbleCleaner() {
        ownBubbleColorPicked = false;
        ownBubbleCleaner.setVisibility(View.GONE);
        ownBubbleIcon.setColorFilter(null);
        sharedHelper.putOwnBubbleColor(oldOwnBubbleColor);
    }

    @OnClick(R.id.sendButtonCleaner)
    public void sendButtonCleaner() {
        sendButtonColorPicked = false;
        sendButtonCleaner.setVisibility(View.GONE);
        sendButtonIcon.setColorNormal(ContextCompat.getColor(this, R.color.colorPrimary));
        sharedHelper.putSendButtonColor(oldSendButtonColor);
    }

    @OnClick(R.id.opponentBubbleCleaner)
    public void opponentBubbleCleaner() {
        opponentBubbleColorPicked = false;
        opponentBubbleCleaner.setVisibility(View.GONE);
        opponentBubbleIcon.setColorFilter(null);
        sharedHelper.putOpponentBubbleColor(oldOpponentBubbleColor);
    }

    @OnClick(R.id.notificationCleaner)
    public void notificationCleaner() {
        pickNotificationIconColorPicked = false;
        notificationCleaner.setVisibility(View.GONE);
        notificationIcon.setColorFilter(null);
        sharedHelper.putNotificationIconColor(oldNotificationIconColor);
    }

    @OnClick(R.id.shopCleaner)
    public void shopCleaner() {
        pickShopIconColorPicked = false;
        shopCleaner.setVisibility(View.GONE);
        shopIcon.setColorFilter(null);
        sharedHelper.putShopIconColor(oldShopIconColor);
    }

    @OnClick(R.id.leftPanelCleaner)
    public void leftPanelCleaner() {
        pickLeftDrawerColorPicked = false;
        leftPanelCleaner.setVisibility(View.GONE);
        sharedHelper.putLeftSlidingPanelColor(oldLeftSlidingPanelHeaderColor);
        Snackbar.make(leftPanelCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
    }

    @OnClick(R.id.feedCleaner)
    public void feedCleaner() {
        feedColorPicked = false;
        feedCleaner.setVisibility(View.GONE);
        Snackbar.make(feedCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putFeedColor(oldFeedColor);
    }

    @OnClick(R.id.friendsCleaner)
    public void friendsCleaner() {
        friendsBackgroundColorPicked = false;
        friendsCleaner.setVisibility(View.GONE);
        Snackbar.make(friendsCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putFriendsColor(oldFriendsColor);
    }

    @OnClick(R.id.messagesCleaner)
    public void messagesCleaner() {
        messagesBackgroundColorPicked = false;
        messagesCleaner.setVisibility(View.GONE);
        Snackbar.make(messagesCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putMessagesColor(oldMessagesColor);
    }

    @OnClick(R.id.chatFieldCleaner)
    public void chatFieldCleaner() {
        chatFieldColorPicked = false;
        Snackbar.make(actionBarCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putChatFieldTextColor(oldChatFieldColor);
        chatFieldCleaner.setVisibility(View.GONE);
    }

    @OnClick(R.id.ownTextCleaner)
    public void ownTextCleaner() {
        ownTextColorPicked = false;
        Snackbar.make(actionBarCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putOwnTextColor(oldOwnTextColor);
        ownTextCleaner.setVisibility(View.GONE);
    }

    @OnClick(R.id.opponentTextCleaner)
    public void opponentTextCleaner() {
        opponentTextColorPicked = false;
        Snackbar.make(actionBarCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putOpponentTextColor(oldOpponentTextColor);
        opponentBubbleCleaner.setVisibility(View.GONE);
    }

    @OnClick(R.id.chatCleaner)
    public void chatCleaner() {
        chatBackgroundColorPicked = false;
        chatCleaner.setVisibility(View.GONE);
        Snackbar.make(chatCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putChatColor(oldChatColor);
    }

    @OnClick(R.id.requestCleaner)
    public void requestCleaner() {
        requestBackgroundColorPicked = false;
        requestCleaner.setVisibility(View.GONE);
        Snackbar.make(requestCleaner, getString(R.string.colorRemoved), Snackbar.LENGTH_SHORT).show();
        sharedHelper.putMyRequestColor(oldMyRequestColor);
    }

    @Override
    public void onBackPressed() {
        checkAndSendResult();

    }

    private void checkAndSendResult() {
        if (anythingChanged) {
            Intent intent = new Intent();
            intent.putExtra("anythingChanged", true);
            setResult(Constants.REQUEST_CUSTOMIZE_MADE, intent);
            finish();
        } else if (!actionBarColorPicked && !fabMenuButtonColorPicked && !pickNotificationIconColorPicked && !pickShopIconColorPicked &&
                !pickLeftDrawerColorPicked && !feedColorPicked && !messagesBackgroundColorPicked && !friendsBackgroundColorPicked &&
                !chatBackgroundColorPicked && !requestBackgroundColorPicked && !opponentBubbleColorPicked &&
                !ownBubbleColorPicked && !sendButtonColorPicked && !statusBarColorPicked && !hamburgerColorPicked
                && !opponentTextColorPicked && !chatFieldColorPicked && !ownTextColorPicked) {
            Intent intent = new Intent();
            intent.putExtra("anythingChanged", false);
            setResult(Constants.REQUEST_CUSTOMIZE_MADE, intent);
            finish();
        } else {
            Intent intent = new Intent();
            intent.putExtra("anythingChanged", true);
            setResult(Constants.REQUEST_CUSTOMIZE_MADE, intent);
            finish();
        }

    }

    private boolean hasAnythingChanged() {
        if (!actionBarColorPicked && !fabMenuButtonColorPicked && !pickNotificationIconColorPicked && !pickShopIconColorPicked &&
                !pickLeftDrawerColorPicked && !feedColorPicked && !messagesBackgroundColorPicked && !friendsBackgroundColorPicked &&
                !chatBackgroundColorPicked && !requestBackgroundColorPicked && !opponentBubbleColorPicked &&
                !ownBubbleColorPicked && !sendButtonColorPicked && !statusBarColorPicked && !hamburgerColorPicked
                && !opponentTextColorPicked && !chatFieldColorPicked && !ownTextColorPicked) {
            return false;
        } else {
            return true;
        }
    }
}
