package ink.va.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.ink.va.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.StickerModel;
import ink.va.utils.Constants;

/**
 * Created by PC-Comp on 8/9/2016.
 */
public class GifAdapter extends RecyclerView.Adapter<GifAdapter.ViewHolder> {
    private List<StickerModel> gifAdapterList;
    private Context context;
    private RecyclerItemClickListener recyclerItemClickListener;


    public GifAdapter(List<StickerModel> gifAdapterList, Context context) {
        this.gifAdapterList = gifAdapterList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gif_single_item_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        StickerModel stickerModel = gifAdapterList.get(position);
        if (stickerModel.isAnimated()) {
            holder.stickerWrapper.setVisibility(View.GONE);
            holder.videoWrapper.setVisibility(View.VISIBLE);
            holder.videoView.setZOrderOnTop(true);
            Uri video = Uri.parse(Constants.MAIN_URL + stickerModel.getStickerUrl());
            holder.videoView.setVideoURI(video);
            holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                            mediaPlayer.seekTo(1000);
                        }
                    });
                }
            });

            holder.videoWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!holder.videoView.isPlaying()) {
                        holder.videoView.start();
                    }
                }
            });
        } else {
            holder.videoWrapper.setVisibility(View.GONE);
            holder.stickerWrapper.setVisibility(View.VISIBLE);
            Ion.with(context).load(Constants.MAIN_URL + stickerModel.getStickerUrl()).intoImageView(holder.gifSingleView).setCallback(new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    holder.gifLoadingSingleItem.setVisibility(View.GONE);
                }
            });

        }

        holder.choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onItemClicked(position, null);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return gifAdapterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView gifSingleView;
        private VideoView videoView;
        private AVLoadingIndicatorView gifLoadingSingleItem;
        private RelativeLayout stickerWrapper;
        private RelativeLayout videoWrapper;
        private Button choose;

        public ViewHolder(View itemView) {
            super(itemView);
            gifSingleView = (ImageView) itemView.findViewById(R.id.stickerNotAnimatedView);
            videoView = (VideoView) itemView.findViewById(R.id.video_view_sticker_choser);
            choose = (Button) itemView.findViewById(R.id.choose);
            stickerWrapper = (RelativeLayout) itemView.findViewById(R.id.sticker_not_animated_parent);
            videoWrapper = (RelativeLayout) itemView.findViewById(R.id.videoWrapper);

            gifLoadingSingleItem = (AVLoadingIndicatorView) itemView.findViewById(R.id.gifLoadingSingleItem);
        }
    }


    public void setOnItemClickListener(RecyclerItemClickListener recyclerItemClickListener) {
        this.recyclerItemClickListener = recyclerItemClickListener;
    }
}
