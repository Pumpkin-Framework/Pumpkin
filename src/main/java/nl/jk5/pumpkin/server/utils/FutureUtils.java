package nl.jk5.pumpkin.server.utils;

import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.exception.MappackLoadingException;
import org.asynchttpclient.ListenableFuture;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class FutureUtils {

    private FutureUtils(){
        throw new UnsupportedOperationException("Can not make instances of FutureUtils");
    }

    public static <T, Y> Optional<T> getResult(ListenableFuture<T> future, CompletableFuture<Y> errorFuture){
        try {
            T response = future.get();
            if(response == null){
                Log.error("Got null response. Should not happen", new Exception("Marker"));
                errorFuture.completeExceptionally(new MappackLoadingException("Got empty server response"));
                return Optional.empty();
            }
            return Optional.of(response);
        } catch (InterruptedException ignored) {
            return Optional.empty();
        } catch (ExecutionException e) {
            Log.error("Could not read data from server", e.getCause());
            errorFuture.completeExceptionally(e.getCause());
            return Optional.empty();
        }
    }
}
