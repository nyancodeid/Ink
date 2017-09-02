package kashmirr.social.interfaces;

/**
 * Created by USER on 2016-07-11.
 */
public interface RequestListener {
    void onAcceptClicked(int position);

    void onDeclineClicked(int position);
    void onItemClicked(int position);
}
