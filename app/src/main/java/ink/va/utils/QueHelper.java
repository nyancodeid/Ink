package ink.va.utils;

import android.content.Context;
import android.content.Intent;

import ink.va.models.ChatModel;
import ink.va.service.SendMessageService;

/**
 * Created by USER on 2016-06-26.
 */
public class QueHelper {
    private ChatModel chatModel;

    public QueHelper() {
    }

    public ChatModel getChatModel() {
        return chatModel;
    }

    public void attachToQue(
            final String mOpponentId,
            final String message,
            final int sentItemLocation,
            final boolean hasGif, final String gifUrl, Context context) {
        Intent intent = new Intent(context, SendMessageService.class);
        intent.putExtra("opponentId", mOpponentId);
        intent.putExtra("message", message);
        intent.putExtra("hasGif", hasGif);
        intent.putExtra("gifUrl", gifUrl);
        intent.putExtra("sentItemLocation", sentItemLocation);
        context.startService(intent);
    }
}
