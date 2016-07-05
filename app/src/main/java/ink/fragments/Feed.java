package ink.fragments;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.ink.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ink.activities.Comments;
import ink.adapters.FeedAdapter;
import ink.interfaces.FeedItemClick;
import ink.models.FeedModel;
import ink.utils.Animations;
import ink.utils.Constants;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-06-21.
 */
public class Feed extends android.support.v4.app.Fragment implements SwipeRefreshLayout.OnRefreshListener, FeedItemClick {
    private List<FeedModel> mFeedModelArrayList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private FeedAdapter mAdapter;
    private FeedModel mFeedModel;
    private SwipeRefreshLayout feedRefresh;
    private SharedHelper mSharedHelper;
    private AnimatedCircleLoadingView feedsLoading;
    private RelativeLayout noPostsWrapper;
    private boolean isOnCreate;

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
        isOnCreate = true;
        mSharedHelper = new SharedHelper(getActivity());
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        feedRefresh = (SwipeRefreshLayout) view.findViewById(R.id.feedRefresh);
        feedRefresh.setColorSchemeColors(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.colorPrimary));
        feedsLoading = (AnimatedCircleLoadingView) view.findViewById(R.id.circle_loading_view);
        noPostsWrapper = (RelativeLayout) view.findViewById(R.id.noPostsWrapper);
        feedsLoading.startIndeterminate();

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
    }

    @Override
    public void onRefresh() {
        mAdapter.setShouldStartAnimation(true);
        getFeeds();
    }

    private void getFeeds() {
        Call<ResponseBody> feedCal = Retrofit.getInstance().getInkService().getPosts(mSharedHelper.getUserId());
        feedCal.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getFeeds();
                    return;
                }
                if (response.body() == null) {
                    getFeeds();
                    return;
                }


                try {
                    String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);

                    if (mFeedModelArrayList != null) {
                        mFeedModelArrayList.clear();
                        mAdapter.notifyDataSetChanged();
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
                        mFeedModel = new FeedModel(id, imageLink, fileName, postBody,
                                posterId, address, datePosted, firstName, lastName, isLiked, likesCount);
                        mFeedModelArrayList.add(mFeedModel);
                        mAdapter.notifyDataSetChanged();
                    }

                    if (feedsLoading.getVisibility() == View.VISIBLE) {
                        feedsLoading.setVisibility(View.GONE);
                        feedsLoading.stopOk();
                    }
                    feedRefresh.setRefreshing(false);
                    mAdapter.notifyDataSetChanged();
                    if (mFeedModelArrayList.size() == 0) {
                        noPostsWrapper.setVisibility(View.VISIBLE);
                    } else {
                        noPostsWrapper.setVisibility(View.GONE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getFeeds();
            }
        });
    }

    @Override
    public void onCardViewClick(int position) {

    }

    @Override
    public void onAddressClick(int position) {
        String address = mFeedModelArrayList.get(position).getAddress();
        openGoogleMaps(address);
    }

    @Override
    public void onAttachmentClick(int position) {
        String fileName = mFeedModelArrayList.get(position).getFileName();
        showPromptDialog(fileName);
    }

    private void showPromptDialog(final String fileName) {
        System.gc();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(Constants.MAIN_URL + Constants.UPLOADED_FILES_DIR + fileName));
        request.setTitle(fileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        downloadManager.enqueue(request);

    }

    @Override
    public void onCardLongClick(int position) {

    }

    @Override
    public void onLikeClick(int position, ImageView likeView, TextView likeCountTV) {
        Animations.animateCircular(likeView);
        boolean isLiked = mFeedModelArrayList.get(position).isLiked();
        if (isLiked) {
            //must dislike
            like(mFeedModelArrayList.get(position).getId(), 1, likeCountTV, position);
            likeView.setBackgroundResource(R.drawable.like_inactive);
            mFeedModelArrayList.get(position).setLiked(false);
        } else {
            //must like
            like(mFeedModelArrayList.get(position).getId(), 0, likeCountTV, position);
            likeView.setBackgroundResource(R.drawable.like_active);
            mFeedModelArrayList.get(position).setLiked(true);
        }
    }

    @Override
    public void onCommentClicked(int position, View commentView) {
        Animations.animateCircular(commentView);
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
        if (isOnCreate) {
            mAdapter.setShouldStartAnimation(true);
            isOnCreate = false;
            getFeeds();
        }
        super.onResume();
    }

    private void like(final String postId, final int isLiking, final TextView likeCountTV, final int position) {
        final Call<ResponseBody> likeCall = Retrofit.getInstance().getInkService().likePost(mSharedHelper.getUserId(), postId, isLiking);
        likeCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    like(postId, isLiking, likeCountTV, position);
                    return;
                }
                if (response.body() == null) {
                    like(postId, isLiking, likeCountTV, position);
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    String likesCount = jsonObject.optString("likes_count");
                    mFeedModelArrayList.get(position).setLikesCount(likesCount);
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
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                like(postId, isLiking, likeCountTV, position);
            }
        });
    }
}
