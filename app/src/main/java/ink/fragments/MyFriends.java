package ink.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ink.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ink.activities.Profile;
import ink.adapters.FriendsAdapter;
import ink.models.FriendsModel;
import ink.utils.ErrorCause;
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
public class MyFriends extends Fragment {
    private SharedHelper mSharedHelper;
    private List<FriendsModel> mFriendsModelArrayList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private FriendsAdapter mFriendsAdapter;
    private FriendsModel mFriendsModel;
    private AVLoadingIndicatorView mFriendsLoading;
    private RelativeLayout mNoFriendsLayout;

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
        mRecyclerView = (RecyclerView) view.findViewById(R.id.friendsRecyclerView);
        mNoFriendsLayout = (RelativeLayout) view.findViewById(R.id.noFriendsLayout);
        mFriendsLoading = (AVLoadingIndicatorView) view.findViewById(R.id.friendsLoading);
        mFriendsAdapter = new FriendsAdapter(mFriendsModelArrayList, getActivity());

        mFriendsModelArrayList.clear();
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(itemAnimator);


        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), Profile.class);
                intent.putExtra("firstName", mFriendsModelArrayList.get(position).getFirstName());
                intent.putExtra("lastName", mFriendsModelArrayList.get(position).getLastName());
                intent.putExtra("phoneNumber", mFriendsModelArrayList.get(position).getPhoneNumber());
                intent.putExtra("id", mFriendsModelArrayList.get(position).getFriendId());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    RelativeLayout relativeLayout = (RelativeLayout) view;
                    CardView cardView = (CardView) relativeLayout.getChildAt(0);
                    RelativeLayout innerLayout = (RelativeLayout) cardView.getChildAt(0);
                    ImageView profileImage = (ImageView) innerLayout.findViewById(R.id.friendImage);
                    TextView username = (TextView) innerLayout.findViewById(R.id.friendName);
                    Pair<View, String> pair1 = Pair.create((View) profileImage, profileImage.getTransitionName());
                    Pair<View, String> pair2 = Pair.create((View) username, username.getTransitionName());
                    handleAnimation(intent, pair1, pair2);
                } else {
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        mRecyclerView.setAdapter(mFriendsAdapter);
        getFriends();
    }

    private void handleAnimation(Intent intent, Pair<View, String>... pairs) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pairs);
        startActivity(intent, options.toBundle());
    }

    private void getFriends() {
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
                            mFriendsLoading.setVisibility(View.GONE);
                            String cause = jsonObject.optString("cause");
                            String finalErrorMessage;
                            if (cause.equals(ErrorCause.NO_FRIENDS)) {
                                mNoFriendsLayout.setVisibility(View.VISIBLE);
                                mFriendsLoading.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.GONE);
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
                            JSONArray friendsArray = jsonObject.optJSONArray("friends");
                            for (int i = 0; i < friendsArray.length(); i++) {
                                JSONObject eachObject = friendsArray.optJSONObject(i);
                                String firstName = eachObject.optString("first_name");
                                String lastname = eachObject.optString("last_name");
                                String phoneNumber = eachObject.optString("phone_number");
                                // TODO: 2016-06-22  add image link to server

                                String imageLink = "";
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
                                mFriendsModel = new FriendsModel(name, imageLink, phoneNumber, friendId, firstName, lastname);
                                mFriendsModelArrayList.add(mFriendsModel);
                                mFriendsAdapter.notifyDataSetChanged();
                            }
                            mFriendsLoading.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        mFriendsLoading.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    mFriendsLoading.setVisibility(View.GONE);
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getActivity(), t.toString(), Toast.LENGTH_SHORT).show();
                mFriendsLoading.setVisibility(View.GONE);
            }
        });
    }


}
