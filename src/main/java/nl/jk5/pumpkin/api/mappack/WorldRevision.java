package nl.jk5.pumpkin.api.mappack;

import java.util.Collection;
import java.util.Date;

public interface WorldRevision {

    int getId();

    Date getCreated();

    Collection<WorldFile> getFiles();
}
