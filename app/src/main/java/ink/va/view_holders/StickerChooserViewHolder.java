package ink.va.view_holders;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.wang.avi.AVLoadingIndicatorView;

import ink.StartupApplication;
import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.StickerModel;
import ink.va.utils.Constants;

/**
 * Created by PC-Comp on 10/28/2016.
 */

public class StickerChooserViewHolder extends RecyclerView.ViewHolder {

    private ImageView gifSingleView;
    private VideoView videoView;
    private AVLoadingIndicatorView gifLoadingSingleItem;
    private RelativeLayout stickerWrapper;
    private RelativeLayout videoWrapper;
    private Button choose;
    private ProgressBar singleVideoViewLoading;

    public StickerChooserViewHolder(View itemView) {
        super(itemView);
        gifSingleView = (ImageView) itemView.findViewById(R.id.stickerNotAnimatedView);
        videoView = (VideoView) itemView.findViewById(R.id.video_view_sticker_choser);
        choose = (Button) itemView.findViewById(R.id.choose);
        stickerWrapper = (RelativeLayout) itemView.findViewById(R.id.sticker_not_animated_parent);
        singleVideoViewLoading = (ProgressBar) itemView.findViewById(R.id.singleVideoViewLoading);
        videoWrapper = (RelativeLayout) itemView.findViewById(R.id.videoWrapper);

        gifLoadingSingleItem = (AVLoadingIndicatorView) itemView.findViewById(R.id.gifLoadingSingleItem);
    }


    public void init(Context context, final StickerModel stickerModel,
                     @Nullable final RecyclerItemClickListener recyclerItemClickListener) {
        if (stickerModel.isAnimated()) {
            stickerWrapper.setVisibility(View.GONE);
            videoWrapper.setVisibility(View.VISIBLE);

            HttpProxyCacheServer proxy = StartupApplication.getProxy(context);
            String proxyUrl = proxy.getProxyUrl(Constants.MAIN_URL + stickerModel.getStickerUrl());
            videoView.setVideoPath(proxyUrl);

            singleVideoViewLoading.setVisibility(View.VISIBLE);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.seekTo(1000);
                    videoView.seekTo(1000);
                    singleVideoViewLoading.setVisibility(View.GONE);
                }
            });

            videoWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!videoView.isPlaying()) {
                        videoView.setBackground(null);
                        videoView.start();
                    }
                }
            });
        } else {
            videoWrapper.setVisibility(View.GONE);
            stickerWrapper.setVisibility(View.VISIBLE);
            Ion.with(context).load(Constants.MAIN_URL + stickerModel.getStickerUrl()).intoImageView(gifSingleView).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    gifLoadingSingleItem.setVisibility(View.GONE);
                }
            });

        }

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onItemClicked(stickerModel);
                }
            }
        });
    }
}
