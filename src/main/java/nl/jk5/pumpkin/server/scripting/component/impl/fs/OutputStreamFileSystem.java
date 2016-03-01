package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;

abstract class OutputStreamFileSystem extends InputStreamFileSystem {

    private final TIntObjectMap<OutputHandle> handles = new TIntObjectHashMap<>();

    @Override
    public boolean isReadOnly() {
        return false;
    }

    ////////////////////////////////////////////////////


    @Override
    public int open(String path, Mode mode) throws IOException {
        synchronized (this) {
            if(mode == Mode.Read){
                return super.open(path, mode);
            }
            if(!isDirectory(path)){
                int handle = this.newHandleId();
                Optional<OutputHandle> outputHandle = this.openOutputHandle(handle, path, mode);
                if(!outputHandle.isPresent()){
                    throw new FileNotFoundException(path);
                }
                this.handles.put(handle, outputHandle.get());
                return handle;
            }else {
                throw new FileNotFoundException(path);
            }
        }
    }

    @Override
    public Handle getHandle(int handle) {
        synchronized (this) {
            Handle h = super.getHandle(handle);
            if(h != null){
                return h;
            }
            return this.handles.get(handle);
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            super.close();
            this.handles.forEachValue(value -> {
                value.close();
                return true;
            });
            this.handles.clear();
        }
    }

    ////////////////////////////////////////////////////

    protected abstract Optional<OutputHandle> openOutputHandle(int id, String path, Mode mode) throws FileNotFoundException;

    ////////////////////////////////////////////////////

    protected abstract class OutputHandle implements Handle {

        private final OutputStreamFileSystem owner;
        private final int handle;
        private final String path;

        private boolean closed = false;

        public OutputHandle(OutputStreamFileSystem owner, int handle, String path) {
            this.owner = owner;
            this.handle = handle;
            this.path = path;
        }

        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() {
            if(!this.closed){
                closed = true;
                this.owner.handles.remove(this.handle);
            }
        }

        @Override
        public int read(byte[] into) throws IOException {
            throw new IOException("bad file descriptor");
        }

        @Override
        public long seek(long to) throws IOException {
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
