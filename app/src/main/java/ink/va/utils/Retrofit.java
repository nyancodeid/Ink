package ink.va.utils;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by USER on 2016-06-19.
 */
public class Retrofit {
    private static Retrofit retrofitInstance = new Retrofit();

    public static Retrofit getInstance() {
        return retrofitInstance;
    }

    public InkService inkService;
    public MusicCloudInterface musicCloudInterface;
    private NewsInterface newsInterface;

    private Retrofit() {
        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.MAIN_URL)
                .build();

        retrofit2.Retrofit cloudRetrofit = new retrofit2.Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.CLOUD_API_URL)
                .build();

        retrofit2.Retrofit newsInterfaceRetrofit = new retrofit2.Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.NEWS_BASE_URL)
                .build();


        inkService = retrofit.create(InkService.class);
        musicCloudInterface = cloudRetrofit.create(MusicCloudInterface.class);
        newsInterface = newsInterfaceRetrofit.create(NewsInterface.class);

    }

    public MusicCloudInterface getMusicCloudInterface() {
        return musicCloudInterface;
    }

    public NewsInterface getNewsInterface() {
        return newsInterface;
    }

    public InkService getInkService() {
        return inkService;
    }

    public interface InkService {
        @FormUrlEncoded
        @POST(Constants.REGISTER_URL)
        Call<ResponseBody> register(@Field("login")
                                    String login, @Field("password")
                                    String password, @Field("firstName") String firstName,
                                    @Field("lastName") String lastName);

        @FormUrlEncoded
        @POST(Constants.LOGIN_URL)
        Call<ResponseBody> login(@Field("login") String login, @Field("password") String password);


        @POST(Constants.FRIENDS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getFriends(@Field("user_id") String userId);

        @POST(Constants.SINGLE_USER_URL)
        @FormUrlEncoded
        Call<ResponseBody> getSingleUserDetails(@Field("user_id") String userId,
                                                @Field("currentUserId") String currentUserId);

        @POST(Constants.SEND_FRIEND_REQUEST_URL)
        @FormUrlEncoded
        Call<ResponseBody> requestFriend(@Field("requesterId") String requesterId,
                                         @Field("requestedUserId") String requestedUserId,
                                         @Field("requesterName") String requesterFullName);


        @POST(Constants.REQUEST_LOCATION_URL)
        @FormUrlEncoded
        Call<ResponseBody> requestFriendLocation(@Field("requesterId") String requesterId,
                                                 @Field("requestedUserId") String requestedUserId,
                                                 @Field("requesterName") String requesterFullName,
                                                 @Field("requestedUserName") String requestedUserName,
                                                 @Field("requestType") String requestType,
                                                 @Field("requesterImage") String requester_image);

        @POST(Constants.MESSAGES_URL)
        @FormUrlEncoded
        Call<ResponseBody> getMessages(@Field("user_id") String userId, @Field("opponent_id") String opponentId);


        @POST(Constants.GET_USER_PASSWORD)
        @FormUrlEncoded
        Call<ResponseBody> getUserPassword(@Field("userId") String userId,
                                           @Field("token") String token);

        @POST(Constants.CHANGE_PASSWORD)
        @FormUrlEncoded
        Call<ResponseBody> changePassword(@Field("userId") String userId,
                                          @Field("token") String token,
                                          @Field("password") String newPassword);

        @POST(Constants.SECURITY_QUESTION)
        @FormUrlEncoded
        Call<ResponseBody> setSecurityQuestion(@Field("userId") String userId,
                                               @Field("securityQuestion") String securityQuestion,
                                               @Field("securityAnswer") String securityAnswer);

        @POST(Constants.SEND_LOCATION_UPDATE_URL)
        @FormUrlEncoded
        Call<ResponseBody> sendLocationUpdate(@Field("opponent_id") String opponentId,
                                              @Field("longitude") String longitude,
                                              @Field("latitude") String latitude);

        @POST(Constants.SEARCH_GROUP_URL)
        @FormUrlEncoded
        Call<ResponseBody> searchGroups(@Field("userId") String userId,
                                        @Field("textToSearch") String textToSearch);


        @POST(Constants.SEARCH_PERSON_URL)
        @FormUrlEncoded
        Call<ResponseBody> searchPerson(@Field("userId") String currentUserId,
                                        @Field("textToSearch") String textToSearch);

        @POST(Constants.DELETE_POST_URL)
        @FormUrlEncoded
        Call<ResponseBody> deletePost(@Field("postId") String postId,
                                      @Field("attachmentName") String attachmentName);

        @POST(Constants.GROUP_REQUESTS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getMyRequests(@Field("ownerId") String userId);

        @POST(Constants.DELETE_ACCOUNT_URL)
        @FormUrlEncoded
        Call<ResponseBody> deleteAccount(@Field("userId") String userId);

        @POST(Constants.SHOP_COINS_URL)
        Call<ResponseBody> getCoins();

        @POST(Constants.SHOP_PACKS_URL)
        Call<ResponseBody> getPacks();

        @POST(Constants.PING_TIME_URL)
        @FormUrlEncoded
        Call<ResponseBody> pingTime(@Field("userId") String userId);

        @POST(Constants.GET_USER_STATUS)
        @FormUrlEncoded
        Call<ResponseBody> getUserStatus(@Field("userId") String userId);

        @POST(Constants.JOIN_GROUP_URL)
        @FormUrlEncoded
        Call<ResponseBody> requestJoin(@Field("ownerId") String ownerId,
                                       @Field("participantId") String participantId,
                                       @Field("participantName") String participantname,
                                       @Field("participantImage") String participantImage,
                                       @Field("participantGroupId") String participantGroupId);

        @POST(Constants.SEND_MESSAGE_URL)
        @FormUrlEncoded
        Call<ResponseBody> sendMessage(@Field("user_id") String userId,
                                       @Field("opponent_id") String opponentId,
                                       @Field("message") String message,
                                       @Field("timezone") String timezone,
                                       @Field("hasGif") boolean hasGif,
                                       @Field("gifUrl") String gifUrl);

        @POST(Constants.WAITERS_QUE)
        @FormUrlEncoded
        Call<ResponseBody> waitersQueAction(@Field("user_id") String userId,
                                            @Field("name") String name,
                                            @Field("status") String status,
                                            @Field("action") String action,
                                            @Field("opponentId") String opponentId);

        @POST(Constants.SEND_CHAT_ROULETTE_MESSAGE)
        @FormUrlEncoded
        Call<ResponseBody> sendChatRouletteMessage(@Field("userId") String userId,
                                                   @Field("opponentId") String opponentId,
                                                   @Field("message") String message);

        @POST(Constants.DISCONNECT_URL)
        @FormUrlEncoded
        Call<ResponseBody> sendDisconnectNotification(@Field("opponentId") String opponentId);

        @POST(Constants.NOTIFY_OPPONENT)
        @FormUrlEncoded
        Call<ResponseBody> notifyOpponent(@Field("opponentId") String opponentId,
                                          @Field("userId") String userId);

        @POST(Constants.GET_WAITERS)
        @FormUrlEncoded
        Call<ResponseBody> getWaiters(@Field("user_id") String userId);


        @POST(Constants.CUSTOMIZATION_URL)
        @FormUrlEncoded
        Call<ResponseBody> saveCustomization(@Field("type") String type,
                                             @Field("userId") String userId,
                                             @Field("statusBar") String statusBar,
                                             @Field("actionBar") String actionBar,
                                             @Field("menuButton") String menuButton,
                                             @Field("sendButton") String sendButton,
                                             @Field("notificationIcon") String notificationIcon,
                                             @Field("shopIcon") String shopIcon,
                                             @Field("hamburgerIcon") String hamburgerIcon,
                                             @Field("leftHeader") String leftHeader,
                                             @Field("feedBackground") String feedBackground,
                                             @Field("friendsBackground") String friendsBackground,
                                             @Field("messagesBackground") String messagesBackground,
                                             @Field("chatBackground") String chatBackground,
                                             @Field("requestBackground") String requestBackground,
                                             @Field("opponentBubble") String opponentBubble,
                                             @Field("ownBubble") String ownBubble,
                                             @Field("opponentText") String opponentText,
                                             @Field("ownText") String ownText,
                                             @Field("chatField") String chatField,
                                             @Field("trendColor") String trendColor);

        @POST(Constants.CUSTOMIZATION_URL)
        @FormUrlEncoded
        Call<ResponseBody> restoreCustomization(@Field("userId") String userId, @Field("type") String type);

        @POST(Constants.CUSTOMIZATION_URL)
        @FormUrlEncoded
        Call<ResponseBody> removeFromCloud(@Field("userId") String userId, @Field("type") String type);

        @POST(Constants.SINGLE_USER_MESSAGES)
        @FormUrlEncoded
        Call<ResponseBody> getMyMessages(@Field("user_id") String userId);

        @POST(Constants.GROUP_PARTICIPANTS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getParticipants(@Field("userId") String userId,
                                           @Field("groupId") String groupId);

        @POST(Constants.GROUP_MESSAGES_URL)
        @FormUrlEncoded
        Call<ResponseBody> getGroupMessages(@Field("group_id") String groupId,
                                            @Field("user_id") String userId);


        @POST(Constants.GROUP_MESSAGES_OPTIONS_URL)
        @FormUrlEncoded
        Call<ResponseBody> changeGroupMessages(@Field("type") String type,
                                               @Field("message") String message,
                                               @Field("messageId") String messageId);

        @POST(Constants.GROUP_OPTIONS_URL)
        @FormUrlEncoded
        Call<ResponseBody> groupOptions(@Field("type") String type,
                                        @Field("groupId") String groupId,
                                        @Field("groupName") String groupName,
                                        @Field("groupDescription") String groupDescription);


        @POST(Constants.RESPOND_TO_REQUEST_URL)
        @FormUrlEncoded
        Call<ResponseBody> respondToRequest(@Field("respondType") String respondType,
                                            @Field("participantId") String participantId,
                                            @Field("participantName") String participantName,
                                            @Field("participantImage") String participantImage,
                                            @Field("groupId") String groupId);

        @POST(Constants.GET_GROUP_URL)
        @FormUrlEncoded
        Call<ResponseBody> getGroups(@Field("user_id") String userId,
                                     @Field("type") String type);

        @POST(Constants.REMOVE_FRIEND_URL)
        @FormUrlEncoded
        Call<ResponseBody> removeFriend(@Field("ownerId") String ownerId,
                                        @Field("friendId") String friendId);

        @POST(Constants.CHECK_IS_FRIEND_URL)
        @FormUrlEncoded
        Call<ResponseBody> isFriendCheck(@Field("userId") String userId,
                                         @Field("friendId") String friendId);

        @POST(Constants.USER_COINS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getCoins(@Field("user_id") String userId);

        @POST(Constants.MY_GROUPS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getMyGroups(@Field("user_id") String userId);

        @POST(Constants.ADD_GROUP_URL)
        @FormUrlEncoded
        Call<ResponseBody> createGroup(@Field("user_id") String userId,
                                       @Field("base64") String base64,
                                       @Field("groupName") String groupName,
                                       @Field("groupDescription") String groupDescription,
                                       @Field("groupColor") String groupColor,
                                       @Field("ownerName") String ownerName,
                                       @Field("ownerImage") String ownerImage);

        @POST(Constants.ADD_GROUP_MESSAGE_URL)
        @FormUrlEncoded
        Call<ResponseBody> sendGroupMessage(@Field("group_id") String groupId,
                                            @Field("group_message") String groupMessage,
                                            @Field("sender_id") String senderId,
                                            @Field("sender_image") String senderImage,
                                            @Field("sender_name") String senderName);


        @POST(Constants.ADD_COMMENT_URL)
        @FormUrlEncoded
        Call<ResponseBody> addComment(@Field("commenter_id") String commenterId, @Field("commenter_image") String commenterImage,
                                      @Field("comment_body") String commentBody,
                                      @Field("post_id") String postId,
                                      @Field("first_name") String firstName,
                                      @Field("last_name") String lastName);

        @POST(Constants.CHAT_MESSAGES)
        @FormUrlEncoded
        Call<ResponseBody> getChatMessages(@Field("user_id") String userId);

        @POST(Constants.DELETE_MESSAGE_URL)
        @FormUrlEncoded
        Call<ResponseBody> deleteMessage(@Field("messageId") String messageId,
                                         @Field("currentUserId") String currentUserId,
                                         @Field("opponentId") String opponentId);

        @POST(Constants.REGISTER_TOKEN)
        @FormUrlEncoded
        Call<ResponseBody> registerToken(@Field("user_id") String userId, @Field("token") String token);


        @POST(Constants.USER_GIFS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getUserGifs(@Field("userId") String userId,
                                       @Field("authKey") String authKey);


        @POST(Constants.REQUEST_DELETE_URL)
        @FormUrlEncoded
        Call<ResponseBody> requestDelete(@Field("user_id") String userId, @Field("opponent_id") String opponentId);

        @POST(Constants.TREND_CATEGORIES_URL)
        @FormUrlEncoded
        Call<ResponseBody> getTrendCategories(@Field("token") String token);

        @POST(Constants.TREND_URL)
        @FormUrlEncoded
        Call<ResponseBody> getTrends(@Field("type") String categoryType);


        @POST(Constants.GET_POSTS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getPosts(@Field("user_id") String userId,
                                    @Field("offset") String offset,
                                    @Field("count") String count);


        @POST(Constants.GET_COMMENTS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getComments(@Field("userId") String userId,
                                       @Field("post_id") String postId);

        @POST(Constants.SOCIAL_LOGIN_URL)
        @FormUrlEncoded
        Call<ResponseBody> socialLogin(@Field("login") String emailOrLogin,
                                       @Field("firstName") String firstName,
                                       @Field("lastName") String lastName,
                                       @Field("imageUrl") String imageUrl,
                                       @Field("token") String token,
                                       @Field("type") String socialLoginType,
                                       @Field("userLink") String userLink,
                                       @Field("facebookName") String facebookName);

        @POST(Constants.LIKE_URL)
        @FormUrlEncoded
        Call<ResponseBody> likePost(@Field("user_id") String userId, @Field("post_id") String postId,
                                    @Field("isLiking") int isLiking);


        @POST(Constants.UPDATE_DETAILS)
        @FormUrlEncoded
        Call<ResponseBody> updateUserDetails(@Field("user_id") String userId, @Field("first_name") String firstName,
                                             @Field("last_name") String lastName,
                                             @Field("address") String address,
                                             @Field("phone_number") String phoneNumber,
                                             @Field("relationship") String relationship,
                                             @Field("gender") String gender,
                                             @Field("facebook") String facebook,
                                             @Field("skype") String skype,
                                             @Field("base64") String base64Image,
                                             @Field("status") String status,
                                             @Field("facebook_name") String facebookName,

                                             @Field("image_link") String imageLink);

        @Multipart
        @POST(Constants.MAKE_POST_URL)
        Call<ResponseBody> makePost(@PartMap Map<String, ProgressRequestBody> map,
                                    @Part("user_id") String userId,
                                    @Part("postBody") String postBody,
                                    @Part("googleAddress") String googleAddress,
                                    @Part("imageLink") String userImageLink,
                                    @Part("firstName") String firstName,
                                    @Part("lastName") String lastName,
                                    @Part("timezone") String timezone,
                                    @Part("type") String type,
                                    @Part("postId") String postId,
                                    @Part("shouldDelete") String shouldDelete);


        @Multipart
        @POST(Constants.SEND_MESSAGE_URL)
        Call<ResponseBody> sendMessageWithAttachment(@PartMap Map<String, ProgressRequestBody> map,
                                                     @Part("user_id") String userId,
                                                     @Part("opponent_id") String opponentId,
                                                     @Part("message") String message,
                                                     @Part("timezone") String timezone,
                                                     @Part("hasGif") boolean hasGif,
                                                     @Part("gifUrl") String gifUrl);

        @POST(Constants.MAKE_POST_URL)
        @FormUrlEncoded
        Call<ResponseBody> makePost(@Field("user_id") String userId,
                                    @Field("postBody") String postBody,
                                    @Field("googleAddress") String googleAddress,
                                    @Field("imageLink") String userImageLink,
                                    @Field("firstName") String firstName,
                                    @Field("lastName") String lastName,
                                    @Field("timezone") String timezone,
                                    @Field("type") String type,
                                    @Field("editedFileName") String editedFileName,
                                    @Field("postId") String postId,
                                    @Field("shouldDelete") String shouldDelete);


        @POST(Constants.COMMENT_OPTIONS_URL)
        @FormUrlEncoded
        Call<ResponseBody> commentOptions(@Field("type") String type,
                                          @Field("commentId") String commentId,
                                          @Field("newCommentBody") String $newCommentBody);

        @POST(Constants.GET_USER_LOGIN)
        @FormUrlEncoded
        Call<ResponseBody> getUserLogin(@Field("login") String login,
                                        @Field("token") String token);


        @POST(Constants.TEMPORARY_PASSWORD)
        @FormUrlEncoded
        Call<ResponseBody> getTemporaryPassword(@Field("login") String inputLogin,
                                                @Field("token") String token);
    }

    public interface MusicCloudInterface {
        @GET("/tracks?client_id=" + Constants.CLOUD_CLIENT_ID)
        Call<ResponseBody> getAllTracks();

        @GET("/tracks?client_id=" + Constants.CLOUD_CLIENT_ID)
        Call<ResponseBody> searchSong(@Query("q") String searchString);

    }

    public interface NewsInterface {
        @GET()
        Call<ResponseBody> getNews(@Url() String fullUrl);
    }
}
