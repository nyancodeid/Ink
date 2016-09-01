package ink.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by PC-Comp on 9/1/2016.
 */
public class ColorModel {
    @SerializedName("user_id")
    public String userId;

    @SerializedName("status_bar")
    public String statusBar;

    @SerializedName("action_bar")
    public String actionBar;

    @SerializedName("menu_button")
    public String menuButton;

    @SerializedName("send_button")
    public String sendButton;

    @SerializedName("notification_icon")
    public String notificationIcon;

    @SerializedName("shop_icon")
    public String shopIcon;

    @SerializedName("hamburger_icon")
    public String hamburgerIcon;

    @SerializedName("left_header")
    public String leftHeader;

    @SerializedName("feed_background")
    public String feedBackground;

    @SerializedName("friends_background")
    public String friendsBackground;

    @SerializedName("messages_background")
    public String messagesBackground;

    @SerializedName("chat_background")
    public String chatBackground;

    @SerializedName("request_background")
    public String requestBackground;

    @SerializedName("opponent_bubble")
    public String opponentBubble;

    @SerializedName("own_bubble")
    public String ownBubble;

    @SerializedName("opponent_text")
    public String opponentText;

    @SerializedName("own_text")
    public String ownText;

    @SerializedName("chat_field")
    public String chatField;

    @SerializedName("id")
    public String itemId;

    @SerializedName("success")
    public boolean success;

    @SerializedName("cause")
    public String cause;
}
