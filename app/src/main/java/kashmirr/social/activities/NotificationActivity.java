package kashmirr.social.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kashmirr.social.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kashmirr.social.adapters.NotificationAdapter;
import kashmirr.social.interfaces.RecyclerItemClickListener;
import kashmirr.social.interfaces.RequestCallback;
import kashmirr.social.models.UserNotificationModel;
import kashmirr.social.utils.InterpreterHelper;
import kashmirr.social.utils.Retrofit;
import kashmirr.social.utils.SharedHelper;
import okhttp3.ResponseBody;

public class NotificationActivity extends BaseActivity implements RecyclerItemClickListener, SwipeRefreshLayout.OnRefreshListener {
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
        UserNotificationModel notificationModel = (UserNotificationModel) object;
        if (!notificationModel.getMethodToRun().isEmpty()) {
            interpreterHelper.evaluateCode(notificationModel.getMethodToRun());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void silentRemove(final UserNotificationModel notificationModel) {
        String id = notificationModel.getId();
        makeRequest(Retrofit.getInstance().getInkService().removeNotification(id),
                null, new RequestCallback() {
                    @Override
                    public void onRequestSuccess(Object result) {
                        try {
                            String responseBody = ((ResponseBody) result).string();
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
                    public void onRequestFailed(Object[] result) {

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
        makeRequest(Retrofit.getInstance().getInkService().getUserNotifications(sharedHelper.getUserId()),
                notificationSwipe, new RequestCallback() {
                    @Override
                    public void onRequestSuccess(Object result) {
                        List<UserNotificationModel> userNotificationModels = (List<UserNotificationModel>) result;

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
                    public void onRequestFailed(Object[] result) {

                    }
                });
    }

    private void silentNotificationCheck() {
        makeRequest(Retrofit.getInstance().getInkService().checkNotificationAsRead(sharedHelper.getUserId()),
                null, null);
    }

    @Override
    public void onRefresh() {
        getNotifications();
    }
}
