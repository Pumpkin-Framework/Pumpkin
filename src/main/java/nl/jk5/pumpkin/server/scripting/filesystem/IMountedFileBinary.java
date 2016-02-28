package nl.jk5.pumpkin.server.scripting.filesystem;

import java.io.*;

public interface IMountedFileBinary extends IMountedFile {

    int read() throws IOException;
    void write(int data) throws IOException;
    void close() throws IOException;
    void flush() throws IOException;
}
