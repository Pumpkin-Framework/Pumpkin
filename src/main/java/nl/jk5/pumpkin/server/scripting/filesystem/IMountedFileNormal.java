package nl.jk5.pumpkin.server.scripting.filesystem;

import java.io.*;

public interface IMountedFileNormal extends IMountedFile {

    String readLine() throws IOException;
    void write(String data, int offset, int length, boolean append) throws IOException;
    void close() throws IOException;
    void flush() throws IOException;
}
