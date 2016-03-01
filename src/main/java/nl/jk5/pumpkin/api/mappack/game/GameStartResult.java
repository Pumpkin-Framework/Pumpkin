package nl.jk5.pumpkin.api.mappack.game;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public final class GameStartResult {

    private final boolean success;
    private final Text failReason;

    private GameStartResult(boolean success, Text failReason){
        this.success = success;
        this.failReason = failReason;
    }

    public boolean isSuccess(){
        return this.success;
    }

    public Text message(){
        if(!this.success){
            return this.failReason;
        }
        return Text.of(TextColors.GREEN, "Game starting");
    }

    public static GameStartResult failed(Text reason){
        return new GameStartResult(false, reason);
    }

    public static GameStartResult success(){
        return new GameStartResult(true, null);
    }
}
