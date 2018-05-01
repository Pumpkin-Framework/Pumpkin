package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import nl.jk5.pumpkin.server.Log;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileInputStreamFileSystem extends InputStreamFileSystem {

    private final ArchiveDirectory archive;

    public ZipFileInputStreamFileSystem(ArchiveDirectory archive) {
        this.archive = archive;
    }

    private boolean lazySpaceUsedInitialized = false;
    private long lazySpaceUsed;

    @Override
    public long spaceTotal() {
        return this.spaceUsed();
    }

    @Override
    public long spaceUsed() {
        if(this.lazySpaceUsedInitialized){
            return this.lazySpaceUsed;
        }
        synchronized (ZipFileInputStreamFileSystem.class){
            this.lazySpaceUsed = recurseSpaceUsed(this.archive);
            this.lazySpaceUsedInitialized = true;
            return lazySpaceUsed;
        }
    }

    private long recurseSpaceUsed(ArchiveDirectory directory){
        long total = 0;
        for(Archive d : directory.children){
            if(d instanceof ArchiveDirectory){
                total += recurseSpaceUsed(((ArchiveDirectory) d));
            }else if(d instanceof ArchiveFile){
                total += ((ArchiveFile) d).size;
            }
        }
        return total;
    }

    // ----------------------------------------------------------------------- //


    @Override
    public boolean exists(String path) {
        synchronized (ZipFileInputStreamFileSystem.class){
            return entry(path).isPresent();
        }
    }

    @Override
    public long size(String path) {
        synchronized (ZipFileInputStreamFileSystem.class){
            Optional<Archive> a = entry(path);
            if(a.isPresent() && a.get().isDirectory){
                return a.get().size();
            }else{
                return 0;
            }
        }
    }

    @Override
    public boolean isDirectory(String path) {
        synchronized (ZipFileInputStreamFileSystem.class){
            Optional<Archive> a = entry(path);
            return a.isPresent() && a.get().isDirectory;
        }
    }

    @Override
    public long lastModified(String path) {
        synchronized (ZipFileInputStreamFileSystem.class){
            Optional<Archive> a = entry(path);
            if(a.isPresent()){
                return a.get().lastModified;
            }else{
                return 0;
            }
        }
    }

    @Override
    public String[] list(String path) throws FileNotFoundException {
        synchronized (ZipFileInputStreamFileSystem.class){
            Optional<Archive> a = entry(path);
            if(a.isPresent() && a.get().isDirectory){
                return a.get().list();
            }else{
                return null;
            }
        }
    }

    // ----------------------------------------------------------------------- //


    @Override
    protected Optional<InputChannel> openInputChannel(String path) throws IOException {
        synchronized (ZipFileInputStreamFileSystem.class){
            return entry(path).map(e -> new InputStreamChannel(e.openStream()));
        }
    }

    // ----------------------------------------------------------------------- //

    private Optional<Archive> entry(String path){
        if(path.startsWith("/")){
            path = path.substring(1);
        }
        if(path.endsWith("/")){
            path = path.substring(0, path.length() - 1);
        }
        String cleanPath = "/" + path.replace("\\", "/").replace("//", "/");
        if(cleanPath.equals("/")){
            return Optional.of(archive);
        }else {
            return archive.find(Arrays.asList(cleanPath.split("/")));
        }
    }



    private static final Cache<String, ArchiveDirectory> cache = CacheBuilder.newBuilder()
            .weakValues()
            .build();

    public static ZipFileInputStreamFileSystem fromFile(File file, String innerPath){
        synchronized (ZipFileInputStreamFileSystem.class){
            try{
                ArchiveDirectory directory = cache.get(file.getPath() + ":" + innerPath, () -> {
                    ZipFile zip = new ZipFile(file.getPath());
                    try {
                        String cleanedPath = innerPath;
                        if (cleanedPath.startsWith("/")) {
                            cleanedPath = cleanedPath.substring(1);
                        }
                        if (cleanedPath.endsWith("/")) {
                            cleanedPath = cleanedPath.substring(0, cleanedPath.length() - 1);
                        }
                        ZipEntry rootEntry = zip.getEntry(cleanedPath);
                        if (rootEntry == null || !rootEntry.isDirectory()) {
                            throw new IllegalArgumentException("Root path " + innerPath + " doesn't exist or is not a directory in zip file " + file.getName());
                        }
                        Set<ArchiveDirectory> directories = new HashSet<>();
                        Set<ArchiveFile> files = new HashSet<>();
                        Enumeration<? extends ZipEntry> iterator = zip.entries();
                        while (iterator.hasMoreElements()) {
                            ZipEntry entry = iterator.nextElement();
                            if (entry.getName().startsWith(cleanedPath)) {
                                if (entry.isDirectory()) {
                                    directories.add(new ArchiveDirectory(entry, cleanedPath, new HashSet<>()));
                                } else {
                                    files.add(new ArchiveFile(zip, entry, cleanedPath));
                                }
                            }
                        }
                        ArchiveDirectory root = null;
                        for (Archive e : ImmutableSet.<Archive>builder().addAll(directories).addAll(files).build()) {
                            if (e.path.length() > 0) {
                                String parent = e.path.substring(0, Math.max(e.path.lastIndexOf('/'), 0));
                                Optional<ArchiveDirectory> first = directories.stream().filter(d -> d.path.equals(parent)).findFirst();
                                if (first.isPresent()) {
                                    first.get().children.add(e);
                                }
                            } else {
                                assert e instanceof ArchiveDirectory;
                                root = (ArchiveDirectory) e;
                            }
                        }
                        return root;
                    } finally {
                        zip.close();
                    }
                });
                if(directory != null){
                    return new ZipFileInputStreamFileSystem(directory);
                }else {
                    return null;
                }
            }catch(Throwable t){
                Log.warn("Failed creating ZIP file system", t);
                return null;
            }
        }
    }


    static abstract class Archive {

        private final ZipEntry entry;
        private final String root;
        public final String path;
        protected final String name;
        private final long lastModified;
        private final boolean isDirectory;

        public Archive(ZipEntry entry, String root) {
            this.entry = entry;
            this.root = root;

            String path = entry.getName();
            if(path.startsWith(root)){
                path = path.substring(root.length());
            }
            if(path.endsWith("/")){
                path = path.substring(0, path.length() - 1);
            }
            this.path = path;

            this.name = path.substring(path.lastIndexOf('/') + 1);
            this.lastModified = entry.getTime();
            this.isDirectory = entry.isDirectory();
        }

        public abstract int size();

        public abstract String[] list();

        public abstract InputStream openStream();

        public abstract Optional<Archive> find(List<String> path);
    }

    private static class ArchiveFile extends Archive {

        private final int size;
        private final byte[] data;

        public ArchiveFile(ZipFile file, ZipEntry entry, String root) throws IOException {
            super(entry, root);

            InputStream inputStream;
            inputStream = file.getInputStream(entry);
            this.data = ByteStreams.toByteArray(inputStream);

            this.size = data.length;
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public String[] list() {
            return null;
        }

        @Override
        public InputStream openStream() {
            return new ByteArrayInputStream(this.data);
        }

        @Override
        public Optional<Archive> find(List<String> path) {
            if(path.size() == 1 && path.get(0).equals(name)){
                return Optional.of(this);
            }
            return Optional.empty();
        }
    }

    private static class ArchiveDirectory extends Archive {

        private final Set<Archive> children;

        public ArchiveDirectory(ZipEntry entry, String root, Set<Archive> children) {
            super(entry, root);
            this.children = children;
        }

        @Override
        public int size() {
            return 0;
        }

        public String[] list(){
            return this.children.stream().map(c -> c.name + (c.isDirectory ? "/" : "")).toArray(String[]::new);
        }

        @Override
        public InputStream openStream() {
            return null;
        }

        @Override
        public Optional<Archive> find(List<String> path) {
            if(path.get(0).equals(name)){
                if(path.size() == 1){
                    return Optional.of(this);
                }
                List<String> subPath = path.subList(1, path.size());
                return children.stream().map(c -> c.find(subPath)).filter(Optional::isPresent).map(Optional::get).findFirst();
            }else return Optional.empty();
        }
    }
}
