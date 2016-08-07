package ink.activities;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.adapters.GroupsAdapter;
import ink.callbacks.GeneralCallback;
import ink.models.GroupsModel;
import ink.utils.CircleTransform;
import ink.utils.RecyclerTouchListener;
import ink.utils.Retrofit;
import ink.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yuku.ambilwarna.AmbilWarnaDialog;

public class Groups extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SharedHelper mSharedHelper;
    @Bind(R.id.groupsRecycler)
    RecyclerView mRecyclerView;
    @Bind(R.id.createGroup)
    FloatingActionButton mCreateGroup;
    @Bind(R.id.groupSwipe)
    SwipeRefreshLayout groupSwipe;
    @Bind(R.id.noGroupsLayout)
    RelativeLayout noGroupsLayout;
    @Bind(R.id.nothingFoundLayout)
    RelativeLayout nothingFoundLayout;
    @Bind(R.id.searchProgress)
    AVLoadingIndicatorView searchProgress;
    private AlertDialog addGroupDialog = null;
    private String chosenColor = "";
    private static final int PICK_IMAGE_RESULT_CODE = 1547;
    private boolean isImageChosen;
    private String mImageLinkToSend;
    private ImageView groupImage;
    private Thread mWorkerThread;
    private Dialog addGroupProgress;
    private List<GroupsModel> groupsModels;
    private GroupsModel groupsModel;
    private GroupsAdapter groupsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);
        groupSwipe.setOnRefreshListener(this);
        addGroupProgress = new Dialog(this, R.style.Theme_Transparent);
        addGroupProgress.setContentView(R.layout.dim_group_layout);
        groupSwipe.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        groupSwipe.post(new Runnable() {
            @Override
            public void run() {
                groupSwipe.setRefreshing(true);
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    mCreateGroup.hide(true);
                else if (dy < 0)
                    mCreateGroup.show(true);
            }
        });
        groupsModels = new ArrayList<>();
        groupsAdapter = new GroupsAdapter(groupsModels, this);
        mSharedHelper = new SharedHelper(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.groupsText));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setItemAnimator(itemAnimator);


        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getApplicationContext(), SingleGroupView.class);
                intent.putExtra("groupName", groupsModels.get(position).getGroupName());
                intent.putExtra("groupId", groupsModels.get(position).getGroupId());
                intent.putExtra("groupColor", groupsModels.get(position).getGroupColor());
                intent.putExtra("groupImage", groupsModels.get(position).getGroupImage());
                intent.putExtra("groupDescription", groupsModels.get(position).getGroupDescription());
                intent.putExtra("groupOwnerId", groupsModels.get(position).getGroupOwnerId());
                intent.putExtra("groupOwnerName", groupsModels.get(position).getGroupOwnerName());
                intent.putExtra("count", groupsModels.get(position).getParticipantsCount());
                intent.putExtra("ownerImage", groupsModels.get(position).getOwnerImage());
                intent.putExtra("isSocialAccount", groupsModels.get(position).isSocialAccount());
                intent.putExtra("isMember", groupsModels.get(position).isMember());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        mRecyclerView.setAdapter(groupsAdapter);


        getGroups();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when collapsed
                if (searchProgress.getVisibility() == View.VISIBLE) {
                    searchProgress.setVisibility(View.GONE);
                }
                if (nothingFoundLayout.getVisibility() == View.VISIBLE) {
                    nothingFoundLayout.setVisibility(View.GONE);
                }
                groupSwipe.post(new Runnable() {
                    @Override
                    public void run() {
                        groupSwipe.setRefreshing(true);
                    }
                });
                mRecyclerView.setVisibility(View.VISIBLE);
                getGroups();
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        });

        if (item.getItemId() == R.id.myGroups) {

        } else if (item.getItemId() == R.id.search) {

        } else if (item.getItemId() == R.id.requests) {
            startActivity(new Intent(getApplicationContext(), RequestsView.class));
        } else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_menu, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                doSearch(newText);
                return true;
            }
        });

        return true;
    }

    private void doSearch(final String newText) {
        if (!newText.trim().toLowerCase().isEmpty()) {
            if (noGroupsLayout.getVisibility() == View.VISIBLE) {
                noGroupsLayout.setVisibility(View.GONE);
            }
            if (nothingFoundLayout.getVisibility() == View.VISIBLE) {
                nothingFoundLayout.setVisibility(View.GONE);
            }
            if (mRecyclerView.getVisibility() == View.VISIBLE) {
                mRecyclerView.setVisibility(View.GONE);
            }
            searchProgress.setVisibility(View.VISIBLE);

            String searchableText = newText.trim().toLowerCase();
            Call<ResponseBody> searchCall = Retrofit.getInstance().getInkService().searchGroups(mSharedHelper.getUserId(),
                    searchableText);
            searchCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response == null) {
                        doSearch(newText);
                        return;
                    }
                    if (response.body() == null) {
                        doSearch(newText);
                        return;
                    }
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        boolean hasResults = jsonObject.optBoolean("hasAnyResult");
                        if (hasResults) {
                            JSONArray resultsArray = jsonObject.optJSONArray("result");
                            if (groupsModels != null) {
                                groupsModels.clear();
                                groupsAdapter.notifyDataSetChanged();
                            }
                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject eachObject = resultsArray.optJSONObject(i);
                                String groupId = eachObject.optString("group_id");
                                String groupImage = eachObject.optString("group_image");
                                String groupName = eachObject.optString("group_name");
                                String groupOwnerName = eachObject.optString("group_owner_name");
                                String groupDescription = eachObject.optString("group_description");
                                String groupOwnerId = eachObject.optString("group_owner_id");
                                String groupColor = eachObject.optString("group_color");
                                String participantsCount = eachObject.optString("participants");
                                String ownerImage = eachObject.optString("owner_image");
                                boolean isMember = eachObject.optBoolean("isMember");
                                boolean isSocialAccount = eachObject.optBoolean("isSocialAccount");

                                groupsModel = new GroupsModel(isSocialAccount, groupId, groupImage, groupName, groupOwnerName, groupDescription,
                                        groupOwnerId, groupColor, participantsCount, ownerImage, isMember);
                                groupsModels.add(groupsModel);
                                groupsAdapter.notifyDataSetChanged();
                            }
                            showResult();
                        } else {
                            showNoResult();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    doSearch(newText);
                }
            });

        }
    }

    private void showResult() {
        if (nothingFoundLayout.getVisibility() == View.VISIBLE) {
            nothingFoundLayout.setVisibility(View.GONE);
        }
        mRecyclerView.setVisibility(View.VISIBLE);
        if (searchProgress.getVisibility() == View.VISIBLE) {
            searchProgress.setVisibility(View.GONE);
        }
        if (noGroupsLayout.getVisibility() == View.VISIBLE) {
            noGroupsLayout.setVisibility(View.GONE);
        }
    }

    private void showNoResult() {
        if (noGroupsLayout.getVisibility() == View.VISIBLE) {
            noGroupsLayout.setVisibility(View.GONE);
        }
        mRecyclerView.setVisibility(View.GONE);
        if (searchProgress.getVisibility() == View.VISIBLE) {
            searchProgress.setVisibility(View.GONE);
        }
        nothingFoundLayout.setVisibility(View.VISIBLE);
    }

    private void getGroups() {
        Call<ResponseBody> groupsCall = Retrofit.getInstance().getInkService().getGroups(mSharedHelper.getUserId());
        groupsCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    getGroups();
                    return;
                }
                if (response.body() == null) {
                    getGroups();
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        if (groupsModels != null) {
                            groupsModels.clear();
                            groupsAdapter.notifyDataSetChanged();
                        }
                        boolean hasAnyGroups = jsonObject.optBoolean("hasGroups");
                        if (hasAnyGroups) {
                            hideNoGroupLayout();
                            JSONArray jsonArray = jsonObject.optJSONArray("groups");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject eachObject = jsonArray.optJSONObject(i);
                                String groupId = eachObject.optString("group_id");
                                String groupImage = eachObject.optString("group_image");
                                String groupName = eachObject.optString("group_name");
                                String groupOwnerName = eachObject.optString("group_owner_name");
                                String groupDescription = eachObject.optString("group_description");
                                String groupOwnerId = eachObject.optString("group_owner_id");
                                String groupColor = eachObject.optString("group_color");
                                String participantsCount = eachObject.optString("participants");
                                String ownerImage = eachObject.optString("owner_image");
                                boolean isMember = eachObject.optBoolean("isMember");
                                boolean isSocialAccount = eachObject.optBoolean("isSocialAccount");

                                groupsModel = new GroupsModel(isSocialAccount, groupId, groupImage, groupName, groupOwnerName, groupDescription,
                                        groupOwnerId, groupColor, participantsCount, ownerImage, isMember);
                                groupsModels.add(groupsModel);
                                groupsAdapter.notifyDataSetChanged();
                                groupSwipe.setRefreshing(false);
                            }
                        } else {
                            groupSwipe.setRefreshing(false);
                            showNoGroupLayout();
                        }
                    } else {
                        getGroups();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                getGroups();
            }
        });
    }

    private void hideNoGroupLayout() {
        noGroupsLayout.setVisibility(View.GONE);
    }

    private void showNoGroupLayout() {
        noGroupsLayout.setVisibility(View.VISIBLE);
    }


    private void showAddGroupDialog() {
        System.gc();

        chosenColor = "";
        mImageLinkToSend = "";

        final AlertDialog.Builder builder = new AlertDialog.Builder(Groups.this);
        builder.setTitle(getString(R.string.createGroup));
        View groupView = getLayoutInflater().inflate(R.layout.add_group_view, null);
        Button pickBackgroundColor = (Button) groupView.findViewById(R.id.pickBackgroundColor);
        Button publishGroup = (Button) groupView.findViewById(R.id.publishGroup);
        final EditText groupName = (EditText) groupView.findViewById(R.id.groupName);
        final EditText groupDescription = (EditText) groupView.findViewById(R.id.groupDescription);
        NestedScrollView nestedScrollView = (NestedScrollView) groupView.findViewById(R.id.nestedScrollView);
        nestedScrollView.setVerticalScrollBarEnabled(true);
        Button pickImage = (Button) groupView.findViewById(R.id.pickImage);
        groupImage = (ImageView) groupView.findViewById(R.id.groupImage);
        publishGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupName.getText().toString().trim().isEmpty()) {
                    groupName.setError(getString(R.string.groupNameError));
                    return;
                }
                if (groupDescription.getText().toString().trim().isEmpty()) {
                    groupDescription.setError(getString(R.string.groupDescriptionError));
                    return;
                }
                publishCreatedGroup(groupName.getText().toString().trim()
                        , groupDescription.getText().toString().trim());
            }
        });
        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        final TextView pickerText = (TextView) groupView.findViewById(R.id.pickerText);
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
        pickBackgroundColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorPicker(new GeneralCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        chosenColor = "#" + s;
                        pickerText.setText(getString(R.string.selectedBackgroundColor));
                        pickerText.setTextColor(Color.parseColor("#" + s));
                    }

                    @Override
                    public void onFailure(String s) {
                        chosenColor = null;
                    }
                });
            }
        });
        builder.setView(groupView);
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        addGroupDialog = builder.show();
        addGroupDialog.show();

    }

    private void dismissLoading() {
        if (addGroupProgress.isShowing()) {
            addGroupProgress.dismiss();
        }
        Snackbar.make(mCreateGroup, getString(R.string.groupCreated), Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        }).show();
        getGroups();
    }

    private void publishCreatedGroup(final String groupName, final String groupDescription) {
        String base64 = "";
        addGroupProgress.show();

        if (mImageLinkToSend != null && !mImageLinkToSend.isEmpty() && isImageChosen) {
            getBase64String(mImageLinkToSend, new GeneralCallback<String>() {
                @Override
                public void onSuccess(String o) {
                    callToServer(o, groupName, groupDescription);
                }

                @Override
                public void onFailure(String o) {
                    publishCreatedGroup(groupName, groupDescription);
                }
            });
        } else {
            callToServer(base64, groupName, groupDescription);
        }

    }

    private void callToServer(final String base64, final String groupName, final String groupDescription) {
        Call<ResponseBody> createGroupCall = Retrofit.getInstance().getInkService().createGroup(mSharedHelper.getUserId(),
                base64, groupName, groupDescription, chosenColor,
                mSharedHelper.getFirstName() + " " + mSharedHelper.getLastName(), mSharedHelper.getImageLink());
        createGroupCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    callToServer(base64, groupName, groupDescription);
                }
                if (response.body() == null) {
                    callToServer(base64, groupName, groupDescription);
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        dismissLoading();
                        if (addGroupDialog != null && addGroupDialog.isShowing()) {
                            addGroupDialog.dismiss();
                        }
                    } else {
                        callToServer(base64, groupName, groupDescription);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callToServer(base64, groupName, groupDescription);
            }
        });
    }

    @OnClick(R.id.createGroup)
    public void createGroup() {
        showAddGroupDialog();
    }

    private void showColorPicker(final GeneralCallback<String> generalCallback) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, Color.parseColor("#3F51B5"), new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                // color is the color selected by the user.
                String hexWithoutAlpha = Integer.toHexString(color).toUpperCase().substring(2);
                generalCallback.onSuccess(hexWithoutAlpha);
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                generalCallback.onFailure(null);
            }
        });
        dialog.show();

    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.selectImage)), PICK_IMAGE_RESULT_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_RESULT_CODE) {
                Uri selectedImageUri = data.getData();
                String selectedImagePath;
                try {
                    selectedImagePath = getRealPathFromURI(selectedImageUri);
                } catch (Exception e) {
                    selectedImagePath = null;
                    AlertDialog.Builder builder = new AlertDialog.Builder(Groups.this);
                    builder.setTitle(getString(R.string.notSupported));
                    builder.setMessage(getString(R.string.notSupportedText));
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                }
                if (selectedImagePath != null) {
                    mImageLinkToSend = selectedImagePath;
                    isImageChosen = true;
                    Picasso.with(getApplicationContext()).load(new File(selectedImagePath)).error(R.drawable.image_laoding_error)
                            .placeholder(R.drawable.no_image_yet_state).transform(new CircleTransform()).fit().centerCrop().into(groupImage);
                } else {
                    isImageChosen = false;
                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void getBase64String(final String path, final GeneralCallback callback) {
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                bitmap = reduceBitmap(bitmap, 500);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
                if (encodedImage == null) {
                    callback.onFailure(encodedImage);
                } else {
                    callback.onSuccess(encodedImage);
                }
                bitmap.recycle();
                bitmap = null;
                mWorkerThread = null;
            }
        });
        mWorkerThread.start();
    }

    private Bitmap reduceBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    public void onRefresh() {
        if (mRecyclerView.getVisibility() == View.GONE) {
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        if (searchProgress.getVisibility() == View.VISIBLE) {
            searchProgress.setVisibility(View.GONE);
        }
        if (nothingFoundLayout.getVisibility() == View.VISIBLE) {
            nothingFoundLayout.setVisibility(View.GONE);
        }
        getGroups();
    }
}
