package ink.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.ActionBar;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ink.R;
import com.koushikdutta.ion.Ion;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ink.adapters.MusicAdapter;
import ink.callbacks.GeneralCallback;
import ink.decorators.DividerItemDecoration;
import ink.interfaces.MusicClickListener;
import ink.models.Track;
import ink.utils.CircleTransform;
import ink.utils.MediaPlayerManager;
import ink.utils.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Music extends BaseActivity implements MusicClickListener {

    @Bind(R.id.musicRecycler)
    RecyclerView musicRecycler;
    @Bind(R.id.musicLoading)
    AVLoadingIndicatorView musicLoading;
    @Bind(R.id.musicInfoSheet)
    View musicInfoSheet;
    @Bind(R.id.openCloseMusicSheet)
    ImageView openCloseMusicSheet;
    @Bind(R.id.statusText)
    TextView statusText;
    @Bind(R.id.currentlyPlayingName)
    TextView currentlyPlayingName;
    @Bind(R.id.playPauseButton)
    ImageView playPauseButton;
    @Bind(R.id.currentlyPlayingImage)
    ImageView currentlyPlayingImage;
    private boolean isMusicChosen;

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

        checkForMusicPlaying();

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isMusicChosen) {
                    return;
                }
                if (MediaPlayerManager.get().isSoundPlaying()) {
                    playPauseButton.setImageResource(R.drawable.play_icon);
                    MediaPlayerManager.get().pauseMusic();
                } else {
                    playPauseButton.setImageResource(R.drawable.pause);
                    MediaPlayerManager.get().playMusic(null, null);
                }
            }
        });

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

    private void checkForMusicPlaying() {
        if (MediaPlayerManager.get().isSoundPlaying()) {
            isMusicChosen = true;
            initBottomSheet(null);
        }
    }


    @Override
    public void onMusicItemClick(int position) {
        Track track = tracks.get(position);
        initBottomSheet(track);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        isMusicChosen = true;
    }

    private void initBottomSheet(@Nullable Track track) {
        statusText.setText(getString(R.string.buffering));

        playPauseButton.setImageResource(R.drawable.pause);

        String title;
        String image;

        if (track == null) {
            statusText.setText(getString(R.string.currentlyPlaying));
            title = MediaPlayerManager.get().getTitle();
            image = MediaPlayerManager.get().getLastImage();
        } else {
            title = track.mTitle;
            image = track.mArtworkURL;
        }


        if (image != null && !image.equals("null")) {
            Ion.with(getApplicationContext()).load(image).withBitmap().transform(new CircleTransform()).intoImageView(currentlyPlayingImage);
        } else {
            currentlyPlayingImage.setBackground(null);
            currentlyPlayingImage.setImageResource(R.drawable.gradient_no_image);
        }
        currentlyPlayingName.setText(title);

        if (track == null) {
            return;
        }
        MediaPlayerManager.get().playMusic(track, new GeneralCallback() {
            @Override
            public void onSuccess(Object o) {
                statusText.setText(getString(R.string.currentlyPlaying));
            }

            @Override
            public void onFailure(Object o) {

            }
        });
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
                    dialog.dismiss();
                    musicLoading.setVisibility(View.VISIBLE);
                    clearTrackArray();
                    Call<ResponseBody> searchCall = Retrofit.getInstance().getMusicCloudInterface().searchSong(searchText);
                    searchCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                String responseBody = response.body().string();
                                Track[] tracks = gson.fromJson(responseBody, Track[].class);
                                loadTracks(tracks);
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
