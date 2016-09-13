package ink.va.utils;

import android.media.AudioManager;
import android.support.annotation.Nullable;

import ink.va.callbacks.GeneralCallback;
import ink.va.models.Track;

/**
 * Created by PC-Comp on 8/1/2016.
 */
public class MediaPlayerManager {
    private static MediaPlayerManager ourInstance = new MediaPlayerManager();

    public static MediaPlayerManager get() {
        return ourInstance;
    }

    private android.media.MediaPlayer mMediaPlayer;
    private String mLastTrack;
    private String mLastImage;
    private String mLastTitle;

    private MediaPlayerManager() {
        mLastTrack = "noTrackYet";
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        mMediaPlayer = new android.media.MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(android.media.MediaPlayer mp) {

            }
        });

        mMediaPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(android.media.MediaPlayer mp) {

            }
        });
    }

    public void playMusic(@Nullable Track track, final @Nullable GeneralCallback generalCallback) {


        if (track == null) {
            mMediaPlayer.start();
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }

        mMediaPlayer = null;
        initMediaPlayer();
        try {
            mLastTrack = track.mStreamURL + "?client_id=" + Constants.CLOUD_CLIENT_ID;
            mLastImage = track.mArtworkURL;
            mLastTitle = track.mTitle;

            mMediaPlayer.setDataSource(track.mStreamURL + "?client_id=" + Constants.CLOUD_CLIENT_ID);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(android.media.MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    if (generalCallback != null) {
                        generalCallback.onSuccess(null);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (generalCallback != null) {
                generalCallback.onFailure(e);
            }
        }
    }

    public void stopMusic() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }

    public void pauseMusic() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }

    public boolean isSoundPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public String getLastTrack() {
        return mLastTrack;
    }

    public String getLastImage() {
        return mLastImage;
    }

    public String getTitle() {
        return mLastTitle;
    }
}
