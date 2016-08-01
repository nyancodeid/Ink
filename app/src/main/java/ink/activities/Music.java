package ink.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ink.R;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.adapters.MusicAdapter;
import ink.decorators.DividerItemDecoration;
import ink.interfaces.MusicClickListener;
import ink.models.Track;
import ink.utils.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Music extends AppCompatActivity implements MusicClickListener {

    @Bind(R.id.musicRecycler)
    RecyclerView musicRecycler;
    @Bind(R.id.musicLoading)
    AVLoadingIndicatorView musicLoading;
    @Bind(R.id.musicInfoSheet)
    View musicInfoSheet;
    @Bind(R.id.openCloseMusicSheet)
    ImageView openCloseMusicSheet;

    private List<Track> tracks;
    private MusicAdapter musicAdapter;
    private Gson gson;
    private BottomSheetBehavior mBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.music));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        gson = new Gson();
        tracks = new ArrayList<>();
        musicAdapter = new MusicAdapter(tracks, this);
        musicAdapter.setonMusicClickListener(this);

        mBottomSheetBehavior = BottomSheetBehavior.from(musicInfoSheet);


        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    openCloseMusicSheet.setVisibility(View.VISIBLE);
                } else {
                    openCloseMusicSheet.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        openCloseMusicSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                openCloseMusicSheet.setVisibility(View.GONE);
            }
        });

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        musicRecycler.setLayoutManager(new LinearLayoutManager(this));
        musicRecycler.setItemAnimator(itemAnimator);
        musicRecycler.addItemDecoration(
                new DividerItemDecoration(this));
        musicRecycler.setAdapter(musicAdapter);
        getAllTracks();
    }


    @Override
    public void onMusicItemClick(int position) {
        Track track = tracks.get(position);
        ink.utils.MediaPlayer.get().playMusic(track);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void getAllTracks() {
        clearTrackArray();

        Call<ResponseBody> listCallback = Retrofit.getInstance().getMusicCloudInterface().getAllTracks();
        listCallback.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    Track[] tracks = gson.fromJson(response.body().string(), Track[].class);
                    loadTracks(tracks);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void clearTrackArray() {
        if (tracks != null) {
            tracks.clear();
        }
        musicAdapter.notifyDataSetChanged();
    }


    private void loadTracks(Track[] trackList) {
        for (int i = 0; i < trackList.length; i++) {
            Track eachTrack = trackList[i];
            tracks.add(eachTrack);
            musicAdapter.notifyDataSetChanged();
        }

        musicLoading.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.music_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            openSearchDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSearchDialog() {
        System.gc();
        final Dialog dialog = new Dialog(Music.this);
        dialog.setContentView(R.layout.search_dialog_view);
        final AutoCompleteTextView searchField = (AutoCompleteTextView) dialog.findViewById(R.id.searchField);
        Button searchButton = (Button) dialog.findViewById(R.id.searchButton);
        RelativeLayout searchRootLayout = (RelativeLayout) dialog.findViewById(R.id.searchRootLayout);

        ImageView closeSearch = (ImageView) dialog.findViewById(R.id.closeSearch);
        closeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = searchField.getText().toString().trim();
                if (searchText.isEmpty()) {
                    Toast.makeText(Music.this, getString(R.string.pleaseInputSearch), Toast.LENGTH_SHORT).show();
                } else {
                    Call<ResponseBody> searchCall = Retrofit.getInstance().getMusicCloudInterface().searchSong(searchText);
                    searchCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                clearTrackArray();
                                String responseBody = response.body().string();
                                Track[] tracks = gson.fromJson(responseBody, Track[].class);
                                loadTracks(tracks);
                                dialog.dismiss();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(Music.this, getString(R.string.failedSearch), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        dialog.show();
    }
}
