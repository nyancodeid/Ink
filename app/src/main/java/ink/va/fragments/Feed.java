package ink.va.fragments;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import ink.va.utils.PermissionsChecker;
import ink.va.utils.PopupMenu;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.ink.va.R.string.share;

/**
 * Created by USER on 2016-06-21.
 */
public class Feed extends android.support.v4.app.Fragment implements SwipeRefreshLayout.OnRefreshListener,
        FeedItemClick, ColorChangeListener {
    private static final String TAG = Feed.class.getSimpleName();
    private static final int SHARE_INTENT_RESULT = 5;
    private static final int STORAGE_PERMISSION_REQUEST = 115;
    private List<FeedModel> mFeedModelArrayList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private FeedAdapter mAdapter;
    private FeedModel mFeedModel;
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

    public static Feed newInstance() {
        Feed feed = new Feed();
        return feed;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.feed_layout, container, false);
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
        noPostsWrapper = (RelativeLayout) view.findViewById(R.id.noPostsWrapper);
        deleteDialog = new ProgressDialog(getActivity());
        deleteDialog.setTitle(getString(R.string.deleting));
        deleteDialog.setMessage(getString(R.string.deletingPost));
        deleteDialog.setCancelable(false);

        configureSearchField();

        feedRefresh.setOnRefreshListener(this);
        mAdapter = new FeedAdapter(mFeedModelArrayList, getActivity());
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
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastItem = layoutManager.findLastCompletelyVisibleItemPosition();
                newFeedsLayout.setVisibility(View.GONE);
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        System.gc();
        ((HomeActivity) getActivity()).setOnColorChangeListener(this);
        if (mOffset == 0) {
            mOffset = 10;
        }
        checkColor();
        getFeeds(0, mOffset, true, false, false);
    }

    private void configureSearchField() {
        parentActivity.getHomeFab().close(true);
        if (parentActivity.getSearchFriend().getVisibility() == View.VISIBLE) {
            parentActivity.getSearchFriend().setVisibility(View.GONE);
        }
        if (parentActivity.getPersonSearchWrapper().getVisibility() == View.VISIBLE) {
            parentActivity.getPersonSearchWrapper().setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        getFeeds(0, mOffset, true, false, false);
    }

    private void getFeeds(final int offset,
                          final int count,
                          final boolean clearItems,
                          final boolean newDataLoading,
                          final boolean showNewFeed) {
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
        Call<ResponseBody> feedCal = Retrofit.getInstance().getInkService().getPosts(mSharedHelper.getUserId(), String.valueOf(offset), String.valueOf(count));
        feedCal.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getFeeds(offset, count, clearItems, newDataLoading, showNewFeed);
                    return;
                }
                if (response.body() == null) {
                    getFeeds(offset, count, clearItems, newDataLoading, showNewFeed);
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);

                    if (clearItems) {
                        if (mFeedModelArrayList != null) {
                            mFeedModelArrayList.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject eachObject = jsonArray.optJSONObject(i);
                        String id = eachObject.optString("id");
                        String imageLink = eachObject.optString("image_link");
                        String fileName = eachObject.optString("file_name");
                        String postBody = eachObject.optString("post_body");
                        String posterId = eachObject.optString("poster_id");
                        String address = eachObject.optString("address");
                        String datePosted = eachObject.optString("date_posted");
                        String firstName = eachObject.optString("first_name");
                        String lastName = eachObject.optString("last_name");
                        boolean isLiked = eachObject.optBoolean("is_liked");
                        String likesCount = eachObject.optString("likes_count");
                        String type = eachObject.optString("type");
                        String groupName = eachObject.optString("group_name");
                        boolean isSocialAccount = eachObject.optBoolean("isSocialAccount");
                        boolean isFriend = eachObject.optBoolean("isFriend");
                        String groupColor = eachObject.optString("groupColor");
                        String groupImage = eachObject.optString("groupImage");
                        String groupDescription = eachObject.optString("groupDescription");
                        String groupOwnerId = eachObject.optString("groupOwnerId");
                        String ownerImage = eachObject.optString("ownerImage");
                        String groupOwnerName = eachObject.optString("groupOwnerName");
                        String commentsCount = eachObject.optString("commentsCount");
                        String count = eachObject.optString("count");
                        String groupMessageFileName = eachObject.optString("group_message_file_name");
                        boolean isMember = eachObject.optBoolean("isMember");
                        mFeedModel = new FeedModel(isFriend, isSocialAccount, id, imageLink, fileName, postBody,
                                posterId, address, datePosted, firstName, lastName, isLiked, likesCount, type, groupName, count, ownerImage, groupOwnerId,
                                groupDescription, groupImage, groupColor, groupOwnerName, commentsCount, groupMessageFileName);
                        mFeedModelArrayList.add(mFeedModel);
                        mAdapter.notifyDataSetChanged();
                    }
                    mAdapter.notifyDataSetChanged();
                    feedRefresh.post(new Runnable() {
                        @Override
                        public void run() {
                            feedRefresh.setRefreshing(false);
                        }
                    });

                    if (mFeedModelArrayList.size() == 0) {
                        noPostsWrapper.setVisibility(View.VISIBLE);
                    } else {
                        noPostsWrapper.setVisibility(View.GONE);
                    }
                    if (!clearItems) {
                        parentActivity.getToolbar().setTitle(HomeActivity.FEED);
                    }
                    if (showNewFeed) {
                        if (jsonArray.length() > 0) {
                            newFeedsLayout.setVisibility(View.VISIBLE);
                        } else {
                            newFeedsLayout.setVisibility(View.GONE);
                        }
                    }
                    if (newDataLoading) {
                        mOffset += 10;
                    }
                } catch (IOException e) {
                    feedRefresh.post(new Runnable() {
                        @Override
                        public void run() {
                            feedRefresh.setRefreshing(false);
                        }
                    });

                    e.printStackTrace();
                } catch (JSONException e) {
                    feedRefresh.post(new Runnable() {
                        @Override
                        public void run() {
                            feedRefresh.setRefreshing(false);
                        }
                    });
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getFeeds(offset, count, clearItems, newDataLoading, showNewFeed);
            }
        });
    }

    @Override
    public void onCardViewClick(int position, String type) {
        switch (type) {
            case Constants.WALL_TYPE_GROUP_MESSAGE:
                startGroupActivity(position);
                break;
            case Constants.WALL_TYPE_POST:
                startCommentActivity(position);
                break;
        }

    }

    private void startGroupActivity(int position) {

        Intent intent = new Intent(getActivity(), SingleGroupView.class);
        intent.putExtra("groupName", mFeedModelArrayList.get(position).getGroupName());
        intent.putExtra("groupId", mFeedModelArrayList.get(position).getId());
        intent.putExtra("groupColor", mFeedModelArrayList.get(position).getGroupColor());
        intent.putExtra("groupImage", mFeedModelArrayList.get(position).getGroupImage());
        intent.putExtra("groupDescription", mFeedModelArrayList.get(position).getGroupDescription());
        intent.putExtra("groupOwnerId", mFeedModelArrayList.get(position).getGroupOwnerId());
        intent.putExtra("groupOwnerName", mFeedModelArrayList.get(position).getGroupOwnerName());
        intent.putExtra("count", mFeedModelArrayList.get(position).getCount());
        intent.putExtra("ownerImage", mFeedModelArrayList.get(position).getOwnerImage());
        intent.putExtra("isSocialAccount", mFeedModelArrayList.get(position).isSocialAccount());
        intent.putExtra("isMember", true);
        intent.putExtra("isFriend", mFeedModelArrayList.get(position).isFriend());
        startActivity(intent);
    }


    private void startCommentActivity(int position) {
        FeedModel clickedModel = mFeedModelArrayList.get(position);
        Intent intent = new Intent(getActivity(), Comments.class);
        intent.putExtra("postId", clickedModel.getId());
        intent.putExtra("userImage", clickedModel.getUserImage());
        intent.putExtra("postBody", clickedModel.getContent());
        intent.putExtra("attachment", clickedModel.getFileName());
        intent.putExtra("location", clickedModel.getAddress());
        intent.putExtra("name", clickedModel.getFirstName() + " " + clickedModel.getLastName());
        intent.putExtra("date", clickedModel.getDatePosted());
        intent.putExtra("likesCount", clickedModel.getLikesCount());
        intent.putExtra("isLiked", clickedModel.isLiked());
        intent.putExtra("isSocialAccount", clickedModel.isSocialAccount());
        intent.putExtra("ownerId", clickedModel.getPosterId());

        intent.putExtra("hasAttachment", clickedModel.hasAttachment());
        intent.putExtra("hasAddress", clickedModel.hasAddress());
        intent.putExtra("attachmentName", clickedModel.getFileName());
        intent.putExtra("addressName", clickedModel.getAddress());
        intent.putExtra("postId", clickedModel.getId());
        intent.putExtra("postBody", clickedModel.getContent());

        intent.putExtra("isPostOwner", clickedModel.isPostOwner());
        intent.putExtra("isFriend", clickedModel.isFriend());


        startActivity(intent);
    }

    @Override
    public void onAddressClick(int position) {
        String address = mFeedModelArrayList.get(position).getAddress();
        openGoogleMaps(address);
    }

    @Override
    public void onAttachmentClick(int position) {
        String fileName = mFeedModelArrayList.get(position).getFileName();
        String userId = mFeedModelArrayList.get(position).getPosterId();
        showPromptDialog(fileName);
    }

    private void showPromptDialog(final String fileName) {

        int index = fileName.indexOf(":");
        String finalFileName = fileName.substring(index + 1, fileName.length());

        System.gc();
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
    public void onCardLongClick(int position) {

    }

    @Override
    public void onLikeClick(int position, ImageView likeView, TextView likeCountTV, View likeWrapper) {
        Animations.animateCircular(likeView);
        boolean isLiked = mFeedModelArrayList.get(position).isLiked();
        likeWrapper.setEnabled(false);
        if (isLiked) {
            //must dislike
            like(mFeedModelArrayList.get(position).getId(), 1, likeCountTV, position, likeWrapper);
            likeView.setBackgroundResource(R.drawable.like_inactive);
            mFeedModelArrayList.get(position).setLiked(false);
        } else {
            //must like
            like(mFeedModelArrayList.get(position).getId(), 0, likeCountTV, position, likeWrapper);
            likeView.setBackgroundResource(R.drawable.like_active);
            mFeedModelArrayList.get(position).setLiked(true);
        }
    }

    @Override
    public void onCommentClicked(int position, View commentView) {
        Animations.animateCircular(commentView);
        startCommentActivity(position);
    }

    @Override
    public void onMoreClicked(final int position, View view) {
        final FeedModel singleModel = mFeedModelArrayList.get(position);
        if (singleModel.isPostOwner()) {
            ink.va.utils.PopupMenu.showPopUp(getActivity(), view, new ItemClickListener<MenuItem>() {
                @Override
                public void onItemClick(MenuItem clickedItem) {
                    switch (clickedItem.getItemId()) {
                        case 0:
                            Intent intent = new Intent(getActivity(), MakePost.class);
                            intent.putExtra("isEditing", true);
                            intent.putExtra("hasAttachment", singleModel.hasAttachment());
                            intent.putExtra("hasAddress", singleModel.hasAddress());
                            intent.putExtra("attachmentName", singleModel.getFileName());
                            intent.putExtra("addressName", singleModel.getAddress());
                            intent.putExtra("postId", singleModel.getId());
                            intent.putExtra("postBody", singleModel.getContent());
                            startActivity(intent);

                            break;
                        case 1:
                            System.gc();
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle(getString(R.string.deletePost));
                            builder.setMessage(getString(R.string.areYouSure));
                            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    deleteDialog.show();
                                    deletePost(mFeedModelArrayList.get(position).getId(), mFeedModelArrayList.get(position).getFileName());
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
                        case 2:
                            openShareView(singleModel);
                            break;
                    }
                }
            }, getString(R.string.edit), getString(R.string.delete), getString(share));
        } else {
            PopupMenu.showPopUp(getActivity(), view, new ItemClickListener<MenuItem>() {
                @Override
                public void onItemClick(MenuItem clickedItem) {
                    switch (clickedItem.getItemId()) {
                        case 0:
                            Intent intent = new Intent(getActivity(), OpponentProfile.class);
                            intent.putExtra("id", singleModel.getPosterId());
                            intent.putExtra("firstName", singleModel.getFirstName());
                            intent.putExtra("lastName", singleModel.getLastName());
                            intent.putExtra("isFriend", singleModel.isFriend());
                            startActivity(intent);
                            break;
                        case 1:
                            openShareView(singleModel);
                            break;
                    }

                }
            }, getString(R.string.viewProfile), getString(share));
        }
    }

    private void openShareView(final FeedModel singleModel) {

        content = new StringBuilder();
        content.append(singleModel.getContent());

        if (singleModel.hasAttachment()) {
            Ion.with(getActivity()).load(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + Uri.encode(singleModel.getFileName())).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    if (e != null) {
                        if (singleModel.hasAddress()) {
                            content.append("\n" + getString(R.string.locatedAt) + " " + singleModel.getAddress());
                        }
                        openShareIntent(intentBitmap, content.toString());
                    } else {
                        intentBitmap = result;
                        if (singleModel.hasAddress()) {
                            content.append("\n" + getString(R.string.locatedAt) + " " + singleModel.getAddress());
                        }
                        openShareIntent(intentBitmap, content.toString());
                    }
                }
            });
        } else {
            intentBitmap = null;
            if (singleModel.hasAddress()) {
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
    public void onImageClicked(int position) {
        FeedModel feedModel = mFeedModelArrayList.get(position);
        Intent intent = new Intent(getActivity(), FullscreenActivity.class);
        String encodedFileName = Uri.encode(feedModel.getFileName());
        intent.putExtra("link", Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + encodedFileName);
        startActivity(intent);
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
                        deleteDialog.dismiss();
                        Snackbar.make(mRecyclerView, getString(R.string.postDeleted), Snackbar.LENGTH_SHORT).show();
                    } else {
                        deleteDialog.dismiss();
                        Snackbar.make(mRecyclerView, getString(R.string.couldNotDeletePost), Snackbar.LENGTH_LONG).show();
                    }
                    getFeeds(0, mOffset, true, false, false);
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

    private void like(final String postId, final int isLiking, final TextView likeCountTV, final int position, final View likeWrapper) {
        final Call<ResponseBody> likeCall = Retrofit.getInstance().getInkService().likePost(mSharedHelper.getUserId(), postId, isLiking);
        likeCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    like(postId, isLiking, likeCountTV, position, likeWrapper);
                    return;
                }
                if (response.body() == null) {
                    like(postId, isLiking, likeCountTV, position, likeWrapper);
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String likesCount = jsonObject.optString("likes_count");
                    mFeedModelArrayList.get(position).setLikesCount(likesCount);
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
                like(postId, isLiking, likeCountTV, position, likeWrapper);
            }
        });
    }

    public void triggerFeedUpdate() {
        System.gc();
        if (mOffset == 0) {
            mOffset = 10;
        }
        getFeeds(0, mOffset, true, false, false);
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
        }
    }
}
