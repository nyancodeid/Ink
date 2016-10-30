package ink.va.adapters;

import android.content.Context;
import android.net.Uri;
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

import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.FriendsModel;
import ink.va.utils.Animations;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;

/**
 * Created by USER on 2016-06-22.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<FriendsModel> friendsModelList;
    private Context mContext;
    private RecyclerItemClickListener recyclerItemClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView friendImage;
        private ImageView friendMoreIcon;
        private View cardView;
        private RelativeLayout friendsRootView;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.friendName);
            friendImage = (ImageView) view.findViewById(R.id.friendImage);
            friendMoreIcon = (ImageView) view.findViewById(R.id.friendMoreIcon);
            friendsRootView = (RelativeLayout) view.findViewById(R.id.friendsRootView);
            cardView = view.findViewById(R.id.friendsCardView);
        }
    }


    public FriendsAdapter(List<FriendsModel> friendsModelList, Context context) {
        mContext = context;
        this.friendsModelList = friendsModelList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_single_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        FriendsModel friendsModel = friendsModelList.get(position);
        holder.name.setText(friendsModel.getFullName());

        if (!friendsModel.getImageLink().isEmpty()) {

            final String url;
            if (friendsModel.isSocialAccount()) {
                url = friendsModel.getImageLink();
            } else {
                String encodedImage = Uri.encode(friendsModel.getImageLink());
                url = Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage;
            }

            Ion.with(mContext).load(url)
                    .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(holder.friendImage);
        } else {
            Ion.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image")
                    .withBitmap().transform(new CircleTransform()).intoImageView(holder.friendImage);
        }

        if (friendsModel.isFriend()) {
            holder.friendMoreIcon.setVisibility(View.VISIBLE);
        } else {
            holder.friendMoreIcon.setVisibility(View.GONE);
        }
        holder.friendMoreIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onAdditionItemClick(position, holder.friendMoreIcon);
                }
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerItemClickListener != null) {
                    recyclerItemClickListener.onItemClicked(position, holder.friendsRootView);
                }
            }
        });
    }


    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Animations.animateCircular(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return friendsModelList.size();
    }

    public void setOnItemClickListener(RecyclerItemClickListener onItemClickListener) {
        this.recyclerItemClickListener = onItemClickListener;
    }
}
