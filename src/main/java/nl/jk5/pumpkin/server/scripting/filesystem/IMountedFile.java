package nl.jk5.pumpkin.server.scripting.filesystem;

import java.io.IOException;

public interface IMountedFile {

    void close() throws IOException;
}