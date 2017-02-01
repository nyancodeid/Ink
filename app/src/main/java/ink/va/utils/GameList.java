package ink.va.utils;

import android.content.Context;

import com.ink.va.R;

import java.util.ArrayList;
import java.util.List;

import ink.va.models.GameModel;

import static ink.va.utils.Constants.GAME_BLACK_JACK;

/**
 * Created by PC-Comp on 2/1/2017.
 */

public class GameList {

    public static List<GameModel> buildGameList(Context context) {
        List<GameModel> gameModels = new ArrayList<>();
        gameModels.add(new GameModel(context.getString(R.string.blackJackTitle), context.getString(R.string.blackJackDescription),
                GAME_BLACK_JACK, R.drawable.black_jack_background));
        return gameModels;
    }
}
