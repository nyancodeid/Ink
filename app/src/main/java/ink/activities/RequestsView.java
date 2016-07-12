package ink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.ink.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.adapters.RequestsAdapter;
import ink.interfaces.RequestListener;
import ink.models.RequestsModel;
import ink.utils.Constants;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestsView extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, RequestListener {

    private SharedHelper sharedHelper;
    @Bind(R.id.requestsRecycler)
    RecyclerView requestsRecycler;
    @Bind(R.id.requestSwipe)
    SwipeRefreshLayout requestSwipe;
    @Bind(R.id.noRequestsLayout)
    RelativeLayout noRequestsLayout;
    private RequestsAdapter requestsAdapter;
    private RequestsModel requestsModel;
    private List<RequestsModel> requestsModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_view);
        ButterKnife.bind(this);
        requestSwipe.setOnRefreshListener(this);
        requestsModels = new ArrayList<>();
        requestsAdapter = new RequestsAdapter(requestsModels, this);
        requestsAdapter.setRequestListener(this);
        sharedHelper = new SharedHelper(this);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        requestsRecycler.setLayoutManager(new LinearLayoutManager(this));
        requestsRecycler.setItemAnimator(itemAnimator);
        requestsRecycler.setAdapter(requestsAdapter);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.myRequests));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getMyRequests();
    }

    private void getMyRequests() {
        if (requestsModels != null) {
            requestsModels.clear();
            requestsAdapter.notifyDataSetChanged();
        }
        requestSwipe.post(new Runnable() {
            @Override
            public void run() {
                requestSwipe.setRefreshing(true);
            }
        });
        Call<ResponseBody> myRequestsCall = Retrofit.getInstance().getInkService().getMyRequests(sharedHelper.getUserId());
        myRequestsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getMyRequests();
                    return;
                }
                if (response.body() == null) {
                    getMyRequests();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        JSONArray jsonArray = jsonObject.optJSONArray("requests");
                        if (jsonArray.length() <= 0) {
                            noRequestsLayout.setVisibility(View.VISIBLE);
                            requestSwipe.setRefreshing(false);
                            return;
                        }
                        noRequestsLayout.setVisibility(View.GONE);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject eachObject = jsonArray.getJSONObject(i);
                            String groupOwnerId = eachObject.optString("group_owner_id");
                            String requesterId = eachObject.optString("requester_id");
                            String requesterName = eachObject.optString("requester_name");
                            String requesterImage = eachObject.optString("requester_image");
                            String requestedGroupId = eachObject.optString("requested_group_id");
                            String requestId = eachObject.optString("request_id");
                            String groupName = eachObject.optString("group_name");
                            requestsModel = new RequestsModel(groupOwnerId, requesterId, requesterName, requesterImage, requestedGroupId,
                                    requestId, groupName);
                            requestsModels.add(requestsModel);
                            requestsAdapter.notifyDataSetChanged();
                        }
                        requestSwipe.setRefreshing(false);

                    } else {
                        getMyRequests();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getMyRequests();
            }
        });
    }

    @Override
    public void onRefresh() {
        getMyRequests();
    }

    @Override
    public void onAcceptClicked(int position) {
        acceptRequest(position);
    }


    @Override
    public void onDeclineClicked(int position) {
        denyRequest(position);
    }

    @Override
    public void onItemClicked(int position) {
        RequestsModel singleModel = requestsModels.get(position);
        String name = singleModel.getRequesterName();
        String[] splited = name.split("\\s+");
        String firstName = splited[0];
        String lastName = splited[1];
        Intent intent = new Intent(getApplicationContext(), OpponentProfile.class);
        intent.putExtra("id", singleModel.getRequesterId());
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void acceptRequest(final int position) {
        final RequestsModel requestsModel = requestsModels.get(position);
        Call<ResponseBody> requestCall = Retrofit.getInstance().getInkService().respondToRequest(Constants.TYPE_ACCEPT_REQUEST,
                requestsModel.getRequesterId(), requestsModel.getRequesterName(), requestsModel.getRequesterImage(),
                requestsModel.getRequestedGroupId());
        requestCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    acceptRequest(position);
                    return;
                }
                if (response.body() == null) {
                    acceptRequest(position);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        Snackbar.make(noRequestsLayout, getString(R.string.requestAccepted), Snackbar.LENGTH_SHORT).show();
                        requestsModels.clear();
                        getMyRequests();
                    } else {
                        denyRequest(position);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                acceptRequest(position);
            }
        });
    }

    private void denyRequest(final int position) {
        RequestsModel requestsModel = requestsModels.get(position);
        Call<ResponseBody> requestCall = Retrofit.getInstance().getInkService().respondToRequest(Constants.TYPE_DENY_REQUEST,
                requestsModel.getRequesterId(), requestsModel.getRequesterName(), requestsModel.getRequesterImage(),
                requestsModel.getRequestedGroupId());
        requestCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response == null) {
                    denyRequest(position);
                    return;
                }
                if (response.body() == null) {
                    denyRequest(position);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        Snackbar.make(noRequestsLayout, getString(R.string.requestDenied), Snackbar.LENGTH_SHORT).show();
                        requestsModels.clear();
                        getMyRequests();
                    } else {
                        denyRequest(position);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                denyRequest(position);
            }
        });
    }

}
