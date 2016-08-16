package ink.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.adapters.CommentAdapter;
import ink.decorators.DividerItemDecoration;
import ink.interfaces.CommentClickHandler;
import ink.interfaces.ItemClickListener;
import ink.interfaces.RecyclerItemClickListener;
import ink.models.CommentModel;
import ink.utils.Animations;
import ink.utils.Constants;
import ink.utils.InputField;
import ink.utils.Keyboard;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Comments extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, CommentClickHandler, RecyclerItemClickListener {

    @Bind(R.id.commentBody)
    EditText mCommentBody;
    @Bind(R.id.noCommentWrapper)
    RelativeLayout mNoCommentWrapper;
    @Bind(R.id.commentsLoading)
    ProgressBar mCommentsLoading;
    @Bind(R.id.addCommentButton)
    FloatingActionButton mAaddCommentButton;
    @Bind(R.id.commentRecycler)
    RecyclerView mCommentRecycler;
    @Bind(R.id.commentRefresher)
    SwipeRefreshLayout mCommentRefresher;
    @Bind(R.id.commentCard)
    CardView commentCard;
    private String mPostId;
    private String mUserImage;
    private String mPostBody;
    private SharedHelper mSharedHelper;
    private List<CommentModel> mCommentModels;
    private CommentAdapter mCommentAdapter;
    private CommentModel mCommentModel;
    private String mAttachment;
    private boolean isOwnerSocialAccount;
    private String mLocation;
    private String mDate;
    private String mName;
    private String mLikesCount;
    private boolean isLiked;
    private Dialog addCommentDialog;
    @Bind(R.id.commentRootLayout)
    View contentView;
    private boolean isResponseReceived;
    private boolean hasComments;
    private String ownerId;
    private ProgressDialog deleteDialog;
    private boolean hasAttachment;
    private boolean hasAddress;
    private String attachmentName;
    private String addressName;
    private String postId;
    private String postBody;
    private Snackbar snackbar;
    private BroadcastReceiver broadcastReceiver;
    private boolean shouldUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        ButterKnife.bind(this);
        mSharedHelper = new SharedHelper(this);
        Bundle extras = getIntent().getExtras();
        ActionBar actionBar = getSupportActionBar();
        mCommentModels = new ArrayList<>();
        mCommentRefresher.setOnRefreshListener(this);
        deleteDialog = new ProgressDialog(Comments.this);
        deleteDialog.setTitle(getString(R.string.deleting));
        deleteDialog.setMessage(getString(R.string.deletingPost));
        deleteDialog.setCancelable(false);
        snackbar = Snackbar.make(mCommentRefresher, getString(R.string.savingChanges), Snackbar.LENGTH_INDEFINITE);
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.comments));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (extras != null) {
            mPostId = extras.getString("postId");
            mUserImage = extras.getString("userImage");
            mPostBody = extras.getString("postBody");
            mAttachment = extras.getString("attachment");
            isOwnerSocialAccount = extras.getBoolean("isSocialAccount");
            mLocation = extras.getString("location");
            mName = extras.getString("name");
            mDate = extras.getString("date");
            mLikesCount = extras.getString("likesCount");
            isLiked = extras.getBoolean("isLiked");
            ownerId = extras.getString("ownerId");

            hasAttachment = extras.getBoolean("hasAttachment");
            hasAddress = extras.getBoolean("hasAddress");
            attachmentName = extras.getString("attachmentName");
            addressName = extras.getString("addressName");
            postId = extras.getString("postId");
            postBody = extras.getString("postBody");

        }
        mCommentAdapter = new CommentAdapter(ownerId, mCommentModels, this, mUserImage,
                mPostBody, mAttachment, mLocation, mDate, mName, mLikesCount, isLiked, isOwnerSocialAccount);
        mCommentAdapter.setOnLikeClickListener(this);
        mCommentRefresher.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        mCommentRecycler.setHasFixedSize(true);
        mCommentRecycler.setLayoutManager(new LinearLayoutManager(this));
        mCommentRecycler.setItemAnimator(itemAnimator);
        mCommentRecycler.addItemDecoration(new DividerItemDecoration(this));
        mCommentRecycler.setAdapter(mCommentAdapter);

        addCommentDialog = new Dialog(this, R.style.Theme_Transparent);
        addCommentDialog.setContentView(R.layout.dim_comment_layout);

        mCommentRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(mCommentRecycler.getWindowToken(), 0);
                }
            }
        });
        mCommentAdapter.setOnItemClickListener(this);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(getPackageName() + "Comments"));

        mAaddCommentButton.setEnabled(false);
        getComments(mPostId, false);
        mCommentBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    mAaddCommentButton.setEnabled(false);
                } else {
                    mAaddCommentButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        attachKeyboardCallback();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (shouldUpdate) {
            LocalBroadcastManager.getInstance(Comments.this).sendBroadcast(new Intent(getPackageName() + "HomeActivity"));
        }
        finish();
        return super.onOptionsItemSelected(item);
    }


    private void getComments(final String postId, final boolean shouldFocus) {
        isResponseReceived = false;
        Call<ResponseBody> commentsCall = Retrofit.getInstance().getInkService().getComments(postId);
        commentsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getComments(postId, shouldFocus);
                    return;
                }
                if (response.body() == null) {
                    getComments(postId, shouldFocus);
                    return;
                }
                if (mCommentModels != null) {
                    mCommentModels.clear();
                    mCommentAdapter.notifyDataSetChanged();
                }
                try {
                    isResponseReceived = true;
                    String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);
                    if (jsonArray.length() <= 0) {
                        mCommentsLoading.setVisibility(View.GONE);
                        mNoCommentWrapper.setVisibility(View.VISIBLE);
                        mCommentRefresher.setRefreshing(false);
                        hasComments = false;
                    } else {
                        hasComments = true;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject eachObject = jsonArray.optJSONObject(i);
                            String commenterId = eachObject.optString("commenter_id");
                            String commenterImage = eachObject.optString("commenter_image");
                            String commentBody = eachObject.optString("comment_body");
                            String postId = eachObject.optString("post_id");
                            String commentId = eachObject.optString("comment_id");
                            String firstName = eachObject.optString("commenter_first_name");
                            String lastName = eachObject.optString("commenter_last_name");
                            boolean isSocialAccount = eachObject.optBoolean("isSocialAccount");
                            mCommentModel = new CommentModel(isSocialAccount, commentId,
                                    commenterId, commenterImage, commentBody, postId, firstName,
                                    lastName);
                            mCommentModels.add(mCommentModel);
                            mCommentAdapter.notifyDataSetChanged();
                        }
                        mNoCommentWrapper.setVisibility(View.GONE);
                        mCommentsLoading.setVisibility(View.GONE);
                        mCommentRefresher.setRefreshing(false);
                        if (shouldFocus) {
                            if (addCommentDialog.isShowing()) {
                                System.gc();
                                addCommentDialog.dismiss();
                            }
                            mCommentBody.setText("");
                            focusUp();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getComments(postId, shouldFocus);
            }
        });
    }

    @OnClick(R.id.addCommentButton)
    public void addCommentButton() {
        isResponseReceived = false;
        mNoCommentWrapper.setVisibility(View.GONE);
        addCommentDialog.show();
        addComment(mCommentBody.getText().toString().trim(), mSharedHelper.getImageLink(), mSharedHelper.getUserId(), mPostId);
    }

    private void addComment(final String commentBody, final String userImage, final String commenterId, final String postId) {
        Call<ResponseBody> addCommentCall = Retrofit.getInstance().getInkService().addComment(commenterId,
                userImage, commentBody, postId, mSharedHelper.getFirstName(), mSharedHelper.getLastName());
        addCommentCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    addComment(commentBody, userImage, commenterId, postId);
                    return;
                }
                if (response.body() == null) {
                    addComment(commentBody, userImage, commenterId, postId);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        getComments(postId, true);
                    } else {
                        addCommentDialog.dismiss();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                addComment(commentBody, userImage, commenterId, postId);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            try {
                LocalBroadcastManager.getInstance(Comments.this).unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {

            }
        }
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        getComments(mPostId, false);
    }

    @Override
    public void onLikeClicked(int position, TextView likesCountTV, ImageView likeView) {
        Animations.animateCircular(likeView);
        if (isLiked) {
            //must dislike
            like(mPostId, 1, likesCountTV);
            likeView.setBackgroundResource(R.drawable.like_inactive);
            mCommentAdapter.setIsLiked(false);
            isLiked = false;
        } else {
            //must like
            like(mPostId, 0, likesCountTV);
            likeView.setBackgroundResource(R.drawable.like_active);
            mCommentAdapter.setIsLiked(true);
            isLiked = true;
        }
    }

    @Override
    public void onAddressClick(int position) {
        openGoogleMaps(mLocation);
    }

    @Override
    public void onAttachmentClick(int position) {
        showPromptDialog(mAttachment);
    }

    @Override
    public void onMoreClick(int position, View view) {
        ink.utils.PopupMenu.showPopUp(Comments.this, view, new ItemClickListener<MenuItem>() {
            @Override
            public void onItemClick(MenuItem clickedItem) {
                switch (clickedItem.getItemId()) {
                    case 0:
                        Intent intent = new Intent(getApplicationContext(), MakePost.class);
                        intent.putExtra("isEditing", true);
                        intent.putExtra("hasAttachment", hasAttachment);
                        intent.putExtra("hasAddress", hasAddress);
                        intent.putExtra("attachmentName", attachmentName);
                        intent.putExtra("addressName", addressName);
                        intent.putExtra("postId", postId);
                        intent.putExtra("postBody", postBody);
                        startActivity(intent);
                        break;
                    case 1:
                        System.gc();
                        AlertDialog.Builder builder = new AlertDialog.Builder(Comments.this);
                        builder.setTitle(getString(R.string.deletePost));
                        builder.setMessage(getString(R.string.areYouSure));
                        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                deleteDialog.show();
                                deletePost();
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
            }
        }, getString(R.string.edit), getString(R.string.delete));
    }

    private void deletePost() {
        Call<ResponseBody> deletePostCall = Retrofit.getInstance().getInkService().deletePost(mPostId, mAttachment);
        deletePostCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    deletePost();
                    return;
                }
                if (response.body() == null) {
                    deletePost();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    deleteDialog.dismiss();
                    if (success) {
                        LocalBroadcastManager.getInstance(Comments.this).sendBroadcast(new Intent(getPackageName() + "HomeActivity"));
                        finish();
                    } else {
                        Snackbar.make(mCommentsLoading, getString(R.string.couldNotDeletePost), Snackbar.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    deleteDialog.dismiss();
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void openGoogleMaps(String address) {
        String uri = "geo:0,0?q=" + address;
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private void showPromptDialog(final String fileName) {
        System.gc();
        AlertDialog.Builder builder = new AlertDialog.Builder(Comments.this);
        builder.setTitle(getString(R.string.downloadQuestion));
        builder.setMessage(getString(R.string.downloadTheFile) + " " + fileName + " ?");
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                queDownload(fileName);
            }
        });
        builder.show();
    }


    private void queDownload(String fileName) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + fileName));
        request.setTitle(fileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        downloadManager.enqueue(request);

    }

    private void like(final String postId, final int isLiking, final TextView likeCountTV) {
        shouldUpdate = true;
        final Call<ResponseBody> likeCall = Retrofit.getInstance().getInkService().likePost(mSharedHelper.getUserId(), postId, isLiking);
        likeCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    like(postId, isLiking, likeCountTV);
                    return;
                }
                if (response.body() == null) {
                    like(postId, isLiking, likeCountTV);
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String likesCount = jsonObject.optString("likes_count");
                    if (!likesCount.equals("0")) {
                        likeCountTV.setVisibility(View.VISIBLE);
                        if (Integer.parseInt(likesCount) > 1) {
                            likeCountTV.setText(likesCount + " " + getString(R.string.likesText));
                        } else {
                            likeCountTV.setText(likesCount + " " + getString(R.string.singleLikeText));
                        }
                    } else {
                        likeCountTV.setVisibility(View.GONE);
                    }
                } catch (IOException e) {
                    shouldUpdate = false;
                    e.printStackTrace();
                } catch (JSONException e) {
                    shouldUpdate = false;
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                like(postId, isLiking, likeCountTV);
            }
        });
    }

    private void focusUp() {
        mCommentRecycler.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                mCommentRecycler.smoothScrollToPosition(0);
            }
        });

    }

    private void attachKeyboardCallback() {
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                contentView.getWindowVisibleDisplayFrame(r);
                int screenHeight = contentView.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {
                    if (isResponseReceived) {
                        if (!hasComments) {
                            if (mNoCommentWrapper.getVisibility() == View.VISIBLE) {
                                mNoCommentWrapper.setVisibility(View.GONE);
                            }
                        }
                    }
                } else {
                    if (isResponseReceived) {
                        if (!hasComments) {
                            if (mNoCommentWrapper.getVisibility() == View.GONE) {
                                mNoCommentWrapper.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onItemClicked(int position, View view) {
        Log.d("fsafsafasfas", "onItemLongClick: " + "on item  click");
        int actualPosition = position - 1;
        try {
            CommentModel singleModel = mCommentModels.get(actualPosition);
            String currentId = singleModel.getCommenterId();
            if (currentId.equals(mSharedHelper.getUserId())) {
                Intent intent = new Intent(getApplicationContext(), MyProfile.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), OpponentProfile.class);
                intent.putExtra("id", currentId);
                intent.putExtra("firstName", singleModel.getFirstName());
                intent.putExtra("lastName", singleModel.getLastName());
                startActivity(intent);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //the header or footer was clicked nothing else.
        }
    }

    @Override
    public void onItemLongClick(int position) {
    }

    private void callCommentServer(final String type, final String commentId, final String newCommmentBody) {
        Call<ResponseBody> commentOptionsCall = Retrofit.getInstance().getInkService().commentOptions(type, commentId, newCommmentBody);
        commentOptionsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    callCommentServer(type, commentId, newCommmentBody);
                    return;
                }
                if (response.body() == null) {
                    callCommentServer(type, commentId, newCommmentBody);
                    return;
                }
                try {
                    String responseBody = response.body().string();

                    getComments(mPostId, false);
                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
                    if (type.equals(Constants.COMMENT_TYPE_EDIT)) {
                        snackbar.setText(getString(R.string.changesWasSaved));
                        snackbar.show();
                    } else {
                        snackbar.setText(getString(R.string.commentWasDeleted));
                        snackbar.show();
                    }
                    Log.d("fasfsafsafa", "onResponse: " + responseBody);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callCommentServer(type, commentId, newCommmentBody);
            }
        });
    }

    @Override
    public void onAdditionItemClick(int position, View view) {
        System.gc();
        int actualPosition = position - 1;
        try {
            final CommentModel commentModel = mCommentModels.get(actualPosition);
            ink.utils.PopupMenu.showPopUp(Comments.this, view, new ItemClickListener<MenuItem>() {
                @Override
                public void onItemClick(MenuItem clickedItem) {
                    switch (clickedItem.getItemId()) {
                        case 0:
                            InputField.createInputFieldView(Comments.this, new InputField.ClickHandler() {
                                @Override
                                public void onPositiveClicked(Object result) {
                                    snackbar.show();
                                    Keyboard.hideKeyboard(getApplicationContext(), commentCard);
                                    callCommentServer(Constants.COMMENT_TYPE_EDIT, commentModel.getCommentId(), String.valueOf(result));
                                }

                                @Override
                                public void onNegativeClicked(Object result) {

                                }
                            });
                            break;
                        case 1:
                            snackbar.show();
                            callCommentServer(Constants.COMMENT_TYPE_DELETE, commentModel.getCommentId(), "");
                            break;
                    }
                }
            }, getString(R.string.editComment), getString(R.string.deleteComment));

        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }

    @Override
    public void onBackPressed() {
        if (shouldUpdate) {
            LocalBroadcastManager.getInstance(Comments.this).sendBroadcast(new Intent(getPackageName() + "HomeActivity"));
        }
        super.onBackPressed();
    }
}
