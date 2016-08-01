package ink.utils;

import android.media.AudioManager;

import ink.models.Track;

/**
 * Created by PC-Comp on 8/1/2016.
 */
public class MediaPlayer {
    private static MediaPlayer ourInstance = new MediaPlayer();

    public static MediaPlayer get() {
        return ourInstance;
    }

    private android.media.MediaPlayer mMediaPlayer;
    private String mLastTrack;

    private MediaPlayer() {
        mLastTrack = "noTrackYet";
        mMediaPlayer = new android.media.MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(android.media.MediaPlayer mp) {
                // TODO: 8/1/2016 handle preperation
            }
        });

        mMediaPlayer.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(android.media.MediaPlayer mp) {
                // TODO: 8/1/2016 handle completion with icon
            }
        });
    }

    public void playMusic(Track track) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }

        try {
            mLastTrack = track.mStreamURL + "?client_id=" + Constants.CLOUD_CLIENT_ID;
            mMediaPlayer.setDataSource(track.mStreamURL + "?client_id=" + Constants.CLOUD_CLIENT_ID);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(android.media.MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }

    public boolean isSoundPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public String getLastTrack() {
        return mLastTrack;
    }
}
