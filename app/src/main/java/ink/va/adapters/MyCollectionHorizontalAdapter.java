package ink.va.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.va.R;

import java.util.LinkedList;
import java.util.List;

import ink.va.models.MyCollectionModel;
import ink.va.view_holders.MyCollectionHorizontalViewHolder;

/**
 * Created by PC-Comp on 12/22/2016.
 */

public class MyCollectionHorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<MyCollectionModel> myCollectionModels;
    private Context context;
    private OnCollectionClickListener onCollectionClickListener;

    public MyCollectionHorizontalAdapter(Context context) {
        myCollectionModels = new LinkedList<>();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_collection_horizontal_list, parent, false);
        return new MyCollectionHorizontalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MyCollectionHorizontalViewHolder) holder).initData(context, myCollectionModels.get(position), onCollectionClickListener);
    }

    @Override
    public int getItemCount() {
        return myCollectionModels.size();
    }

    public void setMyCollectionModels(List<MyCollectionModel> myCollectionModels) {
        this.myCollectionModels = myCollectionModels;
    }

    public interface OnCollectionClickListener {
        void onMoreClicked(View view, MyCollectionModel myCollectionModel);

        void onCollectionClicked(MyCollectionModel myCollectionModel);
    }

    public void setOnCollectionClickListener(OnCollectionClickListener onCollectionClickListener) {
        this.onCollectionClickListener = onCollectionClickListener;
        notifyDataSetChanged();
    }
}
