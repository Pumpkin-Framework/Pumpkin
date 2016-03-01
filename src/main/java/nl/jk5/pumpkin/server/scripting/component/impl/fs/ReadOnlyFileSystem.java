package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Optional;

class ReadOnlyFileSystem extends InputStreamFileSystem implements FileInputStreamFileSystem {

    private final File root;

    private boolean lazySpaceUsedInitialized = false;
    private long lazySpaceUsed;

    public ReadOnlyFileSystem(File root) {
        this.root = root;
    }

    @Override
    public File root() {
        return this.root;
    }

    @Override
    public long spaceTotal() {
        return this.spaceUsed();
    }

    @Override
    public long spaceUsed() {
        if(this.lazySpaceUsedInitialized){
            return this.lazySpaceUsed;
        }
        this.lazySpaceUsed = recurseSpaceUsed(this.root);
        this.lazySpaceUsedInitialized = true;
        return lazySpaceUsed;
    }

    private long recurseSpaceUsed(File file){
        if(file.isDirectory()){
            long total = 0;
            for(File f : file.listFiles()){
                total += recurseSpaceUsed(f);
            }
            return total;
        }else{
            return file.length();
        }
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
}
