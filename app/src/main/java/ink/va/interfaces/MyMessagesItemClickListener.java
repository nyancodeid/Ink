package ink.va.interfaces;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public interface MyMessagesItemClickListener<T> {

    void onItemClick(T clickedItem);

    void onItemLongClick(T clickedItem);
}
