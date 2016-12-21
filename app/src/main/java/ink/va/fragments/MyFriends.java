package ink.va.fragments;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ink.va.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ink.va.activities.HomeActivity;
import ink.va.activities.OpponentProfile;
import ink.va.adapters.FriendsAdapter;
import ink.va.animations.CircularPathAnimation;
import ink.va.interfaces.ColorChangeListener;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.FriendsModel;
import ink.va.models.UserSearchResponse;
import ink.va.models.UserSearchResult;
import ink.va.utils.Animations;
import ink.va.utils.DimDialog;
import ink.va.utils.ErrorCause;
import ink.va.utils.Keyboard;
import ink.va.utils.RealmHelper;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by USER on 2016-06-21.
 */
public class MyFriends extends Fragment implements RecyclerItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, ColorChangeListener {
    private SharedHelper mSharedHelper;
    private List<FriendsModel> mFriendsModelArrayList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private FriendsAdapter mFriendsAdapter;
    private FriendsModel mFriendsModel;
    private RelativeLayout mNoFriendsLayout;
    private HomeActivity parentActivity;
    private EditText personSearchField;
    private Animation slideIn;
    private Animation slideOut;
    private RelativeLayout personSearchWrapper;
    private boolean isClosed;
    private Call<ResponseBody> searchPersonCall;
    private Gson userSearchGSON;
    private SwipeRefreshLayout friendsSwipe;
    private CircularPathAnimation circularPathAnimation;
    private RelativeLayout searchWrapper;
    private RelativeLayout friendsRootLayout;
    private ImageView searchFriendIcon;
    private TextView searchText;
    private BroadcastReceiver updateReceiver;


    public static MyFriends newInstance() {
        MyFriends myFriends = new MyFriends();
        return myFriends;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_friends_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSharedHelper = new SharedHelper(getActivity());
        parentActivity = ((HomeActivity) getActivity());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.friendsRecyclerView);
        parentActivity.getClosePersonSearch().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSearchField(true);
            }
        });
        parentActivity.getSearchFriend().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchField();
            }
        });

        mNoFriendsLayout = (RelativeLayout) view.findViewById(R.id.noFriendsLayout);
        friendsSwipe = (SwipeRefreshLayout) view.findViewById(R.id.friendsSwipe);
        searchFriendIcon = (ImageView) view.findViewById(R.id.searchFriendIcon);
        searchText = (TextView) view.findViewById(R.id.searchText);
        personSearchWrapper = parentActivity.getPersonSearchWrapper();
        personSearchField = parentActivity.getPersonSearchField();
        slideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in);
        slideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out);
        mFriendsAdapter = new FriendsAdapter(mFriendsModelArrayList, getActivity());

        searchWrapper = (RelativeLayout) view.findViewById(R.id.searchWrapper);
        friendsRootLayout = (RelativeLayout) view.findViewById(R.id.friendsRootLayout);

        circularPathAnimation = new CircularPathAnimation(searchWrapper, 30);
        circularPathAnimation.setRepeatCount(Animation.INFINITE);
        circularPathAnimation.setRepeatMode(Animation.RESTART);
        circularPathAnimation.setDuration(1000);

        userSearchGSON = new Gson();

        showSearch();
        friendsSwipe.setOnRefreshListener(this);
        mFriendsModelArrayList.clear();
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(itemAnimator);

        mFriendsAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mFriendsAdapter);

        ((HomeActivity) getActivity()).setOnColorChangeListener(this);

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isDetached()) {
                    getFriends();
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(updateReceiver, new IntentFilter(getActivity().getPackageName() + "MyFriends"));

        checkColor();
        personSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().trim().isEmpty() && charSequence.length() > 0) {
                    doSearch(charSequence.toString());
                } else {
                    if (isAdded()) {
                        stopSearchAnimation(false);
                        if (mFriendsModelArrayList != null) {
                            mFriendsModelArrayList.clear();
                            mFriendsAdapter.notifyDataSetChanged();
                        }
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        getFriends();
    }


    private void startSearchAnimation() {
        if (searchWrapper.getVisibility() == View.GONE) {
            searchWrapper.setVisibility(View.VISIBLE);
        }
        searchText.setText(getString(R.string.searching));
        searchFriendIcon.setImageResource(0);
        searchFriendIcon.setBackgroundResource(R.drawable.searching_friend);
        searchFriendIcon.startAnimation(circularPathAnimation);
    }

    private void stopSearchAnimation(boolean showNoResult) {
        searchFriendIcon.setImageResource(0);
        if (showNoResult) {
            if (searchWrapper.getVisibility() == View.GONE) {
                searchWrapper.setVisibility(View.VISIBLE);
            }
            searchText.setText(getString(R.string.noFriendSearchResult));
            searchFriendIcon.setBackgroundResource(R.drawable.no_friend_result);
            searchFriendIcon.clearAnimation();
        } else {
            if (searchWrapper.getVisibility() == View.VISIBLE) {
                searchWrapper.setVisibility(View.GONE);
            }
            searchText.setText(getString(R.string.searching));
            searchFriendIcon.clearAnimation();
        }

    }

    private void doSearch(final String searchValue) {
        System.gc();
        clearAdapter();
        if (searchPersonCall != null) {
            searchPersonCall.cancel();
        }
        if (mNoFriendsLayout.getVisibility() == View.VISIBLE) {
            mNoFriendsLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        startSearchAnimation();
        searchPersonCall = Retrofit.getInstance().getInkService().searchPerson(mSharedHelper.getUserId(), searchValue);
        searchPersonCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    doSearch(searchValue);
                    return;
                }
                if (response.body() == null) {
                    doSearch(searchValue);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    UserSearchResponse userSearchResponse = userSearchGSON.fromJson(responseBody, UserSearchResponse.class);
                    if (userSearchResponse.success) {
                        ArrayList<UserSearchResult> userSearchResults = userSearchResponse.userSearchResults;
                        for (int i = 0; i < userSearchResults.size(); i++) {
                            UserSearchResult userSearchResult = userSearchResults.get(i);
                            mFriendsModel = new FriendsModel(userSearchResult.isFriend, Boolean.valueOf(userSearchResult.isSocialAccount), userSearchResult.firstName + " " + userSearchResult.lastName,
                                    userSearchResult.imageLink, "", userSearchResult.userId, userSearchResult.firstName, userSearchResult.lastName);
                            mFriendsModelArrayList.add(mFriendsModel);
                            mFriendsAdapter.notifyDataSetChanged();
                        }
                        stopSearchAnimation(false);
                    } else {
                        if (userSearchResponse.cause.equals(ErrorCause.NO_SEARCH_RESULT)) {
                            stopSearchAnimation(true);
                        } else {
                            stopSearchAnimation(false);
                        }
                    }
                } catch (IOException e) {
                    stopSearchAnimation(false);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void clearAdapter() {
        if (mFriendsModelArrayList != null) {
            mFriendsModelArrayList.clear();
            mFriendsAdapter.notifyDataSetChanged();
        }
    }


    private void handleAnimation(Intent intent, Pair<View, String>... pairs) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pairs);
        startActivity(intent, options.toBundle());
    }

    private void getFriends() {
        try {
            friendsSwipe.post(new Runnable() {
                @Override
                public void run() {
                    friendsSwipe.setRefreshing(true);
                }
            });
            clearAdapter();
            mFriendsAdapter.notifyDataSetChanged();
            stopSearchAnimation(false);
            final Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().getFriends(mSharedHelper.getUserId());
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String responseString = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            boolean success = jsonObject.optBoolean("success");
                            if (!success) {
                                friendsSwipe.setRefreshing(false);
                                String cause = jsonObject.optString("cause");
                                String finalErrorMessage;
                                if (cause.equals(ErrorCause.NO_FRIENDS)) {
                                    mNoFriendsLayout.setVisibility(View.VISIBLE);
                                    friendsSwipe.setRefreshing(false);
                                } else {
                                    finalErrorMessage = getString(R.string.serverErrorText);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage(finalErrorMessage);
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                }
                            } else {
                                if (mFriendsModelArrayList != null) {
                                    mFriendsModelArrayList.clear();
                                }
                                mNoFriendsLayout.setVisibility(View.GONE);
                                JSONArray friendsArray = jsonObject.optJSONArray("friends");
                                for (int i = 0; i < friendsArray.length(); i++) {
                                    JSONObject eachObject = friendsArray.optJSONObject(i);
                                    String firstName = eachObject.optString("first_name");
                                    String lastname = eachObject.optString("last_name");
                                    String phoneNumber = eachObject.optString("phone_number");
                                    String imageLink = eachObject.optString("image_link");
                                    String isSocialAccount = eachObject.optString("isSocialAccount");
                                    if (firstName.isEmpty()) {
                                        firstName = getString(R.string.noFirstname);
                                    }
                                    if (lastname.isEmpty()) {
                                        lastname = getString(R.string.noLastname);
                                    }
                                    String name = firstName + " " + lastname;
                                    if (phoneNumber.isEmpty()) {
                                        phoneNumber = getString(R.string.noPhone);
                                    }
                                    String friendId = eachObject.optString("friend_id");
                                    mFriendsModel = new FriendsModel(true, Boolean.valueOf(isSocialAccount), name, imageLink, phoneNumber, friendId, firstName, lastname);
                                    mFriendsModelArrayList.add(mFriendsModel);
                                    mFriendsAdapter.notifyDataSetChanged();
                                }
                                friendsSwipe.setRefreshing(false);
                            }
                        } catch (JSONException e) {
                            friendsSwipe.setRefreshing(false);
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        friendsSwipe.setRefreshing(false);
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    getFriends();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showSearch() {
        parentActivity.getHomeFab().close(true);
        parentActivity.getSearchFriend().setVisibility(View.VISIBLE);

    }

    private void hideSearch() {
        parentActivity.getHomeFab().close(true);
        personSearchField.setText("");
        parentActivity.getSearchFriend().setVisibility(View.GONE);

    }


    private void showSearchField() {
        isClosed = false;
        parentActivity.getHomeFab().close(true);
        personSearchWrapper.setVisibility(View.VISIBLE);
        hideSearch();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            personSearchWrapper.post(new Runnable() {
                @Override
                public void run() {
                    Animations.circularInFromTouch(personSearchWrapper, (int) personSearchWrapper.getX(), (int) personSearchWrapper.getY(), null);
                }
            });

        } else {

            personSearchWrapper.startAnimation(slideIn);
            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

    }

    @Override
    public void onResume() {
        if (searchWrapper.getVisibility() != View.VISIBLE) {
            stopSearchAnimation(false);
        }
        super.onResume();
    }

    private void hideSearchField(boolean loadFriends) {
        mFriendsModelArrayList.clear();
        mFriendsAdapter.notifyDataSetChanged();
        showSearch();
        if (loadFriends) {
            getFriends();
        }

        stopSearchAnimation(false);
        isClosed = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Animations.circularOut(personSearchWrapper, new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    personSearchWrapper.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        } else {
            personSearchWrapper.startAnimation(slideOut);
        }


        Keyboard.hideKeyboard(getActivity(), mRecyclerView);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                personSearchWrapper.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    @Override
    public void onItemClicked(int position, View view) {
        String opponentId = mFriendsModelArrayList.get(position).getFriendId();
        Intent intent = new Intent(getActivity(), OpponentProfile.class);
        intent.putExtra("firstName", mFriendsModelArrayList.get(position).getFirstName());
        intent.putExtra("lastName", mFriendsModelArrayList.get(position).getLastName());
        intent.putExtra("phoneNumber", mFriendsModelArrayList.get(position).getPhoneNumber());
        intent.putExtra("isFriend", mFriendsModelArrayList.get(position).isFriend());
        intent.putExtra("id", opponentId);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(int position) {

    }

    @Override
    public void onAdditionItemClick(int position, View view) {
        final FriendsModel friendsModel = mFriendsModelArrayList.get(position);
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.getMenu().add(getString(R.string.removeFromFriends));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equals(getString(R.string.removeFromFriends))) {
                    removeFriend(friendsModel.getFriendId());
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onItemClicked(Object object) {

    }

    private void removeFriend(final String friendId) {

        System.gc();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.removeFriend));
        builder.setMessage(getString(R.string.removefriendHint));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DimDialog.showDimDialog(getActivity(), getString(R.string.removingFriend));
                RealmHelper.getInstance().removeMessage(friendId, mSharedHelper.getUserId());
                Call<ResponseBody> removeFriendCall = Retrofit.getInstance().getInkService().removeFriend(mSharedHelper.getUserId(), friendId);
                removeFriendCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response == null) {
                            removeFriend(friendId);
                            return;
                        }
                        if (response.body() == null) {
                            removeFriend(friendId);
                            return;
                        }
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseBody);
                            boolean success = jsonObject.optBoolean("success");
                            DimDialog.hideDialog();
                            if (success) {
                                Snackbar.make(personSearchField, getString(R.string.friendRemoved), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                }).show();
                                getFriends();
                            }
                        } catch (IOException e) {
                            DimDialog.hideDialog();
                            e.printStackTrace();
                        } catch (JSONException e) {
                            DimDialog.hideDialog();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        removeFriend(friendId);
                    }
                });
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();


    }

    @Override
    public void onRefresh() {
        getFriends();
    }

    @Override
    public void onColorChanged() {
        if (mSharedHelper != null) {
            if (mSharedHelper.getFriendsColor() != null) {
                friendsRootLayout.setBackgroundColor(Color.parseColor(mSharedHelper.getFriendsColor()));
            }
        }
    }

    @Override
    public void onColorReset() {
        friendsRootLayout.setBackgroundColor(0);
    }

    private void checkColor() {
        if (mSharedHelper != null) {
            if (mSharedHelper.getFriendsColor() != null) {
                friendsRootLayout.setBackgroundColor(Color.parseColor(mSharedHelper.getFriendsColor()));
            }
        }
    }
}
