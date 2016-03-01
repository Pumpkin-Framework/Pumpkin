package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Optional;

class ReadWriteFileSystem extends OutputStreamFileSystem implements FileInputStreamFileSystem {

    private final File root;

    private boolean lazySpaceUsedInitialized = false;
    private long lazySpaceUsed;

    public ReadWriteFileSystem(File root) {
        this.root = root;
    }

    @Override
    public File root() {
        return this.root;
    }

    @Override
    public long spaceTotal() {
        return -1;
    }

    @Override
    public long spaceUsed() {
        return -1;
    }

    /////////////////////////////////////////////////

    @Override
    public boolean delete(String path) {
        File file = new File(root, path);
        return file == root || file.delete();
    }

    @Override
    public boolean makeDirectory(String path) {
        return new File(root, path).mkdir();
    }

    @Override
    public boolean rename(String from, String to) {
        return new File(root, from).renameTo(new File(root, to));
    }

    @Override
    public boolean setLastModified(String path, long time) {
        return new File(root, path).setLastModified(time);
    }

    /////////////////////////////////////////////////

    @Override
    public Optional<InputStreamFileSystem.InputChannel> openInputChannel(String path) throws IOException {
        return Optional.of(new FileChannel(new File(root(), path)));
    }

    class FileChannel implements InputStreamFileSystem.InputChannel {

        private final java.nio.channels.FileChannel channel;

        public FileChannel(File file) throws IOException {
            this.channel = new RandomAccessFile(file, "r").getChannel();
        }

        @Override
        public boolean isOpen() {
            return this.channel.isOpen();
        }

        @Override
        public void close() throws IOException {
            this.channel.close();
        }

        @Override
        public long getPosition() throws IOException {
            return channel.position();
        }

        @Override
        public long setPosition(long position) throws IOException {
            channel.position(position);
            return channel.position();
        }

        @Override
        public int read(byte[] dst) throws IOException {
            return this.channel.read(ByteBuffer.wrap(dst));
        }
    }

    /////////////////////////////////////////////////

    @Override
    protected Optional<OutputHandle> openOutputHandle(int id, String path, Mode mode) throws FileNotFoundException {
        String strMode;
        if(mode == Mode.Append || mode == Mode.Write){
            strMode = "rw";
        }else{
            throw new IllegalArgumentException();
        }
        return Optional.of(new FileHandle(new RandomAccessFile(new File(root, path), strMode), this, id, path, mode));
    }

    protected class FileHandle extends OutputHandle {

        private final RandomAccessFile file;
        private final OutputStreamFileSystem owner;
        private final int handle;
        private final String path;
        private final Mode mode;

        public FileHandle(RandomAccessFile file, OutputStreamFileSystem owner, int handle, String path, Mode mode) {
            super(owner, handle, path);

            this.file = file;
            this.owner = owner;
            this.handle = handle;
            this.path = path;
            this.mode = mode;

            if(mode == Mode.Write){
                try {
                    file.setLength(0);
                }catch(IOException ignored){}
            }
        }

        @Override
        public long position() throws IOException {
            return file.getFilePointer();
        }

        @Override
        public long length() throws IOException {
            return file.length();
        }

        @Override
        public void write(byte[] value) throws IOException {
            file.write(value);
        }

        @Override
        public void close() {
            super.close();
            try{
                file.close();
            }catch(IOException ignored){}
        }

        @Override
        public long seek(long to) throws IOException {
            file.seek(to);
            return to;
        }
    }
}
