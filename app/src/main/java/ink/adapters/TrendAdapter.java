package ink.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.R;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import ink.fragments.TrendModel;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class TrendAdapter extends RecyclerView.Adapter<TrendAdapter.ViewHolder> {
    private ArrayList<TrendModel> trendModelArrayList;
    private Context context;

    public TrendAdapter(Context context, ArrayList<TrendModel> trendModelArrayList) {
        this.context = context;
        this.trendModelArrayList = trendModelArrayList;
    }

    @Override
    public TrendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View trendSingleView = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_trend_view, parent, false);
        return new ViewHolder(trendSingleView);
    }

    @Override
    public void onBindViewHolder(TrendAdapter.ViewHolder holder, int position) {
        final TrendModel trendModel = trendModelArrayList.get(position);
        if (trendModel.isTop()) {
            holder.premiumBadgeIcon.setVisibility(View.VISIBLE);
            holder.premiumText.setVisibility(View.VISIBLE);
        } else {
            holder.premiumBadgeIcon.setVisibility(View.INVISIBLE);
            holder.premiumText.setVisibility(View.GONE);
        }
        holder.trendTitle.setText(trendModel.getTitle());
        holder.trendContent.setText(trendModel.getContent());
        if (trendModel.getImageUrl() != null && !trendModel.getImageUrl().isEmpty()) {
            holder.trendImage.setVisibility(View.VISIBLE);
            Ion.with(context).load(trendModel.getImageUrl()).withBitmap().placeholder(R.drawable.whats_trending_vector).intoImageView(holder.trendImage);
        } else {
            holder.trendImage.setVisibility(View.GONE);
        }
        holder.trendViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlToOpen = trendModel.getExternalUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(urlToOpen));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trendModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView trendTitle;
        private TextView trendContent;
        private TextView premiumText;
        private ImageView trendImage;
        private ImageView premiumBadgeIcon;
        private Button trendViewMore;

        public ViewHolder(View itemView) {
            super(itemView);
            trendTitle = (TextView) itemView.findViewById(R.id.trendTitle);
            trendContent = (TextView) itemView.findViewById(R.id.trendContent);
            premiumText = (TextView) itemView.findViewById(R.id.premiumText);
            trendImage = (ImageView) itemView.findViewById(R.id.trendImage);
            premiumBadgeIcon = (ImageView) itemView.findViewById(R.id.premiumBadgeIcon);
            trendViewMore = (Button) itemView.findViewById(R.id.trendViewMore);
        }
    }
}
