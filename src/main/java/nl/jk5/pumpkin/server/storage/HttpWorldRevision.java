package nl.jk5.pumpkin.server.storage;

import nl.jk5.pumpkin.api.mappack.WorldFile;
import nl.jk5.pumpkin.api.mappack.WorldRevision;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class HttpWorldRevision implements WorldRevision {

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public Date getCreated() {
        return null;
    }

    @Override
    public Collection<WorldFile> getFiles() {
        return null;
    }

    public static CompletableFuture<HttpWorldRevision> byId(int id){
        return null;
    }
}
