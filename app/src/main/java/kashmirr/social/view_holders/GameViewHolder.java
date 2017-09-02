package kashmirr.social.view_holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kashmirr.social.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.sephiroth.android.library.picasso.Picasso;
import kashmirr.social.interfaces.ItemClickListener;
import kashmirr.social.models.GameModel;
import kashmirr.social.utils.PicassoCircleTransformation;

/**
 * Created by PC-Comp on 2/1/2017.
 */

public class GameViewHolder extends RecyclerView.ViewHolder {
    GameModel gameModel;
    private ItemClickListener onItemClickListener;
    @BindView(R.id.gameImage)
    ImageView gameImage;
    @BindView(R.id.gameTitle)
    TextView gameTitle;
    @BindView(R.id.gameDescription)
    TextView gameDescription;

    public GameViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(GameModel gameModel, Context context, ItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        this.gameModel = gameModel;
        Picasso.with(context).load(gameModel.getResourceDrawable()).transform(new PicassoCircleTransformation()).into(gameImage);
        gameTitle.setText(gameModel.getGameTitle());
        gameDescription.setText(gameModel.getGameDescription());

    }

    @OnClick(R.id.play)
    public void clicked() {
        onItemClickListener.onItemClick(gameModel);
    }
}
