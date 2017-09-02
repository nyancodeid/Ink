package kashmirr.social.utils;

import android.content.Context;

import com.kashmirr.social.R;

import java.util.ArrayList;
import java.util.List;

import kashmirr.social.models.GameModel;

import static kashmirr.social.utils.Constants.GAME_BLACK_JACK;
import static kashmirr.social.utils.Constants.GAME_MAFIA;

/**
 * Created by PC-Comp on 2/1/2017.
 */

public class GameList {

    public static List<GameModel> buildGameList(Context context) {
        List<GameModel> gameModels = new ArrayList<>();
        gameModels.add(new GameModel(context.getString(R.string.blackJackTitle), context.getString(R.string.blackJackDescription),
                GAME_BLACK_JACK, R.drawable.black_jack_background));
        gameModels.add(new GameModel(context.getString(R.string.mafiaTitle), context.getString(R.string.mafiaDescription),
                GAME_MAFIA, R.drawable.mafia_small_icon));
        return gameModels;
    }
}
