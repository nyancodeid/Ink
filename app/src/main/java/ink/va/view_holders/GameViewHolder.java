package ink.va.view_holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.interfaces.ItemClickListener;
import ink.va.models.GameModel;

/**
 * Created by PC-Comp on 2/1/2017.
 */

public class GameViewHolder extends RecyclerView.ViewHolder {
    GameModel gameModel;
    private ItemClickListener onItemClickListener;
    @Bind(R.id.gameImage)
    ImageView gameImage;
    @Bind(R.id.gameTitle)
    TextView gameTitle;
    @Bind(R.id.gameDescription)
    TextView gameDescription;

    public GameViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(GameModel gameModel, Context context, ItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        this.gameModel = gameModel;
        gameImage.setImageResource(gameModel.getResourceDrawable());
        gameTitle.setText(gameModel.getGameTitle());
        gameDescription.setText(gameModel.getGameDescription());

    }

    @OnClick(R.id.play)
    public void clicked() {
        onItemClickListener.onItemClick(gameModel);
    }
}
