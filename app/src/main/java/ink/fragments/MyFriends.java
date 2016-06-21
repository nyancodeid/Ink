package ink.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        mFriendsLoading = (AVLoadingIndicatorView) view.findViewById(R.id.friendsLoading);
        mFriendsAdapter = new FriendsAdapter(mFriendsModelArrayList, getActivity());

        mFriendsModelArrayList.clear();
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(itemAnimator);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), Profile.class);
                RelativeLayout relativeLayout = (RelativeLayout) view;
                CardView cardView = (CardView) relativeLayout.getChildAt(0);
                RelativeLayout innerLayout = (RelativeLayout) cardView.getChildAt(0);
                TextView textView = (TextView) innerLayout.findViewById(R.id.friendName);
                intent.putExtra("contact", "hello");
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), (View) textView, "profile");
                startActivity(intent, options.toBundle());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        mRecyclerView.setAdapter(mFriendsAdapter);
        getFreinds();
    }

    private void getFreinds() {
        final Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().getFriends(mSharedHelper.getUserId());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responseString = response.body().string();
                    Log.d("Fasfhasjkfa", "onResponse: " + responseString);
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);
                        boolean success = jsonObject.optBoolean("success");
                        if (!success) {
                            mFriendsLoading.setVisibility(View.GONE);
                            String cause = jsonObject.optString("cause");
                            String finalErrorMessage;
                            if (cause.equals(ErrorCause.NO_FRIENDS)) {

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
                                String name = eachObject.optString("first_name") + " " + eachObject.optString("last_name");
                                String phoneNumber = eachObject.optString("phone_number");
                                String imageLink = "";
                                mFriendsModel = new FriendsModel(name, phoneNumber, imageLink);
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
