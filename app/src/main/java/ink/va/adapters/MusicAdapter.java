package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import java.util.List;

import ink.va.interfaces.MusicClickListener;
import ink.va.models.Track;
import ink.va.utils.CircleTransform;

/**
 * Created by PC-Comp on 8/1/2016.
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private static final int NOT_LOADED = 0;
    private static final int LOADED = 1;
    private List<Track> trackList;
    private Context mContext;
    private MusicClickListener mOnClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView musicName;
        public ImageView musicImage;
        private RelativeLayout musicRootItem;

        public ViewHolder(View view) {
            super(view);
            musicName = (TextView) view.findViewById(R.id.musicName);
            musicImage = (ImageView) view.findViewById(R.id.musicImage);
            musicRootItem = (RelativeLayout) view.findViewById(R.id.musicRootItem);
            musicImage.setTag(NOT_LOADED);
        }
    }


    public MusicAdapter(List<Track> trackList, Context context) {
        mContext = context;
        this.trackList = trackList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_music_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Track track = trackList.get(position);

        holder.musicRootItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onMusicItemClick(position);
                }
            }
        });

        holder.musicName.setText(track.mTitle);

        if (track.mArtworkURL != null && !track.equals("null")) {
            holder.musicImage.setBackground(null);
            Ion.with(mContext).load(track.mArtworkURL).withBitmap().transform(new CircleTransform()).intoImageView(holder.musicImage);
        } else {
            holder.musicImage.setBackground(null);
            holder.musicImage.setBackgroundResource(R.drawable.gradient_no_image);
        }


    }


    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public void setonMusicClickListener(MusicClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

}
