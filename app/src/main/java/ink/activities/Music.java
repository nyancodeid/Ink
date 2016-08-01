package ink.activities;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import ink.utils.Constants;
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

    private List<Track> tracks;
    private MusicAdapter musicAdapter;
    private MediaPlayer mMediaPlayer;
    private Gson gson;

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
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
            initMediaPlayer();
        } else {
            initMediaPlayer();
        }

        try {
            mMediaPlayer.setDataSource(track.mStreamURL + "?client_id=" + Constants.CLOUD_CLIENT_ID);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO: 8/1/2016 handle preperation
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO: 8/1/2016 hande completion with icon
            }
        });

    }

    private void getAllTracks() {
        if (tracks != null) {
            tracks.clear();
        }
        musicAdapter.notifyDataSetChanged();

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

        }
        return super.onOptionsItemSelected(item);
    }

}
