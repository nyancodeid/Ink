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

import com.ink.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ink.adapters.FeedAdapter;
import ink.interfaces.FeedItemClick;
import ink.models.FeedModel;
import ink.utils.Constants;
import ink.utils.RecyclerTouchListener;
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
    private AVLoadingIndicatorView feedsLoading;
    private RelativeLayout noPostsWrapper;

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

        mSharedHelper = new SharedHelper(getActivity());
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        feedRefresh = (SwipeRefreshLayout) view.findViewById(R.id.feedRefresh);
        feedRefresh.setColorSchemeColors(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.colorPrimary));
        feedsLoading = (AVLoadingIndicatorView) view.findViewById(R.id.feedsLoading);
        noPostsWrapper = (RelativeLayout) view.findViewById(R.id.noPostsWrapper);

        feedRefresh.setOnRefreshListener(this);
        mAdapter = new FeedAdapter(mFeedModelArrayList, getActivity());
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(itemAnimator);
        mAdapter.setOnFeedClickListener(this);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onRefresh() {
        getFeeds();
    }

    private void getFeeds() {
        if (mFeedModelArrayList != null) {
            mFeedModelArrayList.clear();
        }
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
                        mFeedModel = new FeedModel(id, imageLink, fileName, postBody,
                                posterId, address, datePosted, firstName, lastName, isLiked);
                        mFeedModelArrayList.add(mFeedModel);
                        mAdapter.notifyDataSetChanged();
                    }

                    if (feedsLoading.getVisibility() == View.VISIBLE) {
                        feedsLoading.setVisibility(View.GONE);
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
    public void onLikeClick(int position, ImageView likeView, TextView likeTV) {
        boolean isLiked = mFeedModelArrayList.get(position).isLiked();
        if (isLiked) {
            //must dislike
            likeView.setBackgroundResource(R.drawable.like_inactive);
            likeTV.setTextColor(ContextCompat.getColor(getActivity(), Constants.TEXT_VIEW_DEFAULT_COLOR));
            mFeedModelArrayList.get(position).setLiked(false);
        } else {
            //must like
            likeView.setBackgroundResource(R.drawable.like_active);
            likeTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            mFeedModelArrayList.get(position).setLiked(true);
        }
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
        getFeeds();
        super.onResume();
    }
}
