package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;
import java.util.Random;

abstract class InputStreamFileSystem implements FileSystem {

    private final TIntObjectMap<Handle> handles = new TIntObjectHashMap<>();

    @Override
    public boolean isReadOnly() {
        return true;
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
        synchronized (this) {
            if(mode == Mode.Read && exists(path) && !isDirectory(path)){
                int handle = this.newHandleId();
                Optional<InputChannel> inputChannel = openInputChannel(path);
                if(!inputChannel.isPresent()){
                    throw new FileNotFoundException(path);
                }
                this.handles.put(handle, new SimpleHandle(this, handle, path, inputChannel.get()));
                return handle;
            }else{
                throw new FileNotFoundException(path);
            }
        }
    }

    @Override
    public Handle getHandle(int handle) {
        synchronized (this) {
            return this.handles.get(handle);
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            this.handles.forEachValue(handle -> {
                handle.close();
                return true;
            });
            this.handles.clear();
        }
    }

    protected abstract Optional<InputChannel> openInputChannel(String path) throws IOException;

    protected interface InputChannel extends ReadableByteChannel {

        @Override
        boolean isOpen();

        @Override
        void close() throws IOException;

        long getPosition() throws IOException;

        long setPosition(long position) throws IOException;

        int read(byte[] dst) throws IOException;

        @Override
        default int read(ByteBuffer dst) throws IOException {
            if(dst.hasArray()){
                return read(dst.array());
            }else{
                int count = Math.max(0, dst.limit() - dst.position());
                byte[] buffer = new byte[count];
                int n = read(buffer);
                if(n > 0){
                    dst.put(buffer, 0, n);
                }
                return n;
            }
        }
    }

    protected class InputStreamChannel implements InputChannel {

        private final InputStream inputStream;

        public InputStreamChannel(InputStream inputStream){
            this.inputStream = inputStream;
        }

        public boolean open = true;

        private long position = 0;

        @Override
        public long setPosition(long position) {
            this.position = position;
            return position;
        }

        @Override
        public long getPosition() {
            return this.position;
        }

        @Override
        public boolean isOpen() {
            return this.open;
        }

        @Override
        public void close() throws IOException {
            if(open){
                open = false;
                this.inputStream.close();
            }
        }

        @Override
        public int read(byte[] dst) throws IOException {
            int read = this.inputStream.read(dst);
            this.position += read;
            return read;
        }
    }

    // ----------------------------------------------------------------------- //

    private class SimpleHandle implements Handle {

        private final InputStreamFileSystem owner;
        private final int handle;
        private final String path;
        private final InputChannel channel;

        public SimpleHandle(InputStreamFileSystem owner, int handle, String path, InputChannel channel) {
            this.owner = owner;
            this.handle = handle;
            this.path = path;
            this.channel = channel;
        }

        @Override
        public long position() throws IOException {
            return this.channel.getPosition();
        }

        @Override
        public long length() {
            return this.owner.size(this.path);
        }

        @Override
        public void close() {
            if(this.channel.isOpen()){
                this.owner.handles.remove(this.handle);
                try{
                    this.channel.close();
                }catch(IOException ignored){}
            }
        }

        @Override
        public int read(byte[] into) throws IOException {
            return this.channel.read(into);
        }

        @Override
        public long seek(long to) throws IOException {
            return this.channel.setPosition(to);
        }

        @Override
        public void write(byte[] value) throws IOException {
            throw new IOException("bad file descriptor");
        }
    }

    private int newHandleId(){
        Random random = new Random();
        int id;
        do{
            id = random.nextInt();
        }while(this.handles.containsKey(id));
        return id;
    }
}
