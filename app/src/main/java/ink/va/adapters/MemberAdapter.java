package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import java.util.List;

import ink.va.interfaces.RecyclerItemClickListener;
import ink.va.models.MemberModel;
import ink.va.utils.Animations;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;

/**
 * Created by USER on 2016-07-12.
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private List<MemberModel> memberModels;
    private Context mContext;
    private RecyclerItemClickListener onClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView friendImage;
        private ImageView friendMoreIcon;
        private CardView friednsCardView;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.friendName);
            friednsCardView = (CardView) view.findViewById(R.id.friendsCardView);
            friendImage = (ImageView) view.findViewById(R.id.friendImage);
            friendMoreIcon = (ImageView) view.findViewById(R.id.friendMoreIcon);
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.friendMoreIcon.setVisibility(View.GONE);
        MemberModel memberModel = memberModels.get(position);
        holder.name.setText(memberModel.getMemberName());
        if (memberModel.getMemberImage() != null && !memberModel.getMemberImage().isEmpty()) {
            String url = Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + memberModel.getMemberImage();
            Ion.with(mContext).load(url)
                    .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(holder.friendImage);
        } else {
            Ion.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap().placeholder(R.drawable.no_background_image)
                    .transform(new CircleTransform()).intoImageView(holder.friendImage);
        }
        holder.friednsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onItemClicked(position, null);
                }
            }
        });
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Animations.animateCircular(holder.itemView);
    }

    public void setOnClickListener(RecyclerItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public int getItemCount() {
        return memberModels.size();
    }
}
