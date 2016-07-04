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


        @POST(Constants.SEND_MESSAGE_URL)
        @FormUrlEncoded
        Call<ResponseBody> sendMessage(@Field("user_id") String userId, @Field("opponent_id") String opponentId, @Field("message") String message, @Field("timezone") String timezone);


        @POST(Constants.SINGLE_USER_MESSAGES)
        @FormUrlEncoded
        Call<ResponseBody> getMyMessages(@Field("user_id") String userId);


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
