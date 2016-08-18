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
import ink.utils.DimDialog;
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
        if (!requestSwipe.isRefreshing()) {
            requestSwipe.post(new Runnable() {
                @Override
                public void run() {
                    requestSwipe.setRefreshing(true);
                }
            });
        }

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

                            String requesterId = eachObject.optString("requester_id");
                            String requesterName = eachObject.optString("requester_name");
                            String requesterImage = eachObject.optString("requester_image");
                            String requestId = eachObject.optString("request_id");
                            boolean isSocialAccount = eachObject.optBoolean("isSocialAccount");
                            String isFriend = eachObject.optString("isFriend");

                            String type = eachObject.optString("type");
                            String groupName = "";
                            String groupOwnerId = "";
                            String requestedGroupId = "";
                            if (type.equals(Constants.REQUEST_RESPONSE_TYPE_GROUP)) {
                                groupName = eachObject.optString("group_name");
                                groupOwnerId = eachObject.optString("group_owner_id");
                                requestedGroupId = eachObject.optString("requested_group_id");
                            }

                            requestsModel = new RequestsModel(type, isSocialAccount, Boolean.valueOf(isFriend), groupOwnerId, requesterId, requesterName, requesterImage, requestedGroupId,
                                    requestId, groupName);
                            requestsModels.add(requestsModel);
                            requestsAdapter.notifyDataSetChanged();
                        }
                        requestSwipe.setRefreshing(false);

                    } else {
                        getMyRequests();
                    }
                } catch (IOException e) {
                    requestSwipe.setRefreshing(false);
                    e.printStackTrace();
                } catch (JSONException e) {
                    requestSwipe.setRefreshing(false);
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
        DimDialog.showDimDialog(RequestsView.this, getString(R.string.accepting));
        RequestsModel requestsModel = requestsModels.get(position);
        if (requestsModel.getType().equals(Constants.REQUEST_RESPONSE_TYPE_GROUP)) {
            acceptGroupRequest(position);
        } else if (requestsModel.getType().equals(Constants.REQUEST_RESPONSE_TYPE_FRIEND_REQUEST)) {
            acceptFriendRequest(position);
        } else if (requestsModel.getType().equals(Constants.REQUEST_RESPONSE_TYPE_LOCATION_REQUEST)) {
            acceptLocationRequest(position);
        }

    }


    @Override
    public void onDeclineClicked(int position) {
        DimDialog.showDimDialog(RequestsView.this, getString(R.string.declining));
        RequestsModel requestsModel = requestsModels.get(position);
        if (requestsModel.getType().equals(Constants.REQUEST_RESPONSE_TYPE_GROUP)) {
            denyGroupRequest(position);
        } else if (requestsModel.getType().equals(Constants.REQUEST_RESPONSE_TYPE_FRIEND_REQUEST)) {
            denyFriendRequest(position);
        } else if (requestsModel.getType().equals(Constants.REQUEST_RESPONSE_TYPE_LOCATION_REQUEST)) {
            declineLocationRequest(position);
        }

    }

    private void acceptLocationRequest(final int position) {
        requestSwipe.post(new Runnable() {
            @Override
            public void run() {
                requestSwipe.setRefreshing(true);
            }
        });
        final RequestsModel requestsModel = requestsModels.get(position);
        Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().respondToRequest(Constants.RESPOND_TYPE_ACCEPT_LOCATION_REQUEST, sharedHelper.getUserId(),
                sharedHelper.getFirstName() + " " + sharedHelper.getLastName(), requestsModel.getRequestId(), requestsModel.getRequesterId());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    acceptLocationRequest(position);
                    return;
                }
                if (response.body() == null) {
                    acceptLocationRequest(position);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    DimDialog.hideDialog();
                    if (success) {
                        // TODO: 8/18/2016 start session chat
                        String fullNameParts[] = requestsModel.getRequesterName().split("\\s");
                        String firstName;
                        String lastName;
                        try {
                            firstName = fullNameParts[0];
                            lastName = fullNameParts[1];
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            firstName = requestsModel.getRequesterName();
                            lastName = "";
                        }

                        Intent intent = new Intent(getApplicationContext(), Chat.class);
                        intent.putExtra("firstName", firstName);
                        intent.putExtra("lastName", lastName);
                        intent.putExtra("hasSession", true);
                        intent.putExtra("opponentId", requestsModel.getRequesterId());
                        intent.putExtra("opponentImage", requestsModel.getRequesterImage());
                        intent.putExtra("isSocialAccount", requestsModel.isSocialAccount());
                        startActivity(intent);

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
                acceptFriendRequest(position);
            }
        });
    }

    private void declineLocationRequest(final int position) {
        requestSwipe.post(new Runnable() {
            @Override
            public void run() {
                requestSwipe.setRefreshing(true);
            }
        });
        RequestsModel requestsModel = requestsModels.get(position);
        Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().respondToRequest(Constants.RESPOND_TYPE_DENY_LOCATION_REQUEST, requestsModel.getRequestId(),
                sharedHelper.getFirstName() + " "
                        + sharedHelper.getLastName(),
                requestsModel.getRequesterId(), sharedHelper.getUserId());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    declineLocationRequest(position);
                    return;
                }
                if (response.body() == null) {
                    declineLocationRequest(position);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        DimDialog.hideDialog();
                        Snackbar.make(requestsRecycler, getString(R.string.friendRequestDenied), Snackbar.LENGTH_LONG).show();
                        getMyRequests();
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
                declineLocationRequest(position);
            }
        });
    }

    private void acceptFriendRequest(final int position) {
        requestSwipe.post(new Runnable() {
            @Override
            public void run() {
                requestSwipe.setRefreshing(true);
            }
        });
        RequestsModel requestsModel = requestsModels.get(position);
        Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().respondToRequest(Constants.RESPOND_TYPE_ACCEPT_FRIEND_REQUEST, sharedHelper.getUserId(),
                sharedHelper.getFirstName() + " " + sharedHelper.getLastName(), "", requestsModel.getRequesterId());
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    acceptFriendRequest(position);
                    return;
                }
                if (response.body() == null) {
                    acceptFriendRequest(position);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    DimDialog.hideDialog();
                    if (success) {
                        Snackbar.make(requestsRecycler, getString(R.string.friendRequestAccepted), Snackbar.LENGTH_LONG).show();
                        getMyRequests();
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
                acceptFriendRequest(position);
            }
        });
    }


    private void denyFriendRequest(final int position) {
        requestSwipe.post(new Runnable() {
            @Override
            public void run() {
                requestSwipe.setRefreshing(true);
            }
        });
        RequestsModel requestsModel = requestsModels.get(position);
        Call<ResponseBody> responseBodyCall = Retrofit.getInstance().getInkService().respondToRequest(Constants.RESPOND_TYPE_DENY_FRIEND_REQUEST, requestsModel.getRequestId(), "",
                "", "");
        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    denyFriendRequest(position);
                    return;
                }
                if (response.body() == null) {
                    denyFriendRequest(position);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        DimDialog.hideDialog();
                        Snackbar.make(requestsRecycler, getString(R.string.friendRequestDenied), Snackbar.LENGTH_LONG).show();
                        getMyRequests();
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
                denyFriendRequest(position);
            }
        });
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
        intent.putExtra("isFriend", singleModel.isFriend());
        startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        DimDialog.hideDialog();
        super.onDestroy();
    }

    private void acceptGroupRequest(final int position) {
        requestSwipe.post(new Runnable() {
            @Override
            public void run() {
                requestSwipe.setRefreshing(true);
            }
        });
        final RequestsModel requestsModel = requestsModels.get(position);
        Call<ResponseBody> requestCall = Retrofit.getInstance().getInkService().respondToRequest(Constants.TYPE_ACCEPT_REQUEST,
                requestsModel.getRequesterId(), requestsModel.getRequesterName(), requestsModel.getRequesterImage(),
                requestsModel.getRequestedGroupId());
        requestCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    acceptGroupRequest(position);
                    return;
                }
                if (response.body() == null) {
                    acceptGroupRequest(position);
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
                        denyGroupRequest(position);
                    }
                    DimDialog.hideDialog();
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
                acceptGroupRequest(position);
            }
        });
    }

    private void denyGroupRequest(final int position) {
        requestSwipe.post(new Runnable() {
            @Override
            public void run() {
                requestSwipe.setRefreshing(true);
            }
        });
        RequestsModel requestsModel = requestsModels.get(position);
        Call<ResponseBody> requestCall = Retrofit.getInstance().getInkService().respondToRequest(Constants.TYPE_DENY_REQUEST,
                requestsModel.getRequesterId(), requestsModel.getRequesterName(), requestsModel.getRequesterImage(),
                requestsModel.getRequestedGroupId());
        requestCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response == null) {
                    denyGroupRequest(position);
                    return;
                }
                if (response.body() == null) {
                    denyGroupRequest(position);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        DimDialog.hideDialog();
                        Snackbar.make(noRequestsLayout, getString(R.string.requestDenied), Snackbar.LENGTH_SHORT).show();
                        requestsModels.clear();
                        getMyRequests();
                    } else {
                        denyGroupRequest(position);
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
                denyGroupRequest(position);
            }
        });
    }

}
