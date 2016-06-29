package ink.utils;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

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
                .baseUrl(Constants.MAIN_URL)
//                .addConverterFactory(GsonConverterFactory.create())
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

        @POST(Constants.REGISTER_TOKEN)
        @FormUrlEncoded
        Call<ResponseBody> registerToken(@Field("user_id") String userId, @Field("token") String token);

        @POST(Constants.UPDATE_DETAILS)
        @FormUrlEncoded
        Call<ResponseBody> updateUserDetails(@Field("user_id") String userId, @Field("first_name") String firstName,
                                             @Field("last_name") String lastName,
                                             @Field("address") String address,
                                             @Field("phone_number") String phoneNumber,
                                             @Field("relationship") String relationship,
                                             @Field("gender") String gender,
                                             @Field("facebook") String facebook,
                                             @Field("skype") String skype);

    }
}
