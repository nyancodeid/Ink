package ink.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.R;
import com.koushikdutta.ion.Ion;

import java.util.List;

import ink.models.MemberModel;
import ink.utils.Animations;
import ink.utils.CircleTransform;
import ink.utils.Constants;

/**
 * Created by USER on 2016-07-12.
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private List<MemberModel> memberModels;
    private Context mContext;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView friendImage;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.friendName);
            friendImage = (ImageView) view.findViewById(R.id.friendImage);
        }
    }


    public MemberAdapter(List<MemberModel> memberModels, Context context) {
        mContext = context;
        this.memberModels = memberModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_single_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MemberModel memberModel = memberModels.get(position);
        holder.name.setText(memberModel.getMemberName());
        if (memberModel.getMemberImage() != null && !memberModel.getMemberImage().isEmpty()) {
            String url = Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + memberModel.getMemberImage();
            Ion.with(mContext).load(url)
                    .withBitmap().transform(new CircleTransform()).intoImageView(holder.friendImage);
        } else {
            Ion.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap()
                    .transform(new CircleTransform()).intoImageView(holder.friendImage);
        }
    }


    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(mContext, R.anim.bounce_interpolator);
        if (viewHolder.itemView.getTag() == null) {
            viewHolder.itemView.setAnimation(animAnticipateOvershoot);
            viewHolder.itemView.setTag("Animated");
        }
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Animations.animateCircular(holder.itemView);
    }


    @Override
    public int getItemCount() {
        return memberModels.size();
    }
}
