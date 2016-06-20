package com.ink;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.ink.activities.HomeActivity;
import com.ink.adapters.FeedAdapter;
import com.ink.models.FeedModel;
import com.ink.utils.RecyclerTouchListener;
import com.ink.utils.RecyclerViewScrollDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by USER on 2016-06-21.
 */
public class Feed extends android.support.v4.app.Fragment {
    private List<FeedModel> mFeedModelArrayList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private FeedAdapter mAdapter;
    private FeedModel mFeedModel;
    private boolean isScrolledUp;
    private HomeActivity parent;

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

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        parent = ((HomeActivity) getActivity());

        mAdapter = new FeedAdapter(mFeedModelArrayList, getActivity());
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(itemAnimator);


        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        mRecyclerView.addOnScrollListener(new RecyclerViewScrollDetector() {
            @Override
            public void onScrollUp() {
                isScrolledUp = true;
            }

            @Override
            public void onScrollDown() {
                isScrolledUp = false;
            }

            @Override
            public void onDragging() {
                if (parent.getFab().getVisibility() != View.GONE && isScrolledUp) {
                    Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            parent.getFab().setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    parent.getFab().startAnimation(animation);
                }
            }

            @Override
            public void onIdle() {
                if (!isScrolledUp) {
                    if (parent.getFab().getVisibility() != View.VISIBLE) {
                        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                parent.getFab().setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        parent.getFab().startAnimation(animation);
                    }
                }
            }

        });
        mRecyclerView.setAdapter(mAdapter);
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            random.setSeed(i * 20);
            mFeedModel = new FeedModel("Good day.", getString(R.string.quoteOpen) + "This is an dummy content" + UUID.randomUUID().toString() + +random.nextLong() + getString(R.string.quoteClose));
            mFeedModelArrayList.add(mFeedModel);
            mAdapter.notifyDataSetChanged();
        }
    }
}
