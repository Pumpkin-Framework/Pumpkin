package nl.jk5.pumpkin.server.scripting.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class Mount {

    private Mount(){}

    public static IWritableMount dir(File dir, long capacity){
        return new WritableDirMount(dir, capacity);
    }

    public static IWritableMount dir(File dir){
        return dir(dir, Long.MAX_VALUE);
    }

    public static IMount combine(IMount... parts){
        return new ComboMount(parts);
    }

    public static IMount zip(File zip, String subFolder) throws IOException {
        return new ZipMount(zip, subFolder);
    }

    public static IMount zip(File zip) throws IOException {
        return zip(zip, "");
    }

    public static IMount jar(File jar, String subFolder) throws IOException {
        return zip(jar, subFolder);
    }

    public static IMount jar(File jar) throws IOException {
        return zip(jar, "");
    }

    public static IMount readOnly(IMount mount){
        return new WrappedReadOnlyMount(mount);
    }

    private static final class WrappedReadOnlyMount implements IMount {

        private final IMount wrapped;

        public WrappedReadOnlyMount(IMount wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public boolean exists(String path) throws IOException {
            return wrapped.exists(path);
        }

        @Override
        public boolean isDirectory(String path) throws IOException {
            return wrapped.isDirectory(path);
        }

        @Override
        public InputStream openForRead(String path) throws IOException {
            return wrapped.openForRead(path);
        }

        @Override
        public long getSize(String path) throws IOException {
            return wrapped.getSize(path);
        }

        @Override
        public void list(String path, List<String> contents) throws IOException {
            wrapped.list(path, contents);
        }
    }
}
