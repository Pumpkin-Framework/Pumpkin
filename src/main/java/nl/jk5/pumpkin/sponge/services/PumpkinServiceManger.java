package nl.jk5.pumpkin.sponge.services;

import nl.jk5.pumpkin.sponge.Log;
import nl.jk5.pumpkin.sponge.hooks.worlds.WorldHooksService;
import org.spongepowered.api.Game;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PumpkinServiceManger {

    private final Game game;

    private final WorldHooksService worldHooksService;

    public PumpkinServiceManger(Game game) {
        this.game = game;

        this.worldHooksService = provide(WorldHooksService.class, null);
    }

    @Nonnull
    private <T> T provide(@Nonnull Class<T> cl, @Nullable Class<? extends T> dummy){
        Optional<T> opt = game.getServiceManager().provide(cl);

        if(!opt.isPresent()){
            if(dummy == null){
                Log.error("Service " + cl.getSimpleName() + " is not available. Pumpkin is not able to run");
                throw new RuntimeException("Service " + cl.getSimpleName() + " is not available. Pumpkin is not able to run");
            }else{
                Log.warn(cl.getSimpleName() + " service is not available. Using a dummy");
                try {
                    return dummy.newInstance();
                } catch (Exception e) {
                    Log.error("Was not able to create an instance of the dummy implementation of " + cl.getSimpleName() + ". Pumpkin is not able to run");
                    throw new RuntimeException("Was not able to create an instance of the dummy implementation of " + cl.getSimpleName() + ". Pumpkin is not able to run");
                }
            }
        }else{
            Log.info("Successfully loaded service " + cl.getSimpleName());
            return opt.get();
        }
    }

    public WorldHooksService getWorldHooksService() {
        return this.worldHooksService;
    }
}
