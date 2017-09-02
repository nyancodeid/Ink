package kashmirr.social.models;

/**
 * Created by PC-Comp on 2/1/2017.
 */

public class GameModel {
    private String gameTitle;
    private String gameDescription;
    private String gameType;
    private int resourceDrawable;

    public GameModel(String gameTitle, String gameDescription, String gameType, int resourceDrawable) {
        this.gameTitle = gameTitle;
        this.resourceDrawable = resourceDrawable;
        this.gameDescription = gameDescription;
        this.gameType = gameType;
    }


    public int getResourceDrawable() {
        return resourceDrawable;
    }

    public void setResourceDrawable(int resourceDrawable) {
        this.resourceDrawable = resourceDrawable;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public String getGameDescription() {
        return gameDescription;
    }

    public void setGameDescription(String gameDescription) {
        this.gameDescription = gameDescription;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
}
