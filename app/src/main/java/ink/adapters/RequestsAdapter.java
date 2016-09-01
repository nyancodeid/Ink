package ink.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ink.R;

import java.util.List;

import fab.FloatingActionButton;
import ink.interfaces.RequestListener;
import ink.models.RequestsModel;
import ink.utils.CircleTransform;
import ink.utils.Constants;

/**
 * Created by USER on 2016-07-11.
 */
public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private List<RequestsModel> requestsModels;
    private Context mContext;
    private RequestListener requestListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView requesterMessage;
        public ImageView requesterImage;
        public FloatingActionButton accept, decline;
        public RelativeLayout requestRootLayout;

        public ViewHolder(View view) {
            super(view);
            requesterMessage = (TextView) view.findViewById(R.id.requesterMessage);
            requesterImage = (ImageView) view.findViewById(R.id.requesterImage);
            accept = (FloatingActionButton) view.findViewById(R.id.acceptRequest);
            requestRootLayout = (RelativeLayout) view.findViewById(R.id.requestRootLayout);
            decline = (FloatingActionButton) view.findViewById(R.id.declineRequest);
        }
    }


    public RequestsAdapter(List<RequestsModel> requestsModels, Context context) {
        mContext = context;
        this.requestsModels = requestsModels;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_request_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        RequestsModel requestsModel = requestsModels.get(position);

        if (!requestsModel.getRequesterImage().isEmpty()) {
            if (requestsModel.isSocialAccount()) {
                Glide.with(mContext).load(requestsModel.getRequesterImage()).placeholder(R.drawable.no_background_image).transform(new CircleTransform(mContext)).into(holder.requesterImage);
            } else {
                Glide.with(mContext).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER +
                        requestsModel.getRequesterImage()).placeholder(R.drawable.no_background_image).transform(new CircleTransform(mContext)).into(holder.requesterImage);
            }
        } else {
            Glide.with(mContext).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").placeholder(R.drawable.no_background_image).transform(new CircleTransform(mContext)).into(holder.requesterImage);
        }
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestListener != null) {
                    requestListener.onAcceptClicked(position);
                }
            }
        });
        holder.decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestListener != null) {
                    requestListener.onDeclineClicked(position);
                }
            }
        });
        holder.requestRootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestListener != null) {
                    requestListener.onItemClicked(position);
                }
            }
        });

        if (requestsModel.getType().equals(Constants.REQUEST_RESPONSE_TYPE_GROUP)) {
            holder.requesterMessage.setText(requestsModel.getRequesterName() + " " + mContext.getString(R.string.personRequestedText) + " " +
                    requestsModel.getGroupName() + " " + mContext.getString(R.string.groupText));
        } else if (requestsModel.getType().equals(Constants.REQUEST_RESPONSE_TYPE_FRIEND_REQUEST)) {
            holder.requesterMessage.setText(requestsModel.getRequesterName() + " " + mContext.getString(R.string.tobeFriend));
        } else if (requestsModel.getType().equals(Constants.REQUEST_RESPONSE_TYPE_LOCATION_REQUEST)) {
            holder.requesterMessage.setText(requestsModel.getRequesterName() + " " + mContext.getString(R.string.requestedShareLocation));
        }


    }


    @Override
    public int getItemCount() {
        return requestsModels.size();
    }

    public void setRequestListener(RequestListener requestListener) {
        this.requestListener = requestListener;
    }
}

