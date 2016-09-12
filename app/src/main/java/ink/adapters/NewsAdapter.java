package ink.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.R;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import ink.interfaces.NewsItemClickListener;
import ink.models.NewsModel;
import ink.models.NewsTopic;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.BaseViewHolder> {

    private ArrayList<NewsModel> newsModels;
    private Context context;
    private NewsItemClickListener itemClickListener;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View nesSingleView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_single_view, parent, false);
        return new BaseViewHolder(nesSingleView);
    }

    public NewsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, final int position) {
        NewsModel singleModel = newsModels.get(position);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.newsTitle.setText(Html.fromHtml(singleModel.newsContent, Html.FROM_HTML_MODE_LEGACY));
            holder.newsContent.setText(Html.fromHtml(singleModel.newsContent, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.newsTitle.setText(Html.fromHtml(singleModel.newsContent));
            holder.newsContent.setText(Html.fromHtml(singleModel.newsContent));
        }

        ArrayList<NewsTopic> newsTopics = singleModel.newsTopics;
        if (!newsTopics.isEmpty()) {
            String largeDescription;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                largeDescription = String.valueOf(Html.fromHtml(newsTopics.get(0).description, Html.FROM_HTML_MODE_LEGACY));
            } else {
                largeDescription = String.valueOf(Html.fromHtml(newsTopics.get(0).description));
            }
            holder.newsContent.setText(largeDescription);
            NewsTopic newsTopic = newsTopics.get(0);
            if (newsTopic.imageUrl != null && !newsTopic.imageUrl.equals("null") && !newsTopic.imageUrl.isEmpty()) {
                Ion.with(context).load(newsTopic.imageUrl).withBitmap().placeholder(R.drawable.breaking_news_vector).intoImageView(holder.newsImage);
            }
        }
        holder.newsViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onViewMoreClicked(holder.newsContent);
                }
            }
        });
        holder.goToWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }


    @Override
    public int getItemCount() {
        if (newsModels == null) {
            return 0;
        }
        return newsModels.size();
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {
        private TextView newsTitle;
        private TextView newsContent;
        private ImageView newsImage;
        private ImageView goToWeb;
        private Button newsViewMore;

        public BaseViewHolder(View itemView) {
            super(itemView);
            newsTitle = (TextView) itemView.findViewById(R.id.newsTitle);
            newsContent = (TextView) itemView.findViewById(R.id.newsContent);
            newsImage = (ImageView) itemView.findViewById(R.id.newsImage);
            goToWeb = (ImageView) itemView.findViewById(R.id.goToWeb);
            newsViewMore = (Button) itemView.findViewById(R.id.newsViewMore);
        }
    }

    public void setNewsModels(ArrayList<NewsModel> newsModels) {
        this.newsModels = newsModels;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(NewsItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
