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

    public void register() {
        mInkService.register("gkaslga", "fasfasfa");

    }

    public InkService getInkService() {
        return mInkService;
    }

    public interface InkService {
        @FormUrlEncoded
        @POST(Constants.REGISTER_URL)
        Call<ResponseBody> register(@Field("login") String login, @Field("password") String password);

        @FormUrlEncoded
        @POST(Constants.LOGIN_URL)
        Call<ResponseBody> login(@Field("login") String login, @Field("password") String password);

        @POST(Constants.FRIENDS_URL)
        @FormUrlEncoded
        Call<ResponseBody> getFriends(@Field("user_id") String userId);

        @POST(Constants.SINGLE_USER_URL)
        @FormUrlEncoded
        Call<ResponseBody> getSingleUserDetails(@Field("user_id") String userId);
    }
}
