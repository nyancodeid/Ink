package ink.utils;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

/**
 * Created by USER on 2016-06-19.
 */
public class Retrofit {
    private static Retrofit ourInstance = new Retrofit();

    public static Retrofit getInstance() {
        return ourInstance;
    }

    public InkService mInkService;

    private Retrofit() {
        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.MAIN_URL)
                .build();

        mInkService = retrofit.create(InkService.class);

    }

    public InkService getInkService() {
        return mInkService;
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
        Call<ResponseBody> getSingleUserDetails(@Field("user_id") String userId);

        @POST(Constants.MESSAGES_URL)
        @FormUrlEncoded
        Call<ResponseBody> getMessages(@Field("user_id") String userId, @Field("opponent_id") String opponentId);

        @POST(Constants.SEARCH_GROUP_URL)
        @FormUrlEncoded
        Call<ResponseBody> searchGroups(@Field("userId") String userId, @Field("textToSearch") String textToSearch);


        @POST(Constants.GROUP_REQUESTS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getMyRequests(@Field("ownerId") String userId);

        @POST(Constants.JOIN_GROUP_URL)
        @FormUrlEncoded
        Call<ResponseBody> requestJoin(@Field("ownerId") String ownerId,
                                       @Field("participantId") String participantId,
                                       @Field("participantName") String participantname,
                                       @Field("participantImage") String participantImage,
                                       @Field("participantGroupId") String participantGroupId);

        @POST(Constants.SEND_MESSAGE_URL)
        @FormUrlEncoded
        Call<ResponseBody> sendMessage(@Field("user_id") String userId, @Field("opponent_id") String opponentId, @Field("message") String message, @Field("timezone") String timezone);

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

        @POST(Constants.SINGLE_USER_MESSAGES)
        @FormUrlEncoded
        Call<ResponseBody> getMyMessages(@Field("user_id") String userId);

        @POST(Constants.GROUP_PARTICIPANTS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getParticipants(@Field("groupId") String groupId);

        @POST(Constants.GROUP_MESSAGES_URL)
        @FormUrlEncoded
        Call<ResponseBody> getGroupMessages(@Field("group_id") String groupId,
                                            @Field("user_id") String userId);


        @POST(Constants.RESPOND_TO_REQUEST_URL)
        @FormUrlEncoded
        Call<ResponseBody> respondToRequest(@Field("respondType") String respondType,
                                            @Field("participantId") String participantId,
                                            @Field("participantName") String participantName,
                                            @Field("participantImage") String participantImage,
                                            @Field("groupId") String groupId);

        @POST(Constants.GET_GROUP_URL)
        @FormUrlEncoded
        Call<ResponseBody> getGroups(@Field("user_id") String userId);

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


        @POST(Constants.REGISTER_TOKEN)
        @FormUrlEncoded
        Call<ResponseBody> registerToken(@Field("user_id") String userId, @Field("token") String token);


        @POST(Constants.REQUEST_DELETE_URL)
        @FormUrlEncoded
        Call<ResponseBody> requestDelete(@Field("user_id") String userId, @Field("opponent_id") String opponentId);


        @POST(Constants.GET_POSTS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getPosts(@Field("user_id") String userId);


        @POST(Constants.GET_COMMENTS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getComments(@Field("post_id") String postId);

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
        Call<ResponseBody> makePost(@PartMap Map<String, RequestBody> map, @Part("user_id") String userId,
                                    @Part("postBody") String postBody,
                                    @Part("googleAddress") String googleAddress,
                                    @Part("imageLink") String userImageLink,
                                    @Part("firstName") String firstName,
                                    @Part("lastName") String lastName, @Part("timezone") String timezone);


        @POST(Constants.MAKE_POST_URL)
        @FormUrlEncoded
        Call<ResponseBody> makePost(@Field("user_id") String userId, @Field("postBody") String postBody,
                                    @Field("googleAddress") String googleAddress,
                                    @Field("imageLink") String userImageLink,
                                    @Field("firstName") String firstName,
                                    @Field("lastName") String lastName, @Field("timezone") String timezone);
    }
}
