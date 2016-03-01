package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import java.io.FileNotFoundException;
import java.io.IOException;

final class ReadOnlyWrapper implements FileSystem {

    private final FileSystem delegate;

    public ReadOnlyWrapper(FileSystem delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public long spaceTotal() {
        return delegate.spaceTotal();
    }

    @Override
    public long spaceUsed() {
        return delegate.spaceUsed();
    }

    @Override
    public boolean exists(String path) {
        return delegate.exists(path);
    }

    @Override
    public long size(String path) {
        return delegate.size(path);
    }

    @Override
    public boolean isDirectory(String path) {
        return delegate.isDirectory(path);
    }

    @Override
    public long lastModified(String path) {
        return delegate.lastModified(path);
    }

    @Override
    public String[] list(String path) throws FileNotFoundException {
        return delegate.list(path);
    }

    @Override
    public boolean delete(String path) {
        return false;
    }

    @Override
    public boolean makeDirectory(String path) {
        return false;
    }

    @Override
    public boolean rename(String from, String to) throws FileNotFoundException {
        return false;
    }

    @Override
    public boolean setLastModified(String path, long time) {
        return false;
    }

    @Override
    public int open(String path, Mode mode) throws IOException {
        if(mode == Mode.Write || mode == Mode.Append){
            throw new FileNotFoundException("read-only filesystem; cannot open for writing: " + path);
        }
        return delegate.open(path, mode);
    }

    @Override
    public Handle getHandle(int handle) {
        return delegate.getHandle(handle);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
