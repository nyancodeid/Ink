package ink.va.view_holders;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ink.va.R;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ink.va.interfaces.MyMessagesItemClickListener;
import ink.va.models.UserMessagesModel;
import ink.va.utils.CircleTransform;
import ink.va.utils.Constants;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by PC-Comp on 1/31/2017.
 */

public class UserMessagesViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.messagesUserName)
    TextView messagesUserName;
    @BindView(R.id.messageBody)
    TextView messageBody;
    @BindView(R.id.messagesImage)
    ImageView messagesImage;
    private MyMessagesItemClickListener onItemClickListener;
    private UserMessagesModel userMessagesModel;
    private SharedHelper sharedHelper;
    private Context context;

    public UserMessagesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void initData(UserMessagesModel userMessagesModel, Context context, @Nullable MyMessagesItemClickListener onItemClickListener) {
        this.context = context;
        this.userMessagesModel = userMessagesModel;
        if (sharedHelper == null) {
            sharedHelper = new SharedHelper(context);
        }
        this.onItemClickListener = onItemClickListener;
        messagesUserName.setText(context.getString(R.string.loadingText));
        checkOpponentNames();

        String message = userMessagesModel.getMessage();
        String finalMessage;

        if (userMessagesModel.getUserId().equals(sharedHelper.getUserId())) {
            if (userMessagesModel.getMessage().isEmpty()) {
                finalMessage = context.getString(R.string.you) + context.getString(R.string.sentSticker);
            } else {
                finalMessage = context.getString(R.string.you) + message.replaceAll(Constants.TYPE_MESSAGE_ATTACHMENT, "");
            }

        } else {
            if (userMessagesModel.getMessage().isEmpty()) {
                String firstName = userMessagesModel.getFirstName() != null ? userMessagesModel.getFirstName().isEmpty() ? context.getString(R.string.NA) : userMessagesModel.getFirstName() :
                        context.getString(R.string.NA);
                String lastName = userMessagesModel.getLastName() != null ? userMessagesModel.getLastName().isEmpty() ? "" : userMessagesModel.getLastName() :
                        "";

                finalMessage = firstName + " " + lastName + " : " + context.getString(R.string.sentSticker);
            } else {

                String firstName = userMessagesModel.getFirstName() != null ? userMessagesModel.getFirstName().isEmpty() ? context.getString(R.string.NA) : userMessagesModel.getFirstName() :
                        context.getString(R.string.NA);
                String lastName = userMessagesModel.getLastName() != null ? userMessagesModel.getLastName().isEmpty() ? "" : userMessagesModel.getLastName() :
                        "";

                finalMessage = firstName + " " + lastName + " : " + message.replaceAll(Constants.TYPE_MESSAGE_ATTACHMENT, "");
            }
        }

        messageBody.setText(StringEscapeUtils.unescapeJava(finalMessage));
        if (!userMessagesModel.getImageName().isEmpty()) {
            String encodedImage = Uri.encode(userMessagesModel.getImageName());

            String url = Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage;
            if (userMessagesModel.isSocialAccount()) {
                url = userMessagesModel.getImageName();
            }
            Ion.with(context).load(url)
                    .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(messagesImage);
        } else {
            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap()
                    .transform(new CircleTransform()).intoImageView(messagesImage);
        }

    }

    @OnClick(R.id.myMessagesRootLayout)
    public void rootClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(userMessagesModel);
        }
    }

    @OnLongClick(R.id.myMessagesRootLayout)
    public boolean longClicked() {
        if (onItemClickListener != null) {
            onItemClickListener.onItemLongClick(userMessagesModel);
        }
        return false;
    }


    private void checkOpponentNames() {
        if (userMessagesModel.getFirstName() == null || userMessagesModel.getFirstName().equals("null") || userMessagesModel.getFirstName().isEmpty()) {
            Retrofit.getInstance().getInkService().getSingleUserDetails(userMessagesModel.getOpponentId().equals(sharedHelper.getUserId())
                    ? userMessagesModel.getUserId() : userMessagesModel.getOpponentId(), sharedHelper.getUserId()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response == null) {
                        checkOpponentNames();
                        return;
                    }
                    if (response.body() == null) {
                        checkOpponentNames();
                        return;
                    }
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String firstName = jsonObject.optString("first_name");
                        String lastName = jsonObject.optString("last_name");
                        String imageUrl = jsonObject.optString("image_link");
                        boolean isSocialAccount = jsonObject.optBoolean("isSocialAccount");
                        messagesUserName.setText(firstName + " " + lastName);

                        userMessagesModel.setFirstName(firstName);
                        userMessagesModel.setLastName(lastName);
                        userMessagesModel.setSocialAccount(isSocialAccount);
                        userMessagesModel.setImageName(imageUrl);

                        String messageBodyText = messageBody.getText().toString().replaceAll(context.getString(R.string.NA), firstName + " " + lastName);
                        messageBody.setText(messageBodyText);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            if (isSocialAccount) {
                                Ion.with(context).load(imageUrl)
                                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(messagesImage);

                            } else {
                                String encodedImage = Uri.encode(imageUrl);

                                Ion.with(context).load(Constants.MAIN_URL + Constants.USER_IMAGES_FOLDER + encodedImage)
                                        .withBitmap().placeholder(R.drawable.no_background_image).transform(new CircleTransform()).intoImageView(messagesImage);

                            }
                        } else {
                            Ion.with(context).load(Constants.ANDROID_DRAWABLE_DIR + "no_image").withBitmap()
                                    .transform(new CircleTransform()).intoImageView(messagesImage);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    messagesUserName.setText(context.getString(R.string.NA));
                }
            });
        } else {
            messagesUserName.setText(userMessagesModel.getFirstName() + " " + userMessagesModel.getLastName());
        }
    }

}
