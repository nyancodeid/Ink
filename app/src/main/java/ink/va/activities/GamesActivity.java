package ink.va.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.adapters.GamesAdapter;
import ink.va.interfaces.ItemClickListener;
import ink.va.models.GameModel;
import ink.va.utils.GameList;

import static ink.va.utils.Constants.GAME_BLACK_JACK;

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
                startActivity(new Intent(this, BlackJack.class));
                break;
        }
    }
}
