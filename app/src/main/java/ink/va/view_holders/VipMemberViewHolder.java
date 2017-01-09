package ink.va.view_holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.ItemClickListener;
import ink.va.models.UserModel;
import ink.va.utils.Constants;

/**
 * Created by PC-Comp on 1/9/2017.
 */

public class VipMemberViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.vipMemberImage)
    ImageView vipMemberImage;
    @Bind(R.id.vipMemberName)
    TextView vipMemberName;
    @Bind(R.id.bottomSpacing)
    View bottomSpacing;

    private ItemClickListener itemClickListener;
    private UserModel userModel;
    private Context context;

    public VipMemberViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(UserModel userModel, int position, ItemClickListener itemClickListener,
                         Context context, int maxSize) {
        this.context = context;
        this.userModel = userModel;
        this.itemClickListener = itemClickListener;
        vipMemberName.setText(userModel.getFirstName() + " " + userModel.getLastName());
        Ion.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + userModel.getImageUrl()).withBitmap().placeholder(R.drawable.vip_image_placeholder).
                intoImageView(vipMemberImage);
        bottomSpacing.setVisibility(position == maxSize ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.vipMemberRoot)
    public void rootClicked() {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(userModel);
        }
    }
}
