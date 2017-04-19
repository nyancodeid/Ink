package ink.va.fragments;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.activities.Comments;
import ink.va.activities.FullscreenActivity;
import ink.va.activities.HomeActivity;
import ink.va.activities.MakePost;
import ink.va.activities.OpponentProfile;
import ink.va.activities.SingleGroupView;
import ink.va.adapters.FeedAdapter;
import ink.va.interfaces.ColorChangeListener;
import ink.va.interfaces.FeedItemClick;
import ink.va.interfaces.ItemClickListener;
import ink.va.models.FeedModel;
import ink.va.utils.Animations;
import ink.va.utils.Constants;
import ink.va.utils.DialogUtils;
import ink.va.utils.ImageLoader;
import ink.va.utils.InputField;
import ink.va.utils.PermissionsChecker;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import ink.va.utils.Time;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import zh.wang.android.yweathergetter4a.WeatherInfo;
import zh.wang.android.yweathergetter4a.YahooWeather;
import zh.wang.android.yweathergetter4a.YahooWeatherInfoListener;

import static ink.va.utils.Constants.EVENT_POST_LIKED;
import static ink.va.utils.Time.DAYTIME_AFTERNOON;
import static ink.va.utils.Time.DAYTIME_EVENING;
import static ink.va.utils.Time.DAYTIME_MORNING;
import static ink.va.utils.Time.DAYTIME_NIGHT;

/**
 * Created by USER on 2016-06-21.
 */
public class Feed extends android.support.v4.app.Fragment implements SwipeRefreshLayout.OnRefreshListener,
        FeedItemClick, ColorChangeListener, YahooWeatherInfoListener, com.google.android.gms.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = Feed.class.getSimpleName();
    private static final int SHARE_INTENT_RESULT = 5;
    private static final int STORAGE_PERMISSION_REQUEST = 115;
    public static final int LOCATION_PERMISSION_REQUEST = 116;
    private RecyclerView mRecyclerView;
    private FeedAdapter mAdapter;
    private SwipeRefreshLayout feedRefresh;
    private SharedHelper mSharedHelper;
    private RelativeLayout noPostsWrapper;
    private ProgressDialog deleteDialog;
    private int mOffset = 0;
    private HomeActivity parentActivity;
    private RelativeLayout newFeedsLayout;
    private RelativeLayout feedRootLayout;
    private Bitmap intentBitmap;
    private String shareFileName;
    private File shareOutPutDir;
    private StringBuilder content;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @BindView(R.id.scrollUpFeed)
    View scrollUpFeed;
    @BindView(R.id.greetingCard)
    View greetingCard;
    @BindView(R.id.greetingTV)
    TextView greetingTV;
    @BindView(R.id.weatherIV)
    ImageView weatherIV;
    private double currentLatitude;
    private double currentLongitude;
    private boolean wasOnPause;
    private boolean failedLocate;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public static Feed newInstance() {
        Feed feed = new Feed();
        return feed;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parentActivity = ((HomeActivity) getActivity());
        mSharedHelper = new SharedHelper(getActivity());
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        newFeedsLayout = (RelativeLayout) view.findViewById(R.id.newFeedsLayout);
        feedRefresh = (SwipeRefreshLayout) view.findViewById(R.id.feedRefresh);
        feedRootLayout = (RelativeLayout) view.findViewById(R.id.feedRootLayout);
        feedRefresh.setColorSchemeColors(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.colorPrimary));
        noPostsWrapper = (RelativeLayout) view.findViewById(R.id.noPostOrErrorWrapper);
        deleteDialog = new ProgressDialog(getActivity());
        deleteDialog.setTitle(getString(R.string.deleting));
        deleteDialog.setMessage(getString(R.string.deletingPost));
        deleteDialog.setCancelable(false);

        feedRefresh.setOnRefreshListener(this);
        mAdapter = new FeedAdapter(getActivity());
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(itemAnimator);
        mAdapter.setOnFeedClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                if (mAdapter.getItemCount() > 5) {
                    if (firstVisiblePosition > 4) {
                        if (scrollUpFeed.getTag().equals(getString(R.string.notVisible))) {
                            showScroller();
                        }
                    } else {
                        if (scrollUpFeed.getTag().equals(getString(R.string.visible))) {
                            hideScroller();
                        }
                    }
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        initGreeting();

        ((HomeActivity) getActivity()).setOnColorChangeListener(this);
        if (mOffset == 0) {
            mOffset = 10;
        }
        checkColor();
        getFeeds(0, mOffset, true, false, false, false);
    }

    public void checkShowComment() {
        if (mSharedHelper.showComments()) {
            int id = Integer.valueOf(mSharedHelper.getPostId());
            for (final FeedModel feedModel : mAdapter.getFeedList()) {
                int eachId = Integer.valueOf(feedModel.getId());
                if (eachId == id) {
                    final int positionOfItem = mAdapter.getFeedList().indexOf(feedModel);
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.scrollToPosition(positionOfItem);
                            mSharedHelper.putPostId("");
                            onCommentClicked(feedModel, null);
                        }
                    });

                    break;
                }
            }
        }

    }


    @OnClick(R.id.scrollUpFeed)
    public void scrollerClicked() {
        mRecyclerView.stopScroll();
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.scrollToPosition(0);
            }
        });
        hideScroller();
    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
        if (weatherInfo != null) {
            // Add your code here
            // weatherInfo object contains all information returned by Yahoo Weather API
            // if `weatherInfo` is null, you can get the error from `errorType`
            if (!isDetached()) {
                String greetingMessage = getString(R.string.greeting_appendix);
                switch (Time.getDayTime()) {
                    case DAYTIME_MORNING:
                        greetingMessage = getString(R.string.greeting_appendix, getString(R.string.morning), mSharedHelper.getFirstName(), weatherInfo.getLocationCity(), weatherInfo.getCurrentText());
                        break;
                    case DAYTIME_AFTERNOON:
                        greetingMessage = getString(R.string.greeting_appendix, getString(R.string.afternoon), mSharedHelper.getFirstName(), weatherInfo.getLocationCity(), weatherInfo.getCurrentText());
                        break;
                    case DAYTIME_EVENING:
                        greetingMessage = getString(R.string.greeting_appendix, getString(R.string.evening), mSharedHelper.getFirstName(), weatherInfo.getLocationCity(), weatherInfo.getCurrentText());
                        break;
                    case DAYTIME_NIGHT:
                        greetingMessage = getString(R.string.greeting_appendix, getString(R.string.night), mSharedHelper.getFirstName(), weatherInfo.getLocationCity(), weatherInfo.getCurrentText());
                        break;
                }

                ImageLoader.loadImage(getActivity(), false, false, weatherInfo.getCurrentConditionIconURL(), 0, R.drawable.sun_icon, weatherIV, null);
                greetingTV.setText(greetingMessage);
                showGreetingCard();
            }

        }
    }

    private void handleLocation(Location location) {
        if (location == null) {
            failedLocate = true;
            hideGreetingCard();
        } else {
            failedLocate = false;
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            YahooWeather yahooWeather = YahooWeather.getInstance();
            yahooWeather.queryYahooWeatherByLatLon(getActivity(), currentLatitude, currentLongitude, this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mSharedHelper.showGreeting()) {
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onRefresh() {
        getFeeds(0, mOffset, true, false, false, false);
    }

    private void getFeeds(final int offset,
                          final int count,
                          final boolean clearItems,
                          final boolean newDataLoading,
                          final boolean showNewFeed, final boolean checkCommentAutomatic) {
        if (clearItems) {
            if (feedRefresh != null) {
                feedRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        feedRefresh.setRefreshing(true);
                    }
                });
            }

        }
        Call<List<FeedModel>> feedCal = Retrofit.getInstance().getInkService().getPosts(mSharedHelper.getUserId(), String.valueOf(offset), String.valueOf(count));
        feedCal.enqueue(new Callback<List<FeedModel>>() {
            @Override
            public void onResponse(Call<List<FeedModel>> call, Response<List<FeedModel>> response) {
                List<FeedModel> feedModels = response.body();

                if (feedModels.isEmpty()) {
                    noPostsWrapper.setVisibility(View.VISIBLE);
                } else {
                    noPostsWrapper.setVisibility(View.GONE);
                }

                mAdapter.setFeedList(feedModels);
                feedRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        feedRefresh.setRefreshing(false);
                    }
                });


                if (!clearItems) {
                    parentActivity.getToolbar().setTitle(HomeActivity.FEED);
                }
                if (showNewFeed) {
                    if (!feedModels.isEmpty()) {
                        newFeedsLayout.setVisibility(View.VISIBLE);
                    } else {
                        newFeedsLayout.setVisibility(View.GONE);
                    }
                }
                if (newDataLoading) {
                    mOffset += 10;
                }
                if (checkCommentAutomatic) {
                    checkShowComment();
                } else {
                    mSharedHelper.putPostId("");
                }

            }

            @Override
            public void onFailure(Call<List<FeedModel>> call, Throwable t) {
                feedRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        feedRefresh.setRefreshing(false);
                    }
                });
                Toast.makeText(parentActivity, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCardViewClick(FeedModel feedModel, String type) {
        switch (type) {
            case Constants.WALL_TYPE_GROUP_MESSAGE:
                startGroupActivity(feedModel);
                break;
            case Constants.WALL_TYPE_POST:
                startCommentActivity(feedModel);
                break;
        }

    }


    private void initGreeting() {
        if (!PermissionsChecker.isLocationPermissionGranted(getActivity())) {
            hideGreetingCard();
            DialogUtils.showPermissionRequestDialog(getActivity(), getString(R.string.location_for_weather), new DialogUtils.DialogListener() {
                @Override
                public void onNegativeClicked() {

                }

                @Override
                public void onDialogDismissed() {

                }

                @Override
                public void onPositiveClicked() {
                    if (!isDetached()) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    }
                }
            });
            return;
        }

    }


    private void showGreetingCard() {
        if (greetingCard.getVisibility() == View.GONE) {
            greetingCard.setVisibility(View.VISIBLE);
        }
    }

    private void hideGreetingCard() {
        if (greetingCard.getVisibility() == View.VISIBLE) {
            greetingCard.setVisibility(View.GONE);
        }
    }


    private void hideScroller() {
        scrollUpFeed.setTag(getString(R.string.notVisible));
        scrollUpFeed.setVisibility(View.GONE);
    }

    private void showScroller() {
        scrollUpFeed.setTag(getString(R.string.visible));
        scrollUpFeed.setVisibility(View.VISIBLE);
    }


    private void startGroupActivity(FeedModel feedModel) {

        Intent intent = new Intent(getActivity(), SingleGroupView.class);
        intent.putExtra("groupName", feedModel.getGroupName());
        intent.putExtra("groupId", feedModel.getId());
        intent.putExtra("groupColor", feedModel.getGroupColor());
        intent.putExtra("groupImage", feedModel.getGroupImage());
        intent.putExtra("groupDescription", feedModel.getGroupDescription());
        intent.putExtra("groupOwnerId", feedModel.getGroupOwnerId());
        intent.putExtra("groupOwnerName", feedModel.getGroupOwnerName());
        intent.putExtra("count", feedModel.getCount());
        intent.putExtra("ownerImage", feedModel.getOwnerImage());
        intent.putExtra("isSocialAccount", feedModel.isSocialAccount());
        intent.putExtra("isMember", true);
        intent.putExtra("isFriend", feedModel.isFriend());
        startActivity(intent);
    }


    private void startCommentActivity(FeedModel feedModel) {

        Intent intent = new Intent(getActivity(), Comments.class);
        intent.putExtra("postId", feedModel.getId());
        intent.putExtra("userImage", feedModel.getUserImage());
        intent.putExtra("postBody", feedModel.getContent());
        intent.putExtra("attachment", feedModel.getFileName());
        intent.putExtra("location", feedModel.getAddress());
        intent.putExtra("name", feedModel.getFirstName() + " " + feedModel.getLastName());
        intent.putExtra("date", Time.convertToLocalTime(feedModel.getDatePosted()));
        intent.putExtra("likesCount", feedModel.getLikesCount());
        intent.putExtra("isLiked", feedModel.isLiked());
        intent.putExtra("isSocialAccount", feedModel.isSocialAccount());
        intent.putExtra("ownerId", feedModel.getPosterId());

        intent.putExtra("attachmentPresent", feedModel.isAttachmentPresent());
        intent.putExtra("addressPresent", feedModel.isAddressPresent());
        intent.putExtra("attachmentName", feedModel.getFileName());
        intent.putExtra("addressName", feedModel.getAddress());
        intent.putExtra("postId", feedModel.getId());
        intent.putExtra("postBody", feedModel.getContent());

        intent.putExtra("isPostOwner", feedModel.isPostOwner());
        intent.putExtra("isFriend", feedModel.isFriend());


        startActivity(intent);
    }

    @Override
    public void onAddressClick(FeedModel feedModel) {
        String address = feedModel.getAddress();
        openGoogleMaps(address);
    }

    @Override
    public void onAttachmentClick(FeedModel feedModel) {
        String fileName = feedModel.getFileName();
        String userId = feedModel.getPosterId();
        showPromptDialog(fileName);
    }

    private void showPromptDialog(final String fileName) {

        int index = fileName.indexOf(":");
        String finalFileName = fileName.substring(index + 1, fileName.length());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.downloadQuestion));
        builder.setMessage(getString(R.string.downloadTheFile) + " " + finalFileName);
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
        int index = fileName.indexOf(":");
        String finalFileName = fileName.substring(index + 1, fileName.length());
        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + fileName));
        request.setTitle(finalFileName);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        downloadManager.enqueue(request);

    }

    @Override
    public void onCardLongClick(FeedModel feedModel) {

    }

    @Override
    public void onLikeClick(FeedModel feedModel, ImageView likeView, TextView likeCountTV, View likeWrapper) {
        Animations.animateCircular(likeView);
        boolean isLiked = feedModel.isLiked();
        likeWrapper.setEnabled(false);
        if (isLiked) {
            //must dislike
            like(feedModel.getId(), 1, likeCountTV, feedModel, likeWrapper);
            likeView.setBackgroundResource(R.drawable.like_inactive);
            feedModel.setLiked(false);
        } else {
            //must like
            like(feedModel.getId(), 0, likeCountTV, feedModel, likeWrapper);
            likeView.setBackgroundResource(R.drawable.like_active);
            feedModel.setLiked(true);
            JSONObject likeJson = new JSONObject();
            try {
                likeJson.put("postOwnerId", feedModel.getPosterId());
                likeJson.put("likerFirstName", mSharedHelper.getFirstName());
                likeJson.put("likerLastName", mSharedHelper.getLastName());
                likeJson.put("likerId", mSharedHelper.getUserId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (((HomeActivity) getActivity()).getSocketService() != null) {
                if (!((HomeActivity) getActivity()).getSocketService().isSocketConnected()) {
                    ((HomeActivity) getActivity()).getSocketService().connectSocket();
                }
                ((HomeActivity) getActivity()).getSocketService().emit(EVENT_POST_LIKED, likeJson);
            } else {
                ((HomeActivity) getActivity()).initService();
                if (!((HomeActivity) getActivity()).getSocketService().isSocketConnected()) {
                    ((HomeActivity) getActivity()).getSocketService().connectSocket();
                }
                ((HomeActivity) getActivity()).getSocketService().emit(EVENT_POST_LIKED, likeJson);
            }
        }
    }

    @Override
    public void onCommentClicked(FeedModel feedModel, View commentView) {
        if (commentView != null) {
            Animations.animateCircular(commentView);
        }

        startCommentActivity(feedModel);
    }

    @Override
    public void onMoreClicked(final FeedModel feedModel, View view) {
        if (feedModel.isPostOwner()) {
            DialogUtils.showPopUp(getActivity(), view, new ItemClickListener<MenuItem>() {
                @Override
                public void onItemClick(MenuItem clickedItem) {
                    switch (clickedItem.getItemId()) {
                        case 0:
                            Intent intent = new Intent(getActivity(), MakePost.class);
                            intent.putExtra("isEditing", true);
                            intent.putExtra("attachmentPresent", feedModel.isAttachmentPresent());
                            intent.putExtra("addressPresent", feedModel.isAddressPresent());
                            intent.putExtra("attachmentName", feedModel.getFileName());
                            intent.putExtra("addressName", feedModel.getAddress());
                            intent.putExtra("postId", feedModel.getId());
                            intent.putExtra("postBody", feedModel.getContent());
                            startActivity(intent);

                            break;
                        case 1:
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(getString(R.string.deletePost));
                            builder.setMessage(getString(R.string.areYouSure));
                            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    deleteDialog.show();
                                    deletePost(feedModel.getId(), feedModel.getFileName());
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
        } else {
            DialogUtils.showPopUp(getActivity(), view, new ItemClickListener<MenuItem>() {
                @Override
                public void onItemClick(MenuItem clickedItem) {
                    switch (clickedItem.getItemId()) {
                        case 0:
                            Intent intent = new Intent(getActivity(), OpponentProfile.class);
                            intent.putExtra("id", feedModel.getPosterId());
                            intent.putExtra("firstName", feedModel.getFirstName());
                            intent.putExtra("lastName", feedModel.getLastName());
                            intent.putExtra("isFriend", feedModel.isFriend());
                            startActivity(intent);
                            break;
                        case 1:
                            showReportField(feedModel);
                            break;
                        case 2:
                            if (mSharedHelper.postMuted(feedModel.getId())) {
                                mSharedHelper.removeNotificationDisabledPost(feedModel.getId());
                                Toast.makeText(parentActivity, getString(R.string.youWillReceiveNotifications), Toast.LENGTH_SHORT).show();
                            } else {
                                mSharedHelper.putNotificationDisabledPostId(feedModel.getId());
                                Toast.makeText(parentActivity, getString(R.string.youWillNotReceiveNotifications), Toast.LENGTH_SHORT).show();
                            }


                            break;
                    }

                }
            }, getString(R.string.viewProfile), getString(R.string.report), mSharedHelper.postMuted(feedModel.getId()) ? getString(R.string.enableNotifications) : getString(R.string.dontReceiveNotificationText));
        }
    }

    private void showReportField(final FeedModel feedModel) {
        InputField.createInputFieldView(getActivity(), new InputField.ClickHandler() {
            @Override
            public void onPositiveClicked(Object... result) {
                AlertDialog dialog = (AlertDialog) result[1];
                dialog.dismiss();
                String reportCauseMessage = String.valueOf(result[0]);
                reportPost(feedModel, reportCauseMessage);
            }

            @Override
            public void onNegativeClicked(Object... result) {
                AlertDialog dialog = (AlertDialog) result[1];
                dialog.dismiss();
            }
        }, null, getString(R.string.reportCause), null);
    }

    private void reportPost(final FeedModel feedModel, final String reportCauseMessage) {
        final ink.va.utils.ProgressDialog dialog = ink.va.utils.ProgressDialog.get().buildProgressDialog(getActivity(), true);
        dialog.setTitle(getString(R.string.connecting));
        dialog.setMessage(getString(R.string.loadingText));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Retrofit.getInstance().getInkService().reportPost(feedModel.getId(), String.valueOf(feedModel.isGlobalPost()), reportCauseMessage, mSharedHelper.getUserId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    reportPost(feedModel, reportCauseMessage);
                    return;
                }
                if (response.body() == null) {
                    reportPost(feedModel, reportCauseMessage);
                    return;
                }
                dialog.hide();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        DialogUtils.showDialog(getActivity(), getString(R.string.done_text), getString(R.string.reported), true, null, false, null);
                        mAdapter.clear();

                        onRefresh();
                    } else {
                        DialogUtils.showDialog(getActivity(), getString(R.string.error), getString(R.string.reportError), true, null, false, null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    DialogUtils.showDialog(getActivity(), getString(R.string.error), getString(R.string.reportError), true, null, false, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                    DialogUtils.showDialog(getActivity(), getString(R.string.error), getString(R.string.reportError), true, null, false, null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dialog.hide();
                DialogUtils.showDialog(getActivity(), getString(R.string.error), getString(R.string.reportError), true, null, false, null);
            }
        });
    }

    private void openShareView(final FeedModel singleModel) {

        content = new StringBuilder();
        content.append(singleModel.getContent());

        if (singleModel.isAttachmentPresent()) {

            ImageLoader.loadImage(getActivity(), false, false, Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + Uri.encode(singleModel.getFileName()),
                    null, 0, null, new ImageLoader.ImageLoadedCallback() {
                        @Override
                        public void onImageLoaded(Object result, Exception e) {
                            if (e != null) {
                                if (singleModel.isAddressPresent()) {
                                    content.append("\n" + getString(R.string.locatedAt) + " " + singleModel.getAddress());
                                }
                                openShareIntent(intentBitmap, content.toString());
                            } else {
                                intentBitmap = (Bitmap) result;
                                if (singleModel.isAddressPresent()) {
                                    content.append("\n" + getString(R.string.locatedAt) + " " + singleModel.getAddress());
                                }
                                openShareIntent(intentBitmap, content.toString());
                            }
                        }
                    });
        } else {
            intentBitmap = null;
            if (singleModel.isAddressPresent()) {
                content.append("\n" + getString(R.string.locatedAt) + " " + singleModel.getAddress());
            }
            openShareIntent(intentBitmap, content.toString());
        }

    }


    private void openShareIntent(@Nullable Bitmap result, String text) {
        if (shareOutPutDir != null && shareOutPutDir.exists()) {
            shareOutPutDir.delete();
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        shareFileName = "Ink_File" + System.currentTimeMillis() + ".jpg";

        if (result != null) {
            if (!PermissionsChecker.isStoragePermissionGranted(getActivity())) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
                return;
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            shareOutPutDir = new File(Environment.getExternalStorageDirectory() + File.separator + shareFileName);
            try {
                shareOutPutDir.createNewFile();
                FileOutputStream fo = new FileOutputStream(shareOutPutDir);
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
                Snackbar.make(mRecyclerView, getString(R.string.error), Snackbar.LENGTH_SHORT).show();
            }
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/" + shareFileName));
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(text);
        stringBuilder.append("\n\n" + getString(R.string.ink_share_text));

        intent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
        startActivityForResult(Intent.createChooser(intent, getString(R.string.share_ink_with)), SHARE_INTENT_RESULT);
    }


    @Override
    public void onImageClicked(FeedModel feedModel) {
        Intent intent = new Intent(getActivity(), FullscreenActivity.class);
        String encodedFileName = Uri.encode(feedModel.getFileName());
        intent.putExtra("link", Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + encodedFileName);
        startActivity(intent);
    }

    @Override
    public void onShareClicked(FeedModel feedModel) {
        openShareView(feedModel);
    }

    private void openGoogleMaps(String address) {
        String uri = "geo:0,0?q=" + address;
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (wasOnPause) {
            wasOnPause = false;
            checkWidget();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        wasOnPause = true;
    }

    private void deletePost(final String postId, final String attachmentName) {
        Call<ResponseBody> deletePostCall = Retrofit.getInstance().getInkService().deletePost(postId, attachmentName);
        deletePostCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    deletePost(postId, attachmentName);
                    return;
                }
                if (response.body() == null) {
                    deletePost(postId, attachmentName);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        mSharedHelper.removeOwnPostId(postId);
                        deleteDialog.dismiss();
                        Snackbar.make(mRecyclerView, getString(R.string.postDeleted), Snackbar.LENGTH_SHORT).show();
                    } else {
                        deleteDialog.dismiss();
                        Snackbar.make(mRecyclerView, getString(R.string.couldNotDeletePost), Snackbar.LENGTH_LONG).show();
                    }
                    getFeeds(0, mOffset, true, false, false, false);
                } catch (IOException e) {
                    e.printStackTrace();
                    deleteDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                deleteDialog.dismiss();
            }
        });
    }

    private void like(final String postId, final int isLiking, final TextView likeCountTV, final FeedModel feedModel, final View likeWrapper) {
        final Call<ResponseBody> likeCall = Retrofit.getInstance().getInkService().likePost(mSharedHelper.getUserId(), postId, isLiking);
        likeCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    like(postId, isLiking, likeCountTV, feedModel, likeWrapper);
                    return;
                }
                if (response.body() == null) {
                    like(postId, isLiking, likeCountTV, feedModel, likeWrapper);
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String likesCount = jsonObject.optString("likes_count");
                    feedModel.setLikesCount(likesCount);
                    likeWrapper.setEnabled(true);
                    if (!likesCount.equals("0")) {
                        likeCountTV.setVisibility(View.VISIBLE);
                        if (Integer.parseInt(likesCount) > 1) {
                            likeCountTV.setText(likesCount + " " + getString(R.string.likesText));
                        } else {
                            likeCountTV.setText(likesCount + " " + getString(R.string.singleLikeText));
                        }
                    } else {
                        likeCountTV.setVisibility(View.INVISIBLE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                like(postId, isLiking, likeCountTV, feedModel, likeWrapper);
            }
        });
    }


    @Override
    public void onColorChanged() {
        if (mSharedHelper != null) {
            if (mSharedHelper.getFeedColor() != null) {
                feedRootLayout.setBackgroundColor(Color.parseColor(mSharedHelper.getFeedColor()));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SHARE_INTENT_RESULT:
                if (shareOutPutDir != null) {
                    shareOutPutDir.delete();
                }
                break;
        }
    }

    @Override
    public void onColorReset() {
        feedRootLayout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.feed_background_color));
    }

    private void checkColor() {
        if (mSharedHelper != null) {
            if (mSharedHelper.getFeedColor() != null) {
                feedRootLayout.setBackgroundColor(Color.parseColor(mSharedHelper.getFeedColor()));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST:
                if (!PermissionsChecker.isStoragePermissionGranted(getActivity())) {
                    Snackbar.make(mRecyclerView, getString(R.string.storagePermissions), Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                } else {
                    openShareIntent(intentBitmap, content.toString());
                }
                break;
            case LOCATION_PERMISSION_REQUEST:
                if (PermissionsChecker.isLocationPermissionGranted(getActivity())) {
                    initGreeting();
                }
                break;
        }
    }

    public void checkWidget() {
        if (greetingCard != null) {
            if (!failedLocate) {
                if (mSharedHelper.showGreeting()) {
                    initGreeting();
                    showGreetingCard();
                } else {
                    hideGreetingCard();
                }
            } else {
                hideGreetingCard();
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                // Create the LocationRequest object
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_NO_POWER)
                        .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                        .setFastestInterval(1 * 1000); // 1 second, in milliseconds
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                handleLocation(location);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleLocation(location);
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }
}
