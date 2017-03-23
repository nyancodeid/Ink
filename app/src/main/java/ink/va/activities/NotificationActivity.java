package ink.va.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.adapters.NotificationAdapter;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.UserNotificationModel;
import ink.va.utils.InterpreterHelper;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity implements RecyclerItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private NotificationAdapter notificationAdapter;
    private InterpreterHelper interpreterHelper;

    @BindView(R.id.noNotificationView)
    View noNotificationView;
    private SharedHelper sharedHelper;
    @BindView(R.id.notificationSwipe)
    SwipeRefreshLayout notificationSwipe;
    @BindView(R.id.notificationRecycler)
    RecyclerView notificationRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_notification);
        ButterKnife.bind(this);
        sharedHelper = new SharedHelper(this);
        notificationSwipe.setOnRefreshListener(this);
        interpreterHelper = new InterpreterHelper(this);
        notificationAdapter = new NotificationAdapter();
        notificationAdapter.setOnItemClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        notificationRecycler.setLayoutManager(linearLayoutManager);
        notificationRecycler.setAdapter(notificationAdapter);
        getNotifications();
        getSupportActionBar().setTitle(getString(R.string.notificationTitle));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onItemClicked(int position, View view) {

    }

    @Override
    public void onItemLongClick(Object object) {
        UserNotificationModel notificationModel = (UserNotificationModel) object;
        if (!notificationModel.getMethodToRun().isEmpty()) {
            interpreterHelper.evaluateCode(notificationModel.getMethodToRun());
        }
    }

    @Override
    public void onAdditionalItemClick(int position, View view) {

    }

    @Override
    public void onAdditionalItemClicked(Object object) {
        UserNotificationModel notificationModel = (UserNotificationModel) object;
        notificationAdapter.removeItem(notificationModel);
        silentRemove(notificationModel);
        if (notificationAdapter.getUserNotificationModels().isEmpty()) {
            showNoNotifications();
        }
    }


    @Override
    public void onItemClicked(Object object) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void silentRemove(final UserNotificationModel notificationModel) {
        String id = notificationModel.getId();
        Retrofit.getInstance().getInkService().removeNotification(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    silentRemove(notificationModel);
                    return;
                }
                if (response.body() == null) {
                    silentRemove(notificationModel);
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (!success) {
                        Toast.makeText(NotificationActivity.this, getString(R.string.couldNotRemoveText), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, getString(R.string.couldNotRemoveText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoNotifications() {
        if (noNotificationView.getVisibility() == View.GONE) {
            noNotificationView.setVisibility(View.VISIBLE);
        }
    }

    private void hideNoNotifications() {
        if (noNotificationView.getVisibility() == View.VISIBLE) {
            noNotificationView.setVisibility(View.GONE);
        }
    }

    private void getNotifications() {
        notificationSwipe.post(new Runnable() {
            @Override
            public void run() {
                notificationSwipe.setRefreshing(true);
            }
        });
        Retrofit.getInstance().getInkService().getUserNotifications(sharedHelper.getUserId()).enqueue(new Callback<List<UserNotificationModel>>() {
            @Override
            public void onResponse(Call<List<UserNotificationModel>> call, Response<List<UserNotificationModel>> response) {
                List<UserNotificationModel> userNotificationModels = response.body();
                notificationSwipe.post(new Runnable() {
                    @Override
                    public void run() {
                        notificationSwipe.setRefreshing(false);
                    }
                });

                if (userNotificationModels.isEmpty()) {
                    showNoNotifications();
                    notificationAdapter.clearItems();
                } else {
                    notificationAdapter.setUserNotificationModels(userNotificationModels);
                    hideNoNotifications();
                    silentNotificationCheck();
                }
            }

            @Override
            public void onFailure(Call<List<UserNotificationModel>> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void silentNotificationCheck() {
        Retrofit.getInstance().getInkService().checkNotificationAsRead(sharedHelper.getUserId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public void onRefresh() {
        getNotifications();
    }
}
