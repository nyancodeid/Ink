package kashmirr.social.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.kashmirr.social.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import kashmirr.social.adapters.GamesAdapter;
import kashmirr.social.interfaces.ItemClickListener;
import kashmirr.social.models.GameModel;
import kashmirr.social.utils.GameList;

import static kashmirr.social.utils.Constants.GAME_BLACK_JACK;
import static kashmirr.social.utils.Constants.GAME_MAFIA;

public class GamesActivity extends BaseActivity implements ItemClickListener {

    @BindView(R.id.gamesRecycler)
    RecyclerView gamesRecycler;
    private GamesAdapter gamesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.games));
        gamesAdapter = new GamesAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        gamesRecycler.setLayoutManager(linearLayoutManager);
        gamesRecycler.setAdapter(gamesAdapter);
        gamesAdapter.setOnItemClickListener(this);
        gamesAdapter.setGameModelList(GameList.buildGameList(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Object clickedItem) {
        GameModel gameModel = (GameModel) clickedItem;
        switch (gameModel.getGameType()) {
            case GAME_BLACK_JACK:
                startActivity(new Intent(this, BlackJackHome.class));
                break;
            case GAME_MAFIA:
//                Toast.makeText(this, getString(R.string.gameInBet), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, Mafia.class));
                break;
        }
    }
}
