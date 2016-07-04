package ink.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import ink.models.FeedModel;
import ink.utils.CircleTransform;
import ink.utils.Constants;

/**
 * Created by USER on 2016-06-20.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private List<FeedModel> feedList;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView feedContent, userPostedTitle,
                whenPosted, feedAddress, feedAttachmentName;
        private ImageView feedUserImage;
        private RelativeLayout feedAddressLayout, feedAttachmentLayout;

        public ViewHolder(View view) {
            super(view);
            userPostedTitle = (TextView) view.findViewById(R.id.userPostedTitle);
            whenPosted = (TextView) view.findViewById(R.id.whenPosted);
            feedAddress = (TextView) view.findViewById(R.id.feedAddress);
            feedAttachmentName = (TextView) view.findViewById(R.id.feedAttachmentName);
            feedContent = (TextView) view.findViewById(R.id.feedContent);
            feedUserImage = (ImageView) view.findViewById(R.id.feedUserImage);
            feedAddressLayout = (RelativeLayout) view.findViewById(R.id.feedAddressLayout);
            feedAttachmentLayout = (RelativeLayout) view.findViewById(R.id.feedAttachmentLayout);
        }
    }


    public FeedAdapter(List<FeedModel> feedList, Context context) {
        mContext = context;
        this.feedList = feedList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_single_view, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FeedModel feedModel = feedList.get(position);
        if (feedModel.getUserImage() != null && !feedModel.getUserImage().isEmpty()) {
            Picasso.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + feedModel.getUserImage())
                    .transform(new CircleTransform())
                    .fit().centerCrop().into(holder.feedUserImage);
        } else {
            Picasso.with(mContext).load(R.drawable.no_image)
                    .transform(new CircleTransform())
                    .fit().centerCrop().into(holder.feedUserImage);
        }

        holder.feedContent.setText(feedModel.getContent());
        holder.whenPosted.setText(feedModel.getDatePosted());
        holder.userPostedTitle.setText(feedModel.getFirstName() + " " + feedModel.getLastName());

        if (feedModel.getFileName() != null && !feedModel.getFileName().isEmpty()) {
            holder.feedAttachmentLayout.setVisibility(View.VISIBLE);
            holder.feedAttachmentName.setText(feedModel.getFileName());
        } else {
            holder.feedAttachmentLayout.setVisibility(View.GONE);
        }

        if (feedModel.getAddress() != null && !feedModel.getAddress().isEmpty()) {
            holder.feedAddressLayout.setVisibility(View.VISIBLE);
            holder.feedAddress.setText(feedModel.getAddress());
        } else {
            holder.feedAddressLayout.setVisibility(View.GONE);
        }

        animate(holder);
    }


    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(mContext, R.anim.bounce_interpolator);
        if (viewHolder.itemView.getTag() == null) {
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
            viewHolder.itemView.setTag("Animated");
        }
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }
}
