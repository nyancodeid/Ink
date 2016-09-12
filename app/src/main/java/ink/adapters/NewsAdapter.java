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
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<NewsModel> newsModels;
    private Context context;
    private NewsItemClickListener itemClickListener;
    private static final int FOOTER_VIEW = 4;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOTER_VIEW) {
            View footerView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.news_footer_view, parent, false);
            return new FooterView(footerView);
        }
        View baseSingleView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_single_view, parent, false);
        return new BaseViewHolder(baseSingleView);

    }

    public NewsAdapter(Context context) {
        this.context = context;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder unknownHolder, final int position) {
        if (position != newsModels.size()) {
            final BaseViewHolder holder = ((BaseViewHolder) unknownHolder);
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
                    holder.newsImage.setVisibility(View.VISIBLE);
                    Ion.with(context).load(newsTopic.imageUrl).withBitmap().placeholder(R.drawable.breaking_news_vector).intoImageView(holder.newsImage);
                } else {
                    holder.newsImage.setVisibility(View.GONE);
                }
            }
            holder.newsViewMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemClickListener != null) {
                        itemClickListener.onViewMoreClicked(holder.newsContent, position);
                    }
                }
            });
        } else {
            final FooterView holder = ((FooterView) unknownHolder);
            holder.loadMoreNews.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemClickListener != null) {
                        itemClickListener.onLoadMoreClicked(holder.loadMoreNews);
                    }
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        if (newsModels == null) {
            return 0;
        }
        if (newsModels.size() == 0) {
            return 1;
        }
        return newsModels.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (newsModels == null) {
            return 1;
        }
        if (position == newsModels.size()) {
            return FOOTER_VIEW;
        }
        return super.getItemViewType(position);
    }

    public class BaseViewHolder extends RecyclerView.ViewHolder {
        private TextView newsTitle;
        private TextView newsContent;
        private ImageView newsImage;
        private Button newsViewMore;

        public BaseViewHolder(View itemView) {
            super(itemView);
            newsTitle = (TextView) itemView.findViewById(R.id.newsTitle);
            newsContent = (TextView) itemView.findViewById(R.id.newsContent);
            newsImage = (ImageView) itemView.findViewById(R.id.newsImage);
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

    public class FooterView extends RecyclerView.ViewHolder {
        private Button loadMoreNews;

        public FooterView(View itemView) {
            super(itemView);
            loadMoreNews = (Button) itemView.findViewById(R.id.loadMoreNews);
        }
    }
}
